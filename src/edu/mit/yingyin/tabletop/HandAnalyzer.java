package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.CV_32SC1;
import static com.googlecode.javacv.cpp.opencv_core.CV_32SC2;
import static com.googlecode.javacv.cpp.opencv_core.CV_WHOLE_SEQ;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateMat;
import static com.googlecode.javacv.cpp.opencv_core.cvCvtSeqToArray;
import static com.googlecode.javacv.cpp.opencv_core.cvMat;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CLOCKWISE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_MOP_OPEN;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_POLY_APPROX_DP;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RETR_EXTERNAL;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvApproxPoly;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvBoundingRect;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvContourPerimeter;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvConvexHull2;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvConvexityDefects;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindNextContour;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvMorphologyEx;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvStartFindContours;

import java.awt.Point;
import java.awt.Rectangle;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc.CvContourScanner;

import edu.mit.yingyin.image.BinaryFast;
import edu.mit.yingyin.image.ThinningTransform;
import edu.mit.yingyin.tabletop.ProcessPacket.ForelimbModel;
import edu.mit.yingyin.tabletop.ProcessPacket.ForelimbModel.ValConfiPair;
import edu.mit.yingyin.util.Geometry;
import edu.mit.yingyin.util.Matrix;

public class HandAnalyzer {
  public static final int HAND_YCUTOFF = 50;

  private static final int CVCLOSE_ITR = 2;
  private static final int CVCONTOUR_APPROX_LEVEL = 2;
  private static final int FOREGROUND_THRESH = 3;
  private static final int PERIM_SCALE = 4;
  private static final float FINGERTIP_ANGLE_THRESH = (float)1.6;
  private static final float TEMPORAL_FILTER_PARAM = (float)0.16;
  private static int[] THINNING_KERNEL_ORTH = {0, 0, 0, 2, 1, 2, 1, 1, 1};
  private static int[] THINNING_KERNEL_DIAG = {2, 0, 0, 1, 1, 0, 2, 1, 2};
  private static int[] PRUNING_KERNEL1 = {0, 0, 0, 0, 1, 0, 0, 2, 2};
  private static int[] PRUNING_KERNEL2 = {0, 0, 0, 0, 1, 0, 2, 2, 0};
  
  private int[] bgDepthMap;
  private IplImage tempImage;
  private List<ForelimbModel> prevForelimbsFeatures = 
      new ArrayList<ProcessPacket.ForelimbModel>();
  
  public HandAnalyzer(int width, int height) {
    tempImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
  }
  
  public void analyzeData(ProcessPacket packet) {
    prevForelimbsFeatures.clear();
    for (ForelimbModel forelimb : packet.foreLimbsFeatures)
      prevForelimbsFeatures.add(new ForelimbModel(forelimb));
    packet.clear();
    
    subtractBackground(packet);
//    findConnectedComponents(packet, PERIM_SCALE);
//    thinningHands(packet);
//    findForelimbFeatures(packet);
//    temporalSmooth(packet);
  }
  
  public void release() {
    tempImage.release();
    System.out.println("HandAnalyzer cleaned up.");
  } 
  
  private void subtractBackground(ProcessPacket packet) {
    int[] depthRawData = packet.depthRawData;
    IplImage depthImage = packet.depthImage;
    if (bgDepthMap == null) {
      bgDepthMap = packet.depthRawData.clone();
    }
    
    ByteBuffer ib = depthImage.getByteBuffer();
    for (int i = 0; i < depthRawData.length; i++) {
      if (bgDepthMap[i] - depthRawData[i] < FOREGROUND_THRESH) {
        ib.put(i, (byte)0);
      } else {
        int depth = depthRawData[i];
        depth = depth > Background.MAX_DEPTH ? Background.MAX_DEPTH : depth;
        ib.put(i, (byte)(depth * 255 / Background.MAX_DEPTH));
      }
    }
    // Cleans up the background subtracted image.
    // The default 3x3 kernel with the anchor at the the center is used.
    // The opening operator involves erosion followed by dilation.
    cvMorphologyEx(depthImage, packet.morphedImage, null, null, 
        CV_MOP_OPEN, CVCLOSE_ITR);
  }
  
