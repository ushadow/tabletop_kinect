package edu.mit.yingyin.util;

import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
import static com.googlecode.javacv.cpp.opencv_core.CV_FONT_HERSHEY_SIMPLEX;
import static com.googlecode.javacv.cpp.opencv_core.cvCircle;
import static com.googlecode.javacv.cpp.opencv_core.cvFillConvexPoly;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvInitFont;
import static com.googlecode.javacv.cpp.opencv_core.cvPutText;
import static com.googlecode.javacv.cpp.opencv_core.cvPolyLine;

import java.awt.Point;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.vecmath.Point2f;

import com.googlecode.javacv.cpp.opencv_core.CvFont;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
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
  
  public static boolean pointInRect(CvPoint p, CvRect rect) {
    if (p.x() >= rect.x() && p.x() <= rect.x() + rect.width() && 
        p.y() >= rect.y() && p.y() <= rect.y() + rect.height())
      return true;
    return false;
  }

  // Type conversion.
  public static Point2f toPoint2f(CvPoint p) {
    return new Point2f(p.x(), p.y());
  }
  
  // Drawing methods.
  /**
   * Draws the convexity defects on the image.
   * @param seqConvexityDefects
   * @param image
   * @param showLabels if true, display the convexity defect index.
   */
  public static void drawConvexityDefects(CvSeq seqConvexityDefects, 
      IplImage image, boolean showLabels) {
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
      
      if (showLabels)
      {
        CvFont font = new CvFont();
        cvInitFont(font, CV_FONT_HERSHEY_SIMPLEX, 0.5, 0.5, 0, 1, CV_AA);
        cvPutText(image, String.format("%d", i), points.position(0), font, 
                  CvScalar.RED);
      }
    }
  }
  
  public static void drawHullCorners(CvMat hullIndices, CvMat points, 
                                     IplImage image) {
    int numChannels = points.channels();
    for (int i = 0; i < hullIndices.length(); i++) {
      int idx = (int) hullIndices.get(i);
      Point p = new Point((int) points.get(idx * numChannels),
                          (int) points.get(idx * numChannels + 1));
      cvCircle(image, new CvPoint(p.x, p.y), 4, CvScalar.WHITE, 1, 8, 0);
    }
  }
  
  public static void drawHull(CvMat hullIndices, CvMat points, IplImage image) {
    CvPoint[] polygons = new CvPoint[1];
    polygons[0] = new CvPoint(hullIndices.length());
    int numChannels = points.channels();
    for (int i = 0; i < hullIndices.length(); i++) {
      int idx = (int) hullIndices.get(i);
      polygons[0].position(i).x((int) points.get(idx * numChannels)).
                              y((int) points.get(idx * numChannels + 1));
    }
    cvPolyLine(image, polygons, new int[]{hullIndices.length()}, 1, 1, 
        CvScalar.RED, 2, CV_AA, 0); 
  }
  
  
  // Image conversion methods.
  
  /**
   * Converts an integer array to an unsigned or signed IplImage with single 
   * channel.
   * 
   * @param intArray
   * @param image
   */
  public static void intToIntIplImage(int[] intArray, IplImage image, 
      float scale) {
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
            buffer.put(h * widthStep + w, 
                (byte)(intArray[h * imageWidth + w] * scale));
          }
        break;
      case 16:
        ShortBuffer sb = buffer.asShortBuffer();
        widthStep /= 2;
        for (int h = 0; h < imageHeight; h++)
          for (int w = 0; w < imageWidth; w++) {
            sb.put(h * widthStep + w, 
                (short) (intArray[h * imageWidth + w] * scale));
          }
        break;
      case 32:
        IntBuffer ib = buffer.asIntBuffer();
        widthStep /= 4;
        for (int h = 0; h < imageHeight; h++)
          for (int w = 0; w < imageWidth; w++) {
            ib.put(h * widthStep + w, 
                (int) (intArray[h * imageWidth + w] * scale));
          }
        break;
      default:
        throw new IllegalArgumentException();
    }
  }
  
  /**
   * Converts an integer array to a float <code>IplImage</code> with scaling.
   * 
   * So image[i] = raw[i] * scale.
   * 
   * @param raw
   * @param image an IplImage of type float (32-bit).
   * @param scale scaling factor to be multiplied to the raw value.
   */
  public static void intToFloatIplImage(int[] raw, IplImage image, float scale) 
  {
    FloatBuffer fb = image.getFloatBuffer();
    int height = image.height();
    int width = image.width();
    int widthStep = image.widthStep() / 4;
    // Converts to float.
    for (int h = 0; h < height; h++)
      for (int w = 0; w < width; w++) {
        float depth = (float) raw[h * width + w] * scale;
        fb.put(h * widthStep + w, depth);
    }
  }
  
  /**
   * Output image values to an output stream.
   * @param pw
   * @param image a float type IplImage.
   */
  public static void saveImage(PrintWriter pw, IplImage image) {
    int widthStep = image.widthStep();
    int depth = image.depth();
    FloatBuffer fb = image.getFloatBuffer();
    for (int h = 0; h < image.height(); h++){
      for (int w = 0; w < image.width(); w++) 
        pw.print(fb.get(h * widthStep * 8 / depth + w) + " ");
      pw.println();
    }
  }
}
