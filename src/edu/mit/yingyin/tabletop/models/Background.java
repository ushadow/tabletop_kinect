package edu.mit.yingyin.tabletop.models;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_32F;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvAbsDiff;
import static com.googlecode.javacv.cpp.opencv_core.cvAdd;
import static com.googlecode.javacv.cpp.opencv_core.cvAddS;
import static com.googlecode.javacv.cpp.opencv_core.cvAvg;
import static com.googlecode.javacv.cpp.opencv_core.cvConvertScale;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvInRange;
import static com.googlecode.javacv.cpp.opencv_core.cvRealScalar;
import static com.googlecode.javacv.cpp.opencv_core.cvSub;
import static com.googlecode.javacv.cpp.opencv_core.cvSubRS;
import static com.googlecode.javacv.cpp.opencv_core.cvZero;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvAcc;
import static com.googlecode.javacv.cpp.opencv_core.cvMul;

import java.nio.FloatBuffer;
import java.util.logging.Logger;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.mit.yingyin.util.CvUtil;

/**
 * Keeps track of statistics of the background model.
 * 
 * @author yingyin
 * 
 */
public class Background {

  private static Logger logger = Logger.getLogger(Background.class.getName());

  private static final float MIN_DIFF = (float) 1;
  
  private static final int BITS_PER_BYTE = 8; 

  /**
   * Float, 1-channel images. All the depth values are scaled between 0 and 1
   * according to <code>maxDepth
   * </code>
   */
  private IplImage scratchI, scratchI2, avgFI, prevFI;

  /**
   * Absolute difference between the current frame and the previous frame.
   */
  private IplImage diffFI, hiFI, lowFI, scaleFI;
  private IplImage mask;
  private boolean first = true;

  /**
   * Counts the number of images learned for averaging later.
   */
  private float count = (float) 0.00001; // Protects against divide by zero.

  /**
   * Average absolute difference before adjustment.
   */
  private float avgDiff;

  private int width, height;
  private boolean initialized = false;

  /**
   * Initializes the background model.
   * 
   * @param width width of the background image.
   * @param height height of the background image.
   */
  public Background(int width, int height) {
    this.width = width;
    this.height = height;

    scratchI = IplImage.create(width, height, IPL_DEPTH_32F, 1);
    scratchI2 = IplImage.create(width, height, IPL_DEPTH_32F, 1);
    avgFI = IplImage.create(width, height, IPL_DEPTH_32F, 1);
    prevFI = IplImage.create(width, height, IPL_DEPTH_32F, 1);
    diffFI = IplImage.create(width, height, IPL_DEPTH_32F, 1);
    hiFI = IplImage.create(width, height, IPL_DEPTH_32F, 1);
    lowFI = IplImage.create(width, height, IPL_DEPTH_32F, 1);
    scaleFI = IplImage.create(width, height, IPL_DEPTH_32F, 1);
    mask = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    cvZero(avgFI);
    cvZero(prevFI);
    cvZero(diffFI);
  }

  public int width() {
    return width;
  }

  public int height() {
    return height;
  }

  /**
   * Learns the background statistics for one more frame.
   * 
   * @param depthRawData int array of depth values.
   */
  public void accumulateBackground(int[] depthRawData) {
    depthToImage(depthRawData, scratchI);
    if (!first) {
      cvAcc(scratchI, avgFI, null);
      count += 1;
      cvConvertScale(avgFI, scratchI2, 1.0 / count, 0); 
      cvAbsDiff(scratchI, scratchI2, scratchI2);
      cvAcc(scratchI2, diffFI, null);
    }
    first = false;
    cvCopy(scratchI, prevFI);
  }

  /**
   * Creates a statistical model of the background.
   * 
   * @param lowScale scale used to multiply with the average absolute difference
   *          to create the low threshold.
   * @param highScale scale used to multiply with the average absolute
   *          difference to create the high threshold.
   */
  public void createModelsFromStats(float lowScale, float highScale) {
    cvConvertScale(avgFI, avgFI, 1.0 / count, 0);
    cvConvertScale(diffFI, diffFI, 1.0 / count, 0);
    avgDiff = (float) (cvAvg(diffFI, null).val(0));
    cvAddS(diffFI, cvRealScalar(MIN_DIFF), diffFI, null);
    createScale(lowScale, highScale);
    setHighThreshold();
    setLowThreshold();
    initialized = true;
  }

  public boolean isInitialized() {
    return initialized;
  }

