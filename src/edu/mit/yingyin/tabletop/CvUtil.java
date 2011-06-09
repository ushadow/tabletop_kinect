package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
import static com.googlecode.javacv.cpp.opencv_core.cvFillConvexPoly;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;

import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc.CvConvexityDefect;

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
}
