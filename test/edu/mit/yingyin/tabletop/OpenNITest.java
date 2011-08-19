package edu.mit.yingyin.tabletop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.OpenNI.GeneralException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OpenNITest {

  private OpenNI openni;
  
  @Before
  public void setUp() {
    try {
      openni = new OpenNI("test_data/config.xml");
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
      openni.waitDepthAndUpdateAll();
      assertEquals(0, frameID1);
      int frameID2 = openni.depthFrameID();
      assertTrue(frameID2 > frameID1);
    } catch (GeneralException e) {
      e.printStackTrace();
      fail();
    }
  }
  
  @Test
  public void testMaxDevice() {
    int maxDepth = openni.deviceMaxDepth();
    assertTrue(maxDepth <= 65535 && maxDepth >= 0);
  }

}
