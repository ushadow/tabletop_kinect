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
  /**
   * Maximum depth in mm for the tabletop application.
   */
  public static final int MAX_DEPTH = 1600;
  /**
   * Number of initial frames to initialize the background.
   */
  private static final int BG_INIT_FRAMES = 30;
  private static final int CVCLOSE_ITR = 2;
  private static final int CVCONTOUR_APPROX_LEVEL = 2;
  /**
   * The ratio between the perimeter of the table and the perimeter of the hand.
   * Assumes hand's dimension is w = 15cm, h = 15cm, and the table's dimension 
   * is w = 122cm, h = 92cm.
   */
  private static final int HAND_PERIM_SCALE = 7;
  /**
   * The ratio between the height of the table and the minimum height of the 
   * hand. Assumes minimum height of the hand is 8cm when only part of the hand 
   * is in the view.
   */
  private static final int HAND_HEIGHT_SCALE = 11;
  private static final float FINGERTIP_ANGLE_THRESH = (float)1.6;
  private static final float TEMPORAL_FILTER_PARAM = (float)0.16;
  private static int[] THINNING_KERNEL_ORTH = {0, 0, 0, 2, 1, 2, 1, 1, 1};
  private static int[] THINNING_KERNEL_DIAG = {2, 0, 0, 1, 1, 0, 2, 1, 2};
  private static int[] PRUNING_KERNEL1 = {0, 0, 0, 0, 1, 0, 0, 2, 2};
  private static int[] PRUNING_KERNEL2 = {0, 0, 0, 0, 1, 0, 2, 2, 0};
  
  private Background background;
  private IplImage tempImage;
  private List<ForelimbModel> prevForelimbsFeatures = 
      new ArrayList<ProcessPacket.ForelimbModel>();
  private IplImage foregroundMask;
  
  /**
   * Initializes the data structures.
   * @param width
   * @param height
   */
  public HandAnalyzer(int width, int height) {
    tempImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    background = new Background(width, height, MAX_DEPTH);
    foregroundMask = IplImage.create(width, height, IPL_DEPTH_8U, 1);
  }
  
  public void analyzeData(ProcessPacket packet) {
    prevForelimbsFeatures.clear();
    for (ForelimbModel forelimb : packet.foreLimbsFeatures)
      prevForelimbsFeatures.add(new ForelimbModel(forelimb));
    packet.clear();
    
    if (packet.depthFrameID < BG_INIT_FRAMES) {
      background.accumulateBackground(packet.depthRawData);
      return;
    } else if (packet.depthFrameID == BG_INIT_FRAMES) {
      background.createModelsFromStats((float)6.0, (float)7.0);
      System.out.println(background.stats());
    }
    
    subtractBackground(packet);
    cleanUpBackground(packet);
    findConnectedComponents(packet, HAND_PERIM_SCALE);
    findHandRegions(packet);
    thinningHands(packet);
    findForelimbFeatures(packet);

    temporalSmooth(packet);
  }
  
  public void release() {
    tempImage.release();
    background.release();
    foregroundMask.release();
    System.out.println("HandAnalyzer released.");
  } 
  
  protected void subtractBackground(ProcessPacket packet) {
    int[] depthData = packet.depthRawData;
    IplImage depthImage = packet.depthImage8U;
 
    background.backgroundDiff(depthData, foregroundMask);
    ByteBuffer depthBuffer = depthImage.getByteBuffer();
    ByteBuffer maskBuffer = foregroundMask.getByteBuffer();
    int maskWidthStep = foregroundMask.widthStep();
    int depthWidthStep = depthImage.widthStep();
    int width = packet.width;
    for (int h = 0; h < packet.height; h++)
      for (int w = 0; w < packet.width; w++) {
        int pos = h * depthWidthStep + w;
        if ((maskBuffer.get(h * maskWidthStep + w) & 0xff) == 255) {
          depthBuffer.put(pos, 
                          (byte)(depthData[h * width + w] * 255 / MAX_DEPTH));
        } else {
          depthBuffer.put(pos, (byte)0);
        }
      }
  }
  
  /**
   * Cleans up the background subtracted image.
   * @param packet ProcessPacket containing the data.
   */
  private void cleanUpBackground(ProcessPacket packet) {
    // The default 3x3 kernel with the anchor at the the center is used.
    // The opening operator involves erosion followed by dilation.
    cvMorphologyEx(packet.depthImage8U, packet.morphedImage, null, null, 
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
  
  private void findHandRegions(ProcessPacket packet) {
    int handHeight = packet.height / HAND_HEIGHT_SCALE;
    for (CvRect rect : packet.boundingBoxes) {
      if (rect.height() >= handHeight) {
        int ymid = packet.height / 2;
        int y1 = rect.y();
        int y2 = y1 + rect.height();
        int yhand = y1;
        if (Math.abs(y1 - ymid) > Math.abs(y2 - ymid)) 
          yhand = y2 - handHeight;
        packet.handRegions.add(
            new Rectangle(rect.x(), yhand, rect.width(), handHeight));
      } else {
        packet.handRegions.add(null);
      }
    }
  }

  private void thinningHands(ProcessPacket packet) {
    ByteBuffer bb = packet.morphedImage.getByteBuffer();
    for (Rectangle rect : packet.handRegions) {
      if (rect != null) {
        byte[][] pixels = new byte[rect.height][rect.width];
        for (int dy = 0; dy < rect.height; dy++) 
          for (int dx = 0; dx < rect. width; dx++) {
            int index = (rect.y + dy) * packet.morphedImage.width() + rect.x + dx;
            if (bb.get(index) == 0)
              pixels[dy][dx] = BinaryFast.background;
            else pixels[dy][dx] = BinaryFast.foreground;
          }
        BinaryFast bf = new BinaryFast(pixels, rect.width, rect.height);
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
        for (int dy = 0; dy < rect.height; dy++) 
          for (int dx = 0; dx < rect. width; dx++) {
            int index = (rect.y + dy) * packet.morphedImage.width() + 
                        rect.x + dx;
            if (pixels[dy][dx] == BinaryFast.background)
              bb.put(index, (byte)0);
            else bb.put(index, (byte)255);
          }
      }
    }
  }
  
  private void findForelimbFeatures(ProcessPacket packet) {
    for (int i = 0; i < packet.hulls.size(); i++) {
      Rectangle handRect = packet.handRegions.get(i);
      if (handRect != null) {
        ForelimbModel forelimb = new ForelimbModel();
        List<ValConfiPair<Point3f>> fingerTips = 
            new ArrayList<ValConfiPair<Point3f>>();
        
        CvMat hull = packet.hulls.get(i);
        CvMat approxPoly = packet.approxPolys.get(i);
        CvRect rect = packet.boundingBoxes.get(i);
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
          if (angle < FINGERTIP_ANGLE_THRESH && C.y >= handRect.y && 
              C.y <= handRect.y + handRect.height) {
            float z = packet.depthRawData[C.y * packet.depthImage8U.width() + 
                                          C.x];
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
