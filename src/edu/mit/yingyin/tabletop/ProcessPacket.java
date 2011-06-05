package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.CV_FILLED;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvClearSeq;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvDrawContours;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import com.googlecode.javacpp.Loader;
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
      cvCopy(packet.depthImage, canvasImage);
      for (CvSeq c = packet.contours; c != null && !c.isNull(); c = c.h_next()){
        cvDrawContours(canvasImage, c, CvScalar.WHITE, CvScalar.WHITE, -1, 
                       CV_FILLED, 8);
        for (int i = 0; i < c.total(); i++) {
          CvPoint p = new CvPoint(cvGetSeqElem(c, i));
          cvCircle(canvasImage, p, 5, CvScalar.WHITE, 1, 8, 0);
        }
      }
      showImage(canvasImage);
    }
    
    public void show(IplImage image) {
      showImage(image);
    }
    
    @Override
    public void dispose() {
      canvasImage.release();
      super.dispose();
      System.out.println("DebugFrame cleaned up.");
    }
  }    

  public int[] depthRawData;
  public IplImage depthImage;
  public CvSeq contours;
  
  public ProcessPacket(int width, int height) {
    depthRawData = new int[width * height];
    depthImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
  }
  
  public void deallocate() {
    depthImage.release();
    if (contours != null && !contours.isNull())
      cvClearSeq(contours);
  }
}
