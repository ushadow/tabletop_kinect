package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_16U;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_32F;
import static org.junit.Assert.assertEquals;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import org.junit.Test;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class CvUtilTest {

  @Test
  public void testDepthToIplImage() {
    int width = 5;
    int height = 5;
    int[] intArray = new int[width * height];
    for (int i = 0; i < intArray.length; i++)
      intArray[i] = i;
    IplImage image = IplImage.create(width, height, IPL_DEPTH_16U, 1);
    CvUtil.intToIplImage(intArray, image);
    ShortBuffer sb = image.getShortBuffer();
    for (int i = 0; i < intArray.length; i++)
      assertEquals(intArray[i], sb.get(i) & 0xffff);
  }
  
  @Test
  public void testIntToFloatImage() {
    final int width = 10, height = 10, scale = 10;
    int[] raw = new int[width * height];
    Arrays.fill(raw, 5);
    IplImage image = IplImage.create(width, height, IPL_DEPTH_32F, 1);
    CvUtil.intToFloatImage(raw, image, scale);
    FloatBuffer imageBuffer = image.getFloatBuffer();
    imageBuffer.rewind();
    while (imageBuffer.remaining() > 0) {
      System.out.print(imageBuffer.get() + " ");
    }
    System.out.println();
  }
}
