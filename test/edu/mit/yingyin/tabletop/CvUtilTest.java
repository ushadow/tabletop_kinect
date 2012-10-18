package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_16U;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_32F;
import static org.junit.Assert.assertEquals;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import org.junit.Test;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.mit.yingyin.util.CvUtil;

public class CvUtilTest {

  @Test
  public void testDepthToIplImage() {
    int width = 5;
    int height = 5;
    int[] intArray = new int[width * height];
    for (int i = 0; i < intArray.length; i++)
      intArray[i] = i;
    IplImage image = IplImage.create(width, height, IPL_DEPTH_16U, 1);
    CvUtil.intToIntIplImage(intArray, image, 1);
    ShortBuffer sb = image.getShortBuffer();
    int widthStep = image.widthStep() / 2;
    for (int h = 0; h < height; h++)
      for (int w = 0; w < width; w++)
        assertEquals(intArray[h * width + w], 
                     sb.get(h * widthStep + w) & 0xffff);
  }
  
  @Test
  public void testIntToFloatImage() {
    final int width = 10, height = 10;
    final float scale = (float) 0.1;
    int[] raw = new int[width * height];
    Arrays.fill(raw, 5);
    IplImage image = IplImage.create(width, height, IPL_DEPTH_32F, 1);
    CvUtil.intToFloatIplImage(raw, image, scale);
    FloatBuffer imageBuffer = image.getFloatBuffer();
    int widthStep = image.widthStep() / 4;
    for (int h = 0; h < height; h++)
      for (int w = 0; w < width; w++)
        assertEquals(0.5, imageBuffer.get(h * widthStep + w), 0.001);
  }
}
