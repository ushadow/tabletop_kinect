package edu.mit.yingyin.tabletop.models;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_32F;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseMat;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseMemStorage;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;

import org.OpenNI.GeneralException;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.mit.yingyin.image.ImageConvertUtils;
import edu.mit.yingyin.tabletop.models.Forelimb.ValConfiPair;


/**
 * ProcessPacket contains the data related to the current frame.
 * @author yingyin
 *
 */
public class ProcessPacket {
  /**
   * Image features for a forelimb.
   * @author yingyin
   *
   */
  static public class ForelimbFeatures {
    public CvMat approxPoly, hull;
    public CvRect boundingBox;
    public CvSeq convexityDefects;
    public CvRect handRegion, armJointRegion;
    public List<ValConfiPair<Point3f>> fingertips = 
        new ArrayList<ValConfiPair<Point3f>>();
    
    public void release() {
      if (approxPoly != null)
        approxPoly.release();
      if (hull != null)
        cvReleaseMat(hull);
    }
  }
  
  /**
   * Integer array of raw depth values in mm from Kinect.
   */
  public int[] depthRawData;
  public IplImage depthImage8U, depthImage32F, derivative, morphedImage, 
         depthImageBlur32F;
  public IplImage foregroundMask;
  public CvMemStorage tempMem;
  public List<ForelimbFeatures> forelimbFeatures = 
      new ArrayList<ForelimbFeatures>();
  public List<Forelimb> forelimbs = new ArrayList<Forelimb>();
  public int depthFrameID;
  public int width, height;
  
  private BufferedImage rgbImage;
  private OpenNIDevice openni;
  
  /**
   * Creates a new <code>ProcessPacket</code> and allocates memory.
   * @param width
   * @param height
   * @param engine the <code>HandTrackingEngine</code> that updates this <code>
   *    ProcessPacket</code>.
   */
  public ProcessPacket(int width, int height, OpenNIDevice openni) {
    
    depthRawData = new int[width * height];
    // Creates an unsigned 8-bit integer image.
    depthImage8U = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    depthImage32F = IplImage.create(width, height, IPL_DEPTH_32F, 1);
    derivative = IplImage.create(width, height, IPL_DEPTH_32F, 1);
    depthImageBlur32F = IplImage.create(width, height, IPL_DEPTH_32F, 1); 
    morphedImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    foregroundMask = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    // Allocates a default size of 64kB of memory.
    tempMem = cvCreateMemStorage(0);
    this.width = width;
    this.height = height;
    this.openni = openni;
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
    foregroundMask.release();
    depthImageBlur32F.release();
    cvReleaseMemStorage(tempMem);
  }
  
  /**
   * Clears the data structures.
   */
  public void clear() {
    // Empty the memory storage. This retrieves the memory from sequences.
    cvClearMemStorage(tempMem);
    forelimbs.clear();
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
  
  /**
   * Returns the raw depth value at (x, y).
   * @param x the x coordinate.
   * @param y the y coordinate.
   * @return the raw depth value.
   */
  public int getDepthRaw(int x, int y) {
    if (x >= width)
      x = width;
    if (y >= height)
      y = height;
    return depthRawData[y * width + x];
  }
  
  /**
   * Returns the current updated RGB image.
   * @throws GeneralException
   */
  public  BufferedImage rgbImage() throws GeneralException {
    if (rgbImage == null)
      rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
    ImageConvertUtils.byteBuffer2BufferedImage(openni.getImageBuffer(), 
                                               rgbImage);
    return rgbImage;
  }
}
