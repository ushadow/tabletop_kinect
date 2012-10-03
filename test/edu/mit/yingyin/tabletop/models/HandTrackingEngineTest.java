package edu.mit.yingyin.tabletop.models;

import static org.junit.Assert.fail;

import org.OpenNI.GeneralException;
import org.junit.Test;

public class HandTrackingEngineTest {
  private static final String OPENNI_CONFIG_FILE = "test_data/config.xml";
  private static final String CALIB_FILE = "test_data/calibration.txt";
  private static final int MAX_DEPTH = 1600;
  
  @Test
  public void testStep() {
    try {
      HandTrackingEngine engine = new HandTrackingEngine(OPENNI_CONFIG_FILE, 
          CALIB_FILE, MAX_DEPTH);
      engine.step();
    } catch (GeneralException ge) {
      ge.printStackTrace();
      fail();
    }
  }
}
