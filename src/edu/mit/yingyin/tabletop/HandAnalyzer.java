package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.CV_16SC1;
import static com.googlecode.javacv.cpp.opencv_core.CV_32SC1;
import static com.googlecode.javacv.cpp.opencv_core.CV_32SC2;
import static com.googlecode.javacv.cpp.opencv_core.CV_8UC1;
import static com.googlecode.javacv.cpp.opencv_core.CV_WHOLE_SEQ;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvConvert;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateMat;
import static com.googlecode.javacv.cpp.opencv_core.cvCvtSeqToArray;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSubRect;
import static com.googlecode.javacv.cpp.opencv_core.cvMat;
import static com.googlecode.javacv.cpp.opencv_core.cvRect;
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
import static com.googlecode.javacv.cpp.opencv_imgproc.cvSobel;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvStartFindContours;

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

import edu.mit.yingyin.tabletop.Forelimb.ValConfiPair;
import edu.mit.yingyin.tabletop.ProcessPacket.ForelimbFeatures;

public class HandAnalyzer {
  /**
   * Maximum depth in mm for the tabletop application.
   */
  public static final int MAX_DEPTH = 1600;
  /**
   * Number of initial frames to initialize the background.
   */
  private static final int BG_INIT_FRAMES = 30;
  /**
   * The number of iterations of morphological transformation. 
   */
  private static final int MORPH_ITR = 1;
  private static final int CONTOUR_APPROX_LEVEL = 2;
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
  private static final float TEMPORAL_FILTER_PARAM = (float)0.16;
  
  private Background background;
  private IplImage tempImage;
  private List<Forelimb> prevForelimbsFeatures = new ArrayList<Forelimb>();
  private IplImage foregroundMask;
  private ForelimbFeatureDetector  ffd = new ForelimbFeatureDetector();
  
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
  
  /**
   * Hand data analysis pipeline.
   * @param packet contains all the relevant data for analysis.
   */
  public void analyzeData(ProcessPacket packet) {
    prevForelimbsFeatures.clear();
    for (Forelimb forelimb : packet.foreLimbs)
      prevForelimbsFeatures.add(new Forelimb(forelimb));
    packet.clear();
    
    CvUtil.intToFloatImage(packet.depthRawData, packet.depthImage32F, 
                           MAX_DEPTH);
    cvSobel(packet.depthImage32F, packet.derivative, 2, 2, 5);
    
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
    ffd.extractFingertipsConvexityDefects(packet);
    //temporalSmooth(packet);
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
    // The default 3x3 kernel with the anchor at the center is used.
    // The opening operator involves erosion followed by dilation. Its effect is
    // to eliminate lone outliers that are higher in intensity (bumps) than 
    // their neighbors.
    cvMorphologyEx(packet.depthImage8U, packet.morphedImage, null, null, 
        CV_MOP_OPEN, MORPH_ITR);
  }
  
  /**
   * Cleans up the foreground segmentation mask, and finds connected components
   * as forelimbs.
   * 
   * @param packet ProcessPacket containing the data necessary for the analysis.
   * @param perimScale len = (image.width + image.height) / perimScale. If 
   *                   contour length < len, delete that contour.
   * @return a sequence of contours
   */
  private void findConnectedComponents(ProcessPacket packet, float perimScale) {
    cvCopy(packet.morphedImage, tempImage);
    
    // CV_RETR_EXTERNAL: retrieves only the extreme outer contours.
    // CV_CHAIN_APPROX_SIMPLE: compresses horizontal, vertical, and diagonal 
    // segments, leaving only their ending points.
    CvContourScanner scanner = cvStartFindContours(tempImage, packet.tempMem, 
        Loader.sizeof(CvContour.class), CV_RETR_EXTERNAL, 
        CV_CHAIN_APPROX_SIMPLE);
    CvSeq c;
    double q = (packet.morphedImage.height() + packet.morphedImage.width()) / 
               perimScale;
    while( (c = cvFindNextContour(scanner)) != null ) {
      double len = cvContourPerimeter(c);
      if (len > q) {
        ForelimbFeatures ff = new ForelimbFeatures();
        // Approximates the contour with fewer vertices.
        CvSeq approxPoly = cvApproxPoly(c, Loader.sizeof(CvContour.class), 
            packet.tempMem, CV_POLY_APPROX_DP, CONTOUR_APPROX_LEVEL, 0);
        CvPoint approxPolyPts = new CvPoint(approxPoly.total());
        cvCvtSeqToArray(approxPoly, approxPolyPts, CV_WHOLE_SEQ);
        ff.approxPoly = cvMat(1, approxPoly.total(), CV_32SC2, approxPolyPts);
        ff.boundingBox = cvBoundingRect(ff.approxPoly, 0);
        ff.hull = cvCreateMat(1, approxPoly.total(), CV_32SC1);
        // returnPoints = 0: returns pointers to the points in the contour
        cvConvexHull2(ff.approxPoly, ff.hull, CV_CLOCKWISE, 0);
        ff.convexityDefects = cvConvexityDefects(ff.approxPoly, ff.hull, 
                                                 packet.tempMem);
        packet.forelimbFeatures.add(ff);
      } 
    }
  }
  
  /**
   * Finds rectangle hand regions from forelimb regions.
   * 
   * @param packet contains all the processing information.
   * 
   * TODO(yingyin): find hand region based on arm orientation. 
   */
  private void findHandRegions(ProcessPacket packet) {
    int handHeight = packet.height / HAND_HEIGHT_SCALE;
    for (ForelimbFeatures ff : packet.forelimbFeatures) {
      CvRect rect = ff.boundingBox;
      if (rect.height() >= handHeight) {
        int ymid = packet.height / 2;
        int y1 = rect.y();
        int y2 = y1 + rect.height();
        int yhand = y1;
        if (Math.abs(y1 - ymid) > Math.abs(y2 - ymid)) 
          yhand = y2 - handHeight;
        ff.handRegion = 
            new Rectangle(rect.x(), yhand, rect.width(), handHeight);
      } 
    }
  }
  
  private void findDerivative(ProcessPacket packet) {
    for (ForelimbFeatures ff: packet.forelimbFeatures) {
      Rectangle hr = ff.handRegion;
      CvMat submat = cvMat(hr.height, hr.width, CV_8UC1, null);
      CvMat derivativeSubmat = cvMat(hr.height, hr.width, CV_16SC1, null);
      CvRect rect = cvRect(hr.x, hr.y, hr.width, hr.height);
      cvGetSubRect(packet.morphedImage, submat, rect);
      cvSobel(submat, derivativeSubmat, 1, 0, 5);
      cvConvert(derivativeSubmat, submat);
      submat.release();
    }
  }

  /**
   * Exponentially weighted moving average filter, i.e. low pass filter.
   * @param packet
   */
  private void temporalSmooth(ProcessPacket packet) {
    for (Forelimb forelimb : packet.foreLimbs) 
      for (Forelimb prevForelimb : prevForelimbsFeatures) {
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
