package edu.mit.yingyin.tabletop.models;

import static com.googlecode.javacv.cpp.opencv_core.CV_32FC1;

import java.nio.FloatBuffer;
import java.util.logging.Logger;

import static org.junit.Assert.*;

import org.junit.Test;

import com.googlecode.javacv.cpp.opencv_core.CvMat;

public class HandPoseDescriptorTest {
  private static final Logger LOGGER  = Logger.getLogger(
      HandPoseDescriptorTest.class.toString());
  private static final float EPSILON = 0.00001f;

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor() throws IllegalArgumentException {
    CvMat points = CvMat.create(10, 2, CV_32FC1);
    new HandPoseDescriptor(points);
  }
  
  @Test
  public void testRadius() {
    final int numPoints = 10;
    CvMat points = CvMat.create(numPoints, 3, CV_32FC1);
    HandPoseDescriptor hpd = new HandPoseDescriptor(points);
    assertEquals(Math.sqrt(numPoints) * 2, hpd.radius(), EPSILON);
  }
  
  @Test
  public void testDescriptorValue() {
    final int numPoints = 4;
    CvMat points = CvMat.create(numPoints, 3, CV_32FC1);
    FloatBuffer fb = points.getFloatBuffer();
    fb.rewind();
    fb.put(0);
    fb.put(0);
    fb.put(-2);
    
    fb.put(4);
    fb.put(0);
    fb.put(3);
    
    fb.put(-2);
    fb.put(0);
    fb.put(0);
    
    fb.put(0);
    fb.put(-1);
    fb.put(-1);
    
    HandPoseDescriptor hpd = new HandPoseDescriptor(points);
    assertEquals(4, hpd.radius(), EPSILON);
    LOGGER.info(hpd.toString());
    assertEquals(0.25, hpd.descriptorValue(0, 4, 0), EPSILON);
    assertEquals(0.25, hpd.descriptorValue(4, 4, 4), EPSILON);
    assertEquals(0.25, hpd.descriptorValue(2, 7, 2), EPSILON);
    assertEquals(0.25, hpd.descriptorValue(1, 6, 1), EPSILON);
  }
}
