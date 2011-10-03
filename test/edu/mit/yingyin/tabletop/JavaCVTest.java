package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static org.junit.Assert.*;

import org.junit.Test;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class JavaCVTest {

  @Test
  public void testIplImage() {
    IplImage image = IplImage.create(10, 10, IPL_DEPTH_8U, 1);
    assertEquals(10, image.height());
    assertEquals(10, image.width());
  }

}
