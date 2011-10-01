package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_32F;
import static com.googlecode.javacv.cpp.opencv_core.cvAbsDiff;
import static com.googlecode.javacv.cpp.opencv_core.cvZero;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvConvertScale;
import static com.googlecode.javacv.cpp.opencv_core.cvScalar;
import static com.googlecode.javacv.cpp.opencv_core.cvAddS;
import static com.googlecode.javacv.cpp.opencv_core.cvAdd;
import static com.googlecode.javacv.cpp.opencv_core.cvSub;
import static com.googlecode.javacv.cpp.opencv_core.cvInRange;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvAcc;

import java.nio.FloatBuffer;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

/**
 * Keeps track of statistics of the background model.
 * @author yingyin
 *
 */
public class Background {
  /**
   * Maximum depth of the background. This is considered to be the maximum value
   * getting from the depth sensor. 
   */
  public static final int MAX_DEPTH = 1600; 
  /**
   * Float, 1-channel images.
   */
  private IplImage scratchI, scratchI2, avgFI, prevFI, diffFI, hiFI, lowFI;
  private boolean first = true;
  /**
   * Counts the number of images learned for averaging later.
   */
  private float count = (float)0.00001; // Protects against divide by zero.
  
  /**
   * Initializes the background model.
   * @param width
   * @param height
   */
  public Background(int width, int height) {
    scratchI = IplImage.create(width, height, IPL_DEPTH_32F, 1);
    scratchI2 = IplImage.create(width, height, IPL_DEPTH_32F, 1);
    avgFI = IplImage.create(width, height, IPL_DEPTH_32F, 1);
    prevFI = IplImage.create(width, height, IPL_DEPTH_32F, 1);
    diffFI = IplImage.create(width, height, IPL_DEPTH_32F, 1);
    hiFI = IplImage.create(width, height, IPL_DEPTH_32F, 1);
    lowFI = IplImage.create(width, height, IPL_DEPTH_32F, 1);
    cvZero(avgFI);
    cvZero(prevFI);
    cvZero(diffFI);
  }
  
  /**
   * Learns the background statistics for one more frame.
   * @param depthRawData int array of depth values.
   */
  public void accumulateBackground(int[] depthRawData) {
    scale(depthRawData, scratchI);
    if (!first) {
      cvAcc(scratchI, avgFI, null);
      cvAbsDiff(scratchI, prevFI, scratchI2);
      cvAcc(scratchI2, diffFI, null);
      count += 1;
      first = false;
    }
    cvCopy(scratchI, prevFI);
  }
  
  /**
   * Creates a statistical model of the background.
   */
  public void createModelsFromStats(float lowScale, float highScale) {
    cvConvertScale(avgFI, avgFI, 1.0/count, 0);
    cvConvertScale(diffFI, diffFI, 1.0/count, 0);
    // Makes sure diff is always something.
    cvAddS(diffFI, cvScalar(1.0, 0, 0, 0), diffFI, null);
    setHighThreshold(highScale);
    setLowThreshold(lowScale);
  }
  
  /**
   * Segments an input input into foreground and background.
   * @param depthRawData input depth image.
   * @param mask a 0 or 255 mask image where 255 means foreground pixel.
   */
  public void backgroundDiff(int[] depthRawData, IplImage mask) {
    // To float.
    scale(depthRawData, scratchI);
    cvInRange(scratchI, lowFI, hiFI, mask);
  }
  
  public void release() {
    scratchI.release();
    scratchI2.release();
    avgFI.release();
    diffFI.release();
    prevFI.release();
    hiFI.release();
    lowFI.release();
  }

  /**
   * Scales integer depth values into a floating-point 1-channel image with
   * values between 0 and 1.
   * @param depthRawData int array of depth values.
   * @param image floating-point 1-channel image with the same number of pixels
   *    as the depth array.
   */
  private void scale(int[] depthRawData, IplImage image) {
    FloatBuffer fb = image.getFloatBuffer();
    fb.rewind();
    // Converts to float.
    while(fb.remaining() > 0) {
      int pos = fb.position();
      float depth = (float)depthRawData[pos] / MAX_DEPTH;
      fb.put(depth > 1 ? depth : 1);
    }
  }
  
  /**
   * Sets the high threshold of the background model for each pixel. Any value 
   * above a that threshold for a particular pixel is considered foreground.
   * 
   * High threshold = average value + average absolute difference * scale
   * 
   * @param scale the factor that multiplies the average frame-to-frame absoute
   *    difference.
   */
  private void setHighThreshold(float scale) {
    cvConvertScale(diffFI, scratchI, scale, 0);
    cvAdd(scratchI, avgFI, hiFI, null);
  }
  
  private void setLowThreshold(float scale) {
    cvConvertScale(diffFI, scratchI, scale, 0);
    cvSub(avgFI, scratchI, lowFI, null);
  }
  
}
