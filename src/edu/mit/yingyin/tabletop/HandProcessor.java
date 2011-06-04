package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateMemStorage;
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

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc.CvContourScanner;

public class HandProcessor {
  private static final int CVCLOSE_ITR = 2;
  private static final int CVCONTOUR_APPROX_LEVEL = 2;
  private static final int MAX_DEPTH = 1600;
  
  private int[] bgDepthMap;
  private CvMemStorage tempMem;
  private IplImage tempImage;
  
  public HandProcessor(int width, int height) {
    tempMem = cvCreateMemStorage(0);
    tempImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
  }
  
  public void processData(ProcessPacket packet) {
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
    
    packet.contours = findConnectedComponents(depthImage, 0, 4);
  }
  
  public CvSeq findConnectedComponents(IplImage mask, int poly1_hull0, 
                                      float perimScale) {
    cvMorphologyEx(mask, mask, null, null, CV_MOP_OPEN, CVCLOSE_ITR);
    cvClearMemStorage(tempMem);

    cvCopy(mask, tempImage);
    
    CvContourScanner scanner = cvStartFindContours(tempImage, tempMem, 
        Loader.sizeof(CvContour.class), CV_RETR_EXTERNAL, 
        CV_CHAIN_APPROX_SIMPLE);
    CvSeq c;
    int numCont = 0;
    double q = (mask.height() + mask.width()) / perimScale;
    while( (c = cvFindNextContour(scanner)) != null ) {
      double len = cvContourPerimeter(c);
      if (len < q) {
        cvSubstituteContour(scanner, null);
      } else {
        CvSeq cNew;
        if (poly1_hull0 == 1) {
          cNew = cvApproxPoly(c, Loader.sizeof(CvContour.class), tempMem,
              CV_POLY_APPROX_DP, CVCONTOUR_APPROX_LEVEL, 0);
        } else {
          cNew = cvConvexHull2(c, tempMem, CV_CLOCKWISE, 1);
        }
        cvSubstituteContour(scanner, cNew);
        numCont++;
      } 
    }
    System.out.println("Number of contours = " + numCont);
    return cvEndFindContours(scanner);
  }
  
  public void cleanUp() {
    cvClearMemStorage(tempMem);
    tempImage.release();
    System.out.println("HandProcessor cleaned up.");
  }
  
}
