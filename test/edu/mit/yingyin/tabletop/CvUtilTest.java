package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_32F;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.logging.Logger;

import org.junit.Test;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.mit.yingyin.util.CvUtil;

public class CvUtilTest {
  private static final Logger LOGGER = Logger.getLogger(
      CvUtilTest.class.getName());

  @Test
  public void testDepthToIplImage() {
    int width = 5;
    int height = 5;
    int[] intArray = new int[width * height];
    for (int i = 0; i < intArray.length; i++)
      intArray[i] = i;
    IplImage image = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    CvUtil.intToIplImage8U(intArray, image, 0, 255);
    ByteBuffer bb = image.getByteBuffer();
    int widthStep = image.widthStep();
    for (int h = 0; h < height; h++)
      for (int w = 0; w < width; w++) {
        byte value = bb.get(h * widthStep + w);
        LOGGER.info("byte value = " + value);
        assertEquals(intArray[h * width + w], value);
      }
  }
  
  @Test
  public void testIntToFloatImage() {
    final int width = 10, height = 10;
    final float scale = (float) 0.1;
    int[] raw = new int[width * height];
    Arrays.fill(raw, 5);
    IplImage image = IplImage.create(width, height, IPL_DEPTH_32F, 1);
    CvUtil.intToIplImage32F(raw, image, scale);
    FloatBuffer imageBuffer = image.getFloatBuffer();
    int widthStep = image.widthStep() / 4;
    for (int h = 0; h < height; h++)
      for (int w = 0; w < width; w++)
        assertEquals(0.5, imageBuffer.get(h * widthStep + w), 0.001);
  }
}
