package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CLOCKWISE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_MOP_OPEN;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_POLY_APPROX_DP;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RETR_EXTERNAL;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvApproxPoly;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvContourPerimeter;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvConvexHull2;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvEndFindContours;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindNextContour;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvMorphologyEx;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvStartFindContours;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvSubstituteContour;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Iterator;

import com.googlecode.javacpp.IntPointer;
import com.googlecode.javacpp.Loader;
import com.googlecode.javacpp.Pointer;
import com.googlecode.javacpp.PointerPointer;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc.CvContourScanner;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_calib3d.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;

public class HandProcessor {
  private static final int CVCLOSE_ITR = 2;
  private static final int CVCONTOUR_APPROX_LEVEL = 2;
  private static final int MAX_DEPTH = 1600;
  
  private int[] bgDepthMap;
  private IplImage tempImage;
  
  public HandProcessor(int width, int height) {
    tempImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
  }
  
  public void processData(ProcessPacket packet) {
    cvClearMemStorage(packet.tempMem);
    int[] depthRawData = packet.depthRawData;
    IplImage depthImage = packet.depthImage;
    if (bgDepthMap == null) {
      bgDepthMap = packet.depthRawData.clone();
    }
    
    ByteBuffer ib = depthImage.getByteBuffer();
    for (int i = 0; i < depthRawData.length; i++) {
      if (bgDepthMap[i] - depthRawData[i] < 5)
        ib.put(i, (byte)0);
      else ib.put(i, (byte)((char)depthRawData[i] * 255 / MAX_DEPTH));
    }
    
    findConnectedComponents(packet, 0, 4);
    findFingerTips(packet);
  }
  
  /**
   * Cleans up the foreground segmentation mask.
   * @param mask a grayscale (8-bit depth) "raw" mask image that will be cleaned
   *             up.
   * @param poly1_hull0 if 1, approximates connected component by polygon, or
   *                    else convex hull.
   * @param perimScale len = (image.width + image.height) / perimScale. If 
   *                   contour length < len, delete that contour.
   * @return a sequence of contours
   */
  public void findConnectedComponents(ProcessPacket packet, int poly1_hull0, 
                                       float perimScale) {
    packet.hulls.clear();
    packet.approxPoly.clear();
    packet.convexityDefects.clear();
    cvMorphologyEx(packet.depthImage, packet.morphedImage, null, null, 
                   CV_MOP_OPEN, CVCLOSE_ITR);

    cvCopy(packet.morphedImage, tempImage);
    
    CvContourScanner scanner = cvStartFindContours(tempImage, packet.tempMem, 
        Loader.sizeof(CvContour.class), CV_RETR_EXTERNAL, 
        CV_CHAIN_APPROX_SIMPLE);
    CvSeq c;
    double q = (packet.morphedImage.height() + packet.morphedImage.width()) / 
               perimScale;
    while( (c = cvFindNextContour(scanner)) != null ) {
      double len = cvContourPerimeter(c);
      if (len > q) {
        CvSeq approxPoly = cvApproxPoly(c, Loader.sizeof(CvContour.class), 
            packet.tempMem, CV_POLY_APPROX_DP, CVCONTOUR_APPROX_LEVEL, 0);
        CvPoint approxPolyPts = new CvPoint(approxPoly.total());
        cvCvtSeqToArray(approxPoly, approxPolyPts, CV_WHOLE_SEQ);
        CvMat approxPolyMat = cvMat(1, approxPoly.total(), CV_32SC2, 
                                    approxPolyPts);
        packet.approxPoly.add(approxPolyMat);
        // returnPoints = 0: returns pointers to the points in the contour
        CvMat hull = cvCreateMat(1, approxPoly.total(), CV_32SC1);
        cvConvexHull2(approxPolyMat, hull, CV_CLOCKWISE, 0);
        packet.hulls.add(hull);
        packet.convexityDefects.add(
            cvConvexityDefects(approxPoly, hull, packet.tempMem));
      } 
    }
  }
  
  public void findFingerTips(ProcessPacket packet) {
    packet.fingerTips.clear();
    for(int i = 0; i < packet.hulls.size(); i++) {
      CvMat approxPoly = packet.approxPoly.get(i);
      CvRect rect = cvBoundingRect(approxPoly, 0); 
      int cutoff = rect.y() + rect.height() - 50;
      CvMat hull = packet.hulls.get(i);
      for (int j = 0; j < hull.length(); j++) {
        int index = (int)hull.get(j);
        int x = (int)approxPoly.get(index * 2);
        int y = (int)approxPoly.get(index * 2 + 1);
        if (y >= cutoff)
          packet.fingerTips.add(new CvPoint(x, y));
      }
    }
  }
  
  public void cleanUp() {
    tempImage.release();
    System.out.println("HandProcessor cleaned up.");
  }
  
}
