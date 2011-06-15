package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CLOCKWISE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_MOP_OPEN;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_POLY_APPROX_DP;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RETR_EXTERNAL;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvApproxPoly;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvContourPerimeter;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvConvexHull2;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindNextContour;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvMorphologyEx;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvStartFindContours;

import java.awt.Point;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc.CvContourScanner;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;

import edu.mit.yingyin.tabletop.ProcessPacket.ForelimbFeatures;
import edu.mit.yingyin.tabletop.ProcessPacket.ForelimbFeatures.ValConfiPair;
import edu.mit.yingyin.util.Geometry;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_calib3d.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;

public class HandProcessor {
  private static final int CVCLOSE_ITR = 2;
  private static final int CVCONTOUR_APPROX_LEVEL = 2;
  private static final int MAX_DEPTH = 1600;
  private static final int HAND_YCUTOFF = 50;
  private static final int FOREGROUND_THRESH = 3;
  private static final int PERIM_SCALE = 4;
  private static final float FINGERTIP_ANGLE_THRESH = (float)1.6;
  private static final float TEMPORAL_FILTER_PARAM = (float)0.16;
  
  private int[] bgDepthMap;
  private IplImage tempImage;
  private List<ForelimbFeatures> prevForelimbsFeatures = 
      new ArrayList<ProcessPacket.ForelimbFeatures>();
  
  public HandProcessor(int width, int height) {
    tempImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
  }
  
  public void processData(ProcessPacket packet) {
    prevForelimbsFeatures.clear();
    for (ForelimbFeatures forelimb : packet.foreLimbsFeatures)
      prevForelimbsFeatures.add(new ForelimbFeatures(forelimb));
    packet.clear();
    
    int[] depthRawData = packet.depthRawData;
    IplImage depthImage = packet.depthImage;
    if (bgDepthMap == null) {
      bgDepthMap = packet.depthRawData.clone();
    }
    
    ByteBuffer ib = depthImage.getByteBuffer();
    for (int i = 0; i < depthRawData.length; i++) {
      if (bgDepthMap[i] - depthRawData[i] < FOREGROUND_THRESH)
        ib.put(i, (byte)0);
      else ib.put(i, (byte)((char)depthRawData[i] * 255 / MAX_DEPTH));
    }
    
    findConnectedComponents(packet, PERIM_SCALE);
    processForelimbFeatures(packet);
    temporalSmooth(packet);
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
  public void findConnectedComponents(ProcessPacket packet, float perimScale) {
   
    // Cleans up the background subtracted image.
    // The default 3x3 kernel with the anchor at the the center is used.
    // The opening operator involves erosion followed by dilation.
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
        packet.approxPolys.add(approxPolyMat);
        CvMat hull = cvCreateMat(1, approxPoly.total(), CV_32SC1);
        // returnPoints = 0: returns pointers to the points in the contour
        cvConvexHull2(approxPolyMat, hull, CV_CLOCKWISE, 0);
        packet.hulls.add(hull);
        packet.convexityDefects.add(
            cvConvexityDefects(approxPolyMat, hull, packet.tempMem));
      } 
    }
  }
  
  public void processForelimbFeatures(ProcessPacket packet) {
    for (int i = 0; i < packet.hulls.size(); i++) {
      ForelimbFeatures forelimb = new ForelimbFeatures();
      List<ValConfiPair<Point>> fingerTips = 
          new ArrayList<ValConfiPair<Point>>();
      
      CvMat hull = packet.hulls.get(i);
      CvMat approxPoly = packet.approxPolys.get(i);
      CvRect rect = cvBoundingRect(approxPoly, 0); 
      int cutoff = rect.y() + rect.height() - HAND_YCUTOFF;
      int numPolyPts = approxPoly.length();

      for (int j = 0; j < hull.length(); j++) {
        int idx = (int)hull.get(j);
        int pdx = (idx - 1 + numPolyPts) % numPolyPts;
        int sdx = (idx + 1) % numPolyPts;
        Point C = new Point((int)approxPoly.get(idx * 2), 
                            (int)approxPoly.get(idx * 2 + 1));
        Point A = new Point((int)approxPoly.get(pdx * 2), 
                            (int)approxPoly.get(pdx * 2 + 1));
        Point B = new Point((int)approxPoly.get(sdx * 2),
                            (int)approxPoly.get(sdx * 2 + 1));
        
        float angle = (float)Geometry.getAngleC(A, B, C);
        if (angle < FINGERTIP_ANGLE_THRESH && C.y >= cutoff) {
          fingerTips.add(new ValConfiPair<Point>(C, 1));
        }
      }
      forelimb.fingertips = fingerTips;

      forelimb.center = new Point(rect.x() + rect.width() / 2, 
          rect.y() + rect.height() / 2);
      
      packet.foreLimbsFeatures.add(forelimb);
    }
  }
  
  /**
   * Exponentially weighted moving average filter, i.e. low pass filter.
   * @param packet
   */
  public void temporalSmooth(ProcessPacket packet) {
    for (ForelimbFeatures forelimb : packet.foreLimbsFeatures) 
      for (ForelimbFeatures prevForelimb : prevForelimbsFeatures) {
        if (forelimb.center.distanceSq(prevForelimb.center) < 100) {
          for (ValConfiPair<Point> fingertip : forelimb.fingertips) { 
            fingertip.confidence *= TEMPORAL_FILTER_PARAM;
            for (ValConfiPair<Point> prevFingertip : prevForelimb.fingertips) {
              if (fingertip.value.distanceSq(prevFingertip.value) < 64) {
                fingertip.confidence += (1 - TEMPORAL_FILTER_PARAM) *
                                        prevFingertip.confidence;
                break;
              }
            }
          }
          break;
        }
      }
  }
  
  public void cleanUp() {
    tempImage.release();
    System.out.println("HandProcessor cleaned up.");
  }
  
}
