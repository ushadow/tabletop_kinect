package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseMat;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseMemStorage;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

/**
 * ProcessPacket contains the data related to the current frame.
 * @author yingyin
 *
 */
public class ProcessPacket {
  static public class ForelimbModel {
    static public class ValConfiPair<T> {
      public T value;
      public float confidence;
      
      public ValConfiPair(T v, float c) {
        value = v;
        confidence = c;
      }
    }
    
    public List<ValConfiPair<Point3f>> fingertips = 
        new ArrayList<ValConfiPair<Point3f>>();
    public Point center;
    
    public ForelimbModel() {}
    
    public ForelimbModel(ForelimbModel other) {
      center = new Point(other.center);
      for (ValConfiPair<Point3f> p: other.fingertips) {
        fingertips.add(new ValConfiPair<Point3f>(
            new Point3f(p.value), p.confidence));
      }
    }
  }
  
  /**
   * Integer array of raw depth values from Kinect.
   */
  public int[] depthRawData;
  public IplImage depthImage;
  public IplImage morphedImage;
  public CvMemStorage tempMem;
  public List<CvMat> approxPolys = new ArrayList<CvMat>();
  public List<CvMat> hulls = new ArrayList<CvMat>();
  public List<CvRect> boundingBoxes = new ArrayList<CvRect>();
  public List<CvSeq> convexityDefects = new ArrayList<CvSeq>();
  public List<ForelimbModel> foreLimbsFeatures = 
      new ArrayList<ForelimbModel>();
  public int depthFrameID;
  
  public ProcessPacket(int width, int height) {
    depthRawData = new int[width * height];
    // Creates an unsigned 8-bit integer image.
    depthImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    morphedImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    // Allocates a default size of 64kB of memory.
    tempMem = cvCreateMemStorage(0);
  }
  
  /**
   * Releases the memory from all the data structures.
   */
  public void release() {
    clear();
    depthImage.release();
    morphedImage.release();
    cvReleaseMemStorage(tempMem);
  }
  
  /**
   * Clears the data structures.
   */
  public void clear() {
    for (CvMat m : approxPolys) {
      if (m != null && !m.isNull())
        m.release();
    }
    approxPolys.clear();
    
    for (CvMat m : hulls) 
        cvReleaseMat(m);
    hulls.clear();
    
    // Empty the memory storage.
    cvClearMemStorage(tempMem);
    convexityDefects.clear();
    foreLimbsFeatures.clear();
    boundingBoxes.clear();
  }
}
