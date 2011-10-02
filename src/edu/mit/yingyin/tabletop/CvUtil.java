package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
import static com.googlecode.javacv.cpp.opencv_core.cvCircle;
import static com.googlecode.javacv.cpp.opencv_core.cvFillConvexPoly;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;

import java.awt.Point;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc.CvConvexityDefect;

/**
 * Utility functions that wrapps around OpenCV funtions.
 * @author yingyin
 *
 */
public class CvUtil {
  public static int distance2(CvPoint p1, CvPoint p2) {
    return (p1.x() - p2.x()) * (p1.x() - p2.x()) + 
           (p1.y() - p2.y()) * (p1.y() - p2.y());
  }
  
  public static CvPoint midPoint(CvPoint p1, CvPoint p2) {
    return new CvPoint((p1.x() + p2.x()) / 2, (p1.y() + p2.y()) / 2 );
  }
  
  public static void drawConvexityDefects(CvSeq seqConvexityDefects, 
                                          IplImage image) {
    for (int i = 0; i < seqConvexityDefects.total(); i++) {
      CvConvexityDefect defect = new CvConvexityDefect(
          cvGetSeqElem(seqConvexityDefects, i));
      CvPoint points = new CvPoint(3);
      points.position(0).x(defect.start().x()).y(defect.start().y());
      points.position(1).x(defect.depth_point().x()).
                         y(defect.depth_point().y());
      points.position(2).x(defect.end().x()).y(defect.end().y());
      cvFillConvexPoly(image, points.position(0), 3, CvScalar.WHITE, CV_AA, 0);
    }
  }
  
  public static void drawHullCorners(CvMat hullIndices, CvMat points, 
                                     IplImage image) {
    for (int i = 0; i < hullIndices.length(); i++) {
      int idx = (int)hullIndices.get(i);
      Point p = new Point((int)points.get(idx * points.channels()),
                          (int)points.get(idx * points.channels() + 1));
      cvCircle(image, new CvPoint(p.x, p.y), 4, CvScalar.WHITE, 1, 8, 0);
    }
  }
  
  /**
   * Converts an integer array to a IplImage with single channel.
   * @param intArray
   * @param image
   */
  public static void intToIplImage(int[] intArray, IplImage image) {
    ByteBuffer buffer = image.getByteBuffer();
    buffer.rewind();
    switch (image.depth()) {
      case 8:
        for (int value : intArray)
          buffer.put((byte)value);
        break;
      case 16:
        ShortBuffer sb = buffer.asShortBuffer();
        for (int value : intArray)
          sb.put((short)value);
        break;
      case 32:
        IntBuffer ib = buffer.asIntBuffer();
        for (int value : intArray)
          ib.put(value);
        break;
      default:
        throw new IllegalArgumentException();
    }
  }
  
  /**
   * Scales an integer array to a float image with values between 0 and 1.
   * 
   * So image[i] = raw[i] / scale.
   * 
   * @param raw
   * @param image
   * @param scale the factor for conversion.
   */
  public static void intToFloatImage(int[] raw, IplImage image, int scale) {
    FloatBuffer fb = image.getFloatBuffer();
    fb.rewind();
    // Converts to float.
    while(fb.remaining() > 0) {
      int pos = fb.position();
      float depth = (float)raw[pos] / scale;
      fb.put(depth > 1 ? 1 : depth);
    }
  }
}
