package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
import static com.googlecode.javacv.cpp.opencv_core.CV_FONT_HERSHEY_SIMPLEX;
import static com.googlecode.javacv.cpp.opencv_core.cvCircle;
import static com.googlecode.javacv.cpp.opencv_core.cvFillConvexPoly;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvInitFont;
import static com.googlecode.javacv.cpp.opencv_core.cvPutText;

import java.awt.Point;
import java.awt.Rectangle;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.vecmath.Point2f;

import com.googlecode.javacv.cpp.opencv_core.CvFont;
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
  // Geometry methods.
  
  public static int distance2(CvPoint p1, CvPoint p2) {
    return (p1.x() - p2.x()) * (p1.x() - p2.x()) + 
           (p1.y() - p2.y()) * (p1.y() - p2.y());
  }
  
  public static CvPoint midPoint(CvPoint p1, CvPoint p2) {
    return new CvPoint((p1.x() + p2.x()) / 2, (p1.y() + p2.y()) / 2 );
  }
  
  public static boolean pointInRect(CvPoint p, Rectangle rect) {
    if (p.x() >= rect.x && p.x() <= rect.x + rect.width && p.y() >= rect.y &&
        p.y() <= rect.y + rect.height)
      return true;
    return false;
  }

  // Type conversion.
  public static Point2f toPoint2f(CvPoint p) {
    return new Point2f(p.x(), p.y());
  }
  
  // Drawing methods.
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
      // CV_AA: antialiased.
      cvFillConvexPoly(image, points.position(0), 3, CvScalar.WHITE, CV_AA, 0);
      
      CvFont font = new CvFont();
      cvInitFont(font, CV_FONT_HERSHEY_SIMPLEX, 0.5, 0.5, 0, 1, CV_AA);
      cvPutText(image, String.format("%d", i), points.position(0), font, 
                CvScalar.WHITE);
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
  
  // Image conversion methods.
  
  /**
   * Converts an integer array to a IplImage with single channel.
   * @param intArray
   * @param image
   */
  public static void intToIplImage(int[] intArray, IplImage image) {
    ByteBuffer buffer = image.getByteBuffer();
    // For IplImage, image width is not necessarily equal to the width step in 
    // of the buffer.
    int imageWidth = image.width();
    int imageHeight = image.height();
    int widthStep = image.widthStep(); // In number of bytes.
    switch (image.depth()) {
      case 8:
        for (int h = 0; h < imageHeight; h++)
          for (int w = 0; w < imageWidth; w++) {
            buffer.put(h * widthStep + w, (byte)intArray[h * imageWidth + w]);
          }
        break;
      case 16:
        ShortBuffer sb = buffer.asShortBuffer();
        widthStep /= 2;
        for (int h = 0; h < imageHeight; h++)
          for (int w = 0; w < imageWidth; w++) {
            sb.put(h * widthStep + w, (short)intArray[h * imageWidth + w]);
          }
        break;
      case 32:
        IntBuffer ib = buffer.asIntBuffer();
        widthStep /= 4;
        for (int h = 0; h < imageHeight; h++)
          for (int w = 0; w < imageWidth; w++) {
            ib.put(h * widthStep + w, intArray[h * imageWidth + w]);
          }
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
   * @param image an IplImage of type float (32-bit).
   * @param scale the factor for conversion.
   */
  public static void intToFloatImage(int[] raw, IplImage image, int scale) {
    FloatBuffer fb = image.getFloatBuffer();
    int height = image.height();
    int width = image.width();
    int widthStep = image.widthStep() / 4;
    // Converts to float.
    for (int h = 0; h < height; h++)
      for (int w = 0; w < width; w++) {
        float depth = (float)raw[h * width + w] / scale;
        fb.put(h * widthStep + w, depth > 1 ? 1 : depth);
    }
  }
}
