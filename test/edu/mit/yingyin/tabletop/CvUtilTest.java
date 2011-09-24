package edu.mit.yingyin.tabletop;

import static org.junit.Assert.*;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.junit.Test;

import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_16U;

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
}
