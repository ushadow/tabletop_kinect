package edu.mit.yingyin.tabletop.models;

import static org.junit.Assert.fail;

import org.OpenNI.GeneralException;
import org.junit.Test;

public class HandTrackingEngineTest {
  private static final String OPENNI_CONFIG_FILE = "test_data/config.xml";
  private static final String CALIB_FILE = "test_data/calibration.txt";
  
  @Test
  public void testStep() {
    try {
      HandTrackingEngine engine = new HandTrackingEngine(null, 
          OPENNI_CONFIG_FILE, CALIB_FILE);
      engine.step();
    } catch (GeneralException ge) {
      ge.printStackTrace();
      fail();
    }
  }
}
