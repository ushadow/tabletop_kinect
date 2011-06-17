package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseMemStorage;

import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import rywang.viewer.FPSCounter;

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

/**
 * ProcessPacket contains the data related to the current frame.
 * @author yingyin
 *
 */
public class ProcessPacket {
  static public class ForelimbFeatures {
    static public class ValConfiPair<T> {
      public T value;
      public float confidence;
      
      public ValConfiPair(T v, float c) {
        value = v;
        confidence = c;
      }
    }
    
    public List<ValConfiPair<Point>> fingertips = 
        new ArrayList<ValConfiPair<Point>>();
    public Point center;
    
    public ForelimbFeatures() {}
    
    public ForelimbFeatures(ForelimbFeatures other) {
      center = new Point(other.center);
      for (ValConfiPair<Point> p: other.fingertips) {
        fingertips.add(new ValConfiPair<Point>(
            new Point(p.value), p.confidence));
      }
    }
  }
  
  public int[] depthRawData;
  public IplImage depthImage;
  public IplImage morphedImage;
  public CvMemStorage tempMem;
  public List<CvMat> approxPolys = new ArrayList<CvMat>();
  public List<CvMat> hulls = new ArrayList<CvMat>();
  public List<CvRect> boundingBoxes = new ArrayList<CvRect>();
  public List<CvSeq> convexityDefects = new ArrayList<CvSeq>();
  public List<ForelimbFeatures> foreLimbsFeatures = 
      new ArrayList<ForelimbFeatures>();
  
  public ProcessPacket(int width, int height) {
    depthRawData = new int[width * height];
    depthImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    morphedImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    tempMem = cvCreateMemStorage(0);
  }
  
  public void cleanUp() {
    clear();
    depthImage.release();
    morphedImage.release();
    cvReleaseMemStorage(tempMem);
  }
  
  public void clear() {
    for (CvMat m : approxPolys) {
      if (m != null && !m.isNull())
        m.release();
    }
    approxPolys.clear();
    for (CvMat m : hulls) 
        cvReleaseMat(m);
    hulls.clear();
    convexityDefects.clear();
    foreLimbsFeatures.clear();
    boundingBoxes.clear();
    cvClearMemStorage(tempMem);
  }
}
