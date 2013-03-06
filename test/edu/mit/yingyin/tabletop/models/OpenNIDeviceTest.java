package edu.mit.yingyin.tabletop.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.OpenNI.GeneralException;
import org.OpenNI.StatusException;
import org.junit.After;
import org.junit.Test;

public class OpenNIDeviceTest {
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
}
