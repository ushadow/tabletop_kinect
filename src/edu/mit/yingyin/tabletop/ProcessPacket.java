package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvClearSeq;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseMemStorage;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacpp.PointerPointer;
import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_calib3d.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;

public class ProcessPacket {
  static public class DebugFrame extends CanvasFrame {
    private static final long serialVersionUID = 1L;
    private IplImage canvasImage;
    
    public DebugFrame(int width, int height) {
      super("Debug Frame");
      setCanvasSize(width, height);
      canvasImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    }
    
    public void show(ProcessPacket packet) {
      cvCopy(packet.morphedImage, canvasImage);
      for (CvSeq contour = packet.contours; 
           contour != null && !contour.isNull(); contour = contour.h_next()){
//        cvDrawContours(canvasImage, contour, CvScalar.WHITE, CvScalar.WHITE, -1, 
//                       CV_FILLED, 8);
        CvRect rect = cvBoundingRect(contour, 0);
        cvRectangle(canvasImage, new CvPoint(rect.x(), rect.y()), 
            new CvPoint(rect.x() + rect.width(), rect.y() + rect.height()), 
            CvScalar.WHITE, 1, 8, 0);
       
      }
      for (CvSeq hull : packet.hulls) {
        for (int i =0; i < hull.total(); i++) {
          PointerPointer pp = new PointerPointer(cvGetSeqElem(hull, i));
          CvPoint p = new CvPoint(pp.get());
          cvCircle(canvasImage, p, 5, CvScalar.WHITE, 1, 8, 0);
        }
      }
      showImage(canvasImage);
    }
    
    public void show(IplImage image) {
      showImage(image);
    }
    
    public void cleanUp() {
      canvasImage.release();
      super.dispose();
      System.out.println("DebugFrame cleaned up.");
    }
  }    

  public int[] depthRawData;
  public IplImage depthImage;
  public IplImage morphedImage;
  public CvMemStorage tempMem;
  public CvSeq contours;
  public List<CvSeq> hulls = new ArrayList<CvSeq>();
  public List<CvSeq> convexityDefects = new ArrayList<CvSeq>();
  
  public ProcessPacket(int width, int height) {
    depthRawData = new int[width * height];
    depthImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    morphedImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    tempMem = cvCreateMemStorage(0);
  }
  
  public void cleanUp() {
    depthImage.release();
    cvReleaseMemStorage(tempMem);
    if (contours != null && !contours.isNull())
      cvClearSeq(contours);
  }
}
