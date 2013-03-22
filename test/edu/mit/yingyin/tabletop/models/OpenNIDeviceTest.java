package edu.mit.yingyin.tabletop.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.logging.Logger;

import org.OpenNI.GeneralException;
import org.OpenNI.Point3D;
import org.OpenNI.StatusException;
import org.junit.After;
import org.junit.Test;

public class OpenNIDeviceTest {
  private static final Logger LOGGER = Logger.getLogger(
      OpenNIDeviceTest.class.getName());
  private static final int WIDTH = 640;
  private static final int HEIGHT = 480;
  
  private OpenNIDevice openni;
  
  public void setUpFromConfigFile() {
    try {
      openni = new OpenNIDevice("test_data/config.xml");
    } catch (GeneralException ge) {
      ge.printStackTrace();
      System.exit(-1);
    }
  }
  
  public void setUpFromRecording() {
    try {
      openni = new OpenNIDevice("test_data/Captured.oni");
    } catch (GeneralException ge) {
      ge.printStackTrace();
      fail();
    }
  }
  
  @After
  public void tearDown() {
    if (openni != null)
      openni.release();
  }
  
  @Test
  public void testSetUpFromRecording() {
    setUpFromRecording();
    try {
      int frameID = openni.getDepthFrameID();
      assertEquals(0, frameID);
      openni.waitDepthUpdateAll();
      assertEquals(1, openni.getDepthFrameID());
    } catch (GeneralException ge) {
      ge.printStackTrace();
      fail();
    }
  }
  
  @Test
  public void testFrameID() {
    setUpFromConfigFile();
    try {
      int frameID1 = openni.getDepthFrameID();
      assertEquals(0, frameID1);
      openni.waitDepthUpdateAll();
      int frameID2 = openni.getDepthFrameID();
      assertTrue(frameID2 > frameID1);
    } catch (GeneralException e) {
      e.printStackTrace();
      fail();
    }
  }
  
  @Test
  public void testMaxDeviceDepth() {
    setUpFromConfigFile();
    int maxDepth = openni.getDeviceMaxDepth();
    System.out.println("Device max depth = " + maxDepth);
    assertTrue(maxDepth <= 65535 && maxDepth >= 0);
  }
  
  @Test
  public void testWidthAndHeight() {
    setUpFromConfigFile();
    assertEquals(480, openni.getImageHeight());
    assertEquals(640, openni.getImageWidth());
  }
  
  @Test
  public void testDepthDataSize() {
    setUpFromConfigFile(); 
    try {
      openni.waitDepthUpdateAll();
      assertEquals(WIDTH * HEIGHT * 2, openni.getDepthByteBufferSize());
    } catch (StatusException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  @Test
  public void testConvertRealWorldToProjective() {
    setUpFromConfigFile();
    Point3D[] points = new Point3D[2];
    points[0] = new Point3D(1, 2, -3);
    points[1] = new Point3D(1, 2, -30);
    try {
      Point3D[] converted = openni.convertRealWorldToProjective(points);
      LOGGER.info(String.format("(%f, %f, %f)", 
                  converted[0].getX(), converted[0].getY(), 
                  converted[0].getZ()));
      LOGGER.info(String.format("(%f, %f, %f)", 
          converted[1].getX(), converted[1].getY(), 
          converted[1].getZ()));
    } catch (StatusException e) {
      fail();
    }
  }
}