  /**
   * Segments an input input into foreground and background.
   * 
   * @param depthRawData input depth image.
   * @param mask a 0 or 255 mask image where 255 means foreground pixel.
   */
  public void backgroundDiff(int[] depthRawData, IplImage mask) {
    // To float.
    depthToImage(depthRawData, scratchI);
    // lowFI is inclusive lower bound, and hiFI is exclusive higher bound.
    cvInRange(scratchI, lowFI, hiFI, mask);
    // Inverts the results.
    cvSubRS(mask, cvRealScalar(255), mask, null);
  }

  /**
   * Releases memory.
   */
  public void release() {
    scratchI.release();
    scratchI2.release();
    avgFI.release();
    diffFI.release();
    prevFI.release();
    hiFI.release();
    lowFI.release();
    mask.release();
    logger.info("Background relesed.");
  }

  /**
   * @return statistics of the background as a string.
   */
  public String stats() {
    StringBuffer sb = new StringBuffer();
    sb.append(String.format("Average background depth: %f\n", avgDepth()));
    sb.append(String.format("Average background absolute difference: %f\n",
        avgDiff()));
    return sb.toString();
  }

  /**
   * @return a buffer of average scaled depth of the background.
   */
  public FloatBuffer avgBuffer() {
    return avgFI.getFloatBuffer();
  }

  /**
   * @return number of values per row in the average depth buffer.
   */
  public int avgBufferWidthStep() {
    return avgFI.widthStep() * BITS_PER_BYTE / avgFI.depth();
  }

  /**
   * @return a buffer of average scaled absolute difference of the background.
   */
  public FloatBuffer diffBuffer() {
    return diffFI.getFloatBuffer();
  }

  /**
   * @return number of values per row in the average diff buffer.
   */
  public int diffBufferWidthStep() {
    return diffFI.widthStep() * 8 / diffFI.depth();
  }

  /**
   * @return the average background depth value.
   */
  public float avgDepth() {
    return (float) (cvAvg(avgFI, null).val(0));
  }

  /**
   * @return the average absolute difference before adjustment.
   */
  public float avgDiff() {
    return avgDiff;
  }

  /**
   * @return the string representation of the background model.
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Average Frame:\n");
    FloatBuffer fb = avgFI.getFloatBuffer();
    fb.rewind();
    while (fb.remaining() > 0)
      sb.append(fb.get() + " ");
    sb.append("\nDiff Frame:\n");
    fb = diffFI.getFloatBuffer();
    fb.rewind();
    while (fb.remaining() > 0)
      sb.append(fb.get() + " ");
    sb.append("\nHigher Bound:\n");
    fb = hiFI.getFloatBuffer();
    fb.remaining();
    while (fb.remaining() > 0)
      sb.append(fb.get() + " ");
    return sb.toString();
  }

  /**
   * Scales integer depth values into a floating-point 1-channel image with
   * values between 0 and 1.
   * 
   * @param depthRawData int array of depth values.
   * @param image floating-point 1-channel image with the same number of pixels
   *          as the depth array.
   */
  private void depthToImage(int[] depthRawData, IplImage image) {
    CvUtil.intToFloatImage(depthRawData, image, 1);
  }

  /**
   * Sets the high threshold of the background model for each pixel. Any value
   * above a that threshold for a particular pixel is considered foreground.
   * 
   * High threshold = average value + average absolute difference * scale
   * 
   * @param scale the factor that multiplies the average frame-to-frame absolute
   *          difference.
   */
  private void setHighThreshold() {
    cvMul(diffFI, scaleFI, scratchI, 1);
    cvAdd(scratchI, avgFI, hiFI, null);
  }

  private void setLowThreshold() {
    cvMul(diffFI, scaleFI, scratchI, 1);
    cvSub(avgFI, scratchI, lowFI, null);
  }
  
  private void createScale(float lowScale, float highScale) {
    FloatBuffer fb = scaleFI.getFloatBuffer();
    int widthStep = scaleFI.widthStep() * BITS_PER_BYTE / scaleFI.depth();
    float maxDist = dis2FromCenter(width, height);
    float range = highScale - lowScale;
    for (int h = 0; h < height; h++)
      for (int w = 0; w < width; w++) {
        float scale = dis2FromCenter(w, h) * range / maxDist + lowScale;
        fb.put(h * widthStep + w, scale);
    }
  }
  
  private float dis2FromCenter(int x, int y) {
    float x1 = x - (float) width / 2;
    float y1 = y - (float) height / 2;
    return x1 * x1 + y1 * y1;
  }
}
