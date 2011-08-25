package edu.mit.yingyin.tabletop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.OpenNI.GeneralException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OpenNIDeviceTest {

  private OpenNIDevice openni;
  
  @Before
  public void setUp() {
    try {
      openni = new OpenNIDevice("test_data/config.xml");
    } catch (GeneralException e) {
      e.printStackTrace();
      fail();
    }
  }
  
  @After
  public void tearDown() {
    if (openni != null)
      openni.release();
  }
  
  @Test
  public void testFrameID() {
    try {
      int frameID1 = openni.depthFrameID();
      openni.waitDepthUpdateAll();
      assertEquals(0, frameID1);
      int frameID2 = openni.depthFrameID();
      assertTrue(frameID2 > frameID1);
    } catch (GeneralException e) {
      e.printStackTrace();
      fail();
    }
  }
  
  @Test
  public void testMaxDeviceDepth() {
    int maxDepth = openni.deviceMaxDepth();
    System.out.println("Device max depth = " + maxDepth);
    assertTrue(maxDepth <= 65535 && maxDepth >= 0);
  }
  
  @Test
  public void testWidthAndHeight() {
    assertEquals(480, openni.imageHeight());
    assertEquals(640, openni.imageWidth());
  }

}