  /**
   * Cleans up the foreground segmentation mask.
   * @param packet ProcessPacket containing the data necessary for the analysis.
   * @param perimScale len = (image.width + image.height) / perimScale. If 
   *                   contour length < len, delete that contour.
   * @return a sequence of contours
   */
  private void findConnectedComponents(ProcessPacket packet, float perimScale) {
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
        packet.boundingBoxes.add(cvBoundingRect(approxPolyMat, 0));
        CvMat hull = cvCreateMat(1, approxPoly.total(), CV_32SC1);
        // returnPoints = 0: returns pointers to the points in the contour
        cvConvexHull2(approxPolyMat, hull, CV_CLOCKWISE, 0);
        packet.hulls.add(hull);
        packet.convexityDefects.add(
            cvConvexityDefects(approxPolyMat, hull, packet.tempMem));
      } 
    }
  }
  
  private void thinningHands(ProcessPacket packet) {
    ByteBuffer bb = packet.morphedImage.getByteBuffer();
    for (CvRect rect : packet.boundingBoxes) {
      if (rect.height() > HAND_YCUTOFF) {
        Rectangle handRect = new Rectangle(rect.x(), 
            rect.y() + rect.height() - HAND_YCUTOFF, 
            rect.width(), HAND_YCUTOFF);
        byte[][] pixels = new byte[handRect.height][handRect.width];
        for (int dy = 0; dy < handRect.height; dy++) 
          for (int dx = 0; dx < handRect. width; dx++) {
            int index = (handRect.y + dy) * packet.morphedImage.width() + 
                        handRect.x + dx;
            if (bb.get(index) == 0)
              pixels[dy][dx] = BinaryFast.background;
            else pixels[dy][dx] = BinaryFast.foreground;
          }
        BinaryFast bf = new BinaryFast(pixels, handRect.width, handRect.height);
        for (int i = 0; i < 20; i++) {
          ThinningTransform.thinBinaryOnce(bf, THINNING_KERNEL_ORTH);
          ThinningTransform.thinBinaryOnce(bf, THINNING_KERNEL_DIAG);
          Matrix.rot90(THINNING_KERNEL_ORTH, 3);
          Matrix.rot90(THINNING_KERNEL_DIAG, 3);
        }
        for (int i = 0; i < 8; i++) {
          ThinningTransform.thinBinaryOnce(bf, PRUNING_KERNEL1);
          ThinningTransform.thinBinaryOnce(bf, PRUNING_KERNEL2);
          Matrix.rot90(PRUNING_KERNEL1, 3);
          Matrix.rot90(PRUNING_KERNEL2, 3);
        }
        for (int dy = 0; dy < handRect.height; dy++) 
          for (int dx = 0; dx < handRect. width; dx++) {
            int index = (handRect.y + dy) * packet.morphedImage.width() + 
                        handRect.x + dx;
            if (pixels[dy][dx] == BinaryFast.background)
              bb.put(index, (byte)0);
            else bb.put(index, (byte)255);
          }
      }
    }
  }
  
  private void findForelimbFeatures(ProcessPacket packet) {
    for (int i = 0; i < packet.hulls.size(); i++) {
      ForelimbModel forelimb = new ForelimbModel();
      List<ValConfiPair<Point3f>> fingerTips = 
          new ArrayList<ValConfiPair<Point3f>>();
      
      CvMat hull = packet.hulls.get(i);
      CvMat approxPoly = packet.approxPolys.get(i);
      CvRect rect = packet.boundingBoxes.get(i);
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
          float z = packet.depthRawData[C.y * packet.depthImage.width() + C.x];
          fingerTips.add(new ValConfiPair<Point3f>(
              new Point3f(C.x, C.y, z), 1));
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
  private void temporalSmooth(ProcessPacket packet) {
    for (ForelimbModel forelimb : packet.foreLimbsFeatures) 
      for (ForelimbModel prevForelimb : prevForelimbsFeatures) {
        if (forelimb.center.distanceSq(prevForelimb.center) < 100) {
          for (ValConfiPair<Point3f> fingertip : forelimb.fingertips) { 
            fingertip.confidence *= TEMPORAL_FILTER_PARAM;
            for (ValConfiPair<Point3f> prevTip : prevForelimb.fingertips) {
              if (fingertip.value.distanceSquared(prevTip.value) < 64) {
                fingertip.confidence += (1 - TEMPORAL_FILTER_PARAM) *
                                        prevTip.confidence;
                break;
              }
            }
          }
          break;
        }
      }
  }
}
