package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_16S;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_32F;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseMat;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseMemStorage;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

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
  /**
   * Features for a forelimb.
   * @author yingyin
   *
   */
  static public class ForelimbFeatures {
    CvMat approxPoly, hull;
    CvRect boundingBox;
    CvSeq convexityDefects;
    Rectangle handRegion;
    
    public void release() {
      if (approxPoly != null)
        approxPoly.release();
      if (hull != null)
        cvReleaseMat(hull);
    }
  }
  /**
   * Integer array of raw depth values from Kinect.
   */
  public int[] depthRawData;
  public IplImage depthImage8U;
  public IplImage depthImage32F;
  public IplImage derivative;
  public IplImage morphedImage;
  public CvMemStorage tempMem;
  public List<ForelimbFeatures> forelimbFeatures = 
      new ArrayList<ForelimbFeatures>();
  public List<Forelimb> foreLimbs = new ArrayList<Forelimb>();
  public int depthFrameID;
  public int width, height;
  public List<Point> labels;
  
  public ProcessPacket(int width, int height) {
    depthRawData = new int[width * height];
    // Creates an unsigned 8-bit integer image.
    depthImage8U = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    depthImage32F = IplImage.create(width, height, IPL_DEPTH_32F, 1);
    derivative = IplImage.create(width, height, IPL_DEPTH_32F, 1);
    morphedImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    // Allocates a default size of 64kB of memory.
    tempMem = cvCreateMemStorage(0);
    this.width = width;
    this.height = height;
  }
  
  /**
   * Releases the memory from all the data structures.
   */
  public void release() {
    clear();
    depthImage8U.release();
    depthImage32F.release();
    derivative.release();
    morphedImage.release();
    cvReleaseMemStorage(tempMem);
  }
  
  /**
   * Clears the data structures.
   */
  public void clear() {
    // Empty the memory storage. This retrieves the memory from sequences.
    cvClearMemStorage(tempMem);
    foreLimbs.clear();
    for (ForelimbFeatures ff : forelimbFeatures)
      ff.release();
    forelimbFeatures.clear();
  }
  
  /**
   * Gets one row of depth raw values.
   * @param row
   * @return
   */
  public int[] getDepthRaw(int row) {
    int[] rowData = new int[width];
    System.arraycopy(depthRawData, width * row, rowData, 0, width);
    return rowData;
  }
  
  public int getDepthRaw(int x, int y) {
    return depthRawData[y * width + x];
  }
}
