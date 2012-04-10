package edu.mit.yingyin.tabletop;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.mit.yingyin.tabletop.CalibrationExample.CalibMethod;

public class CalibrationExampleTest {
  private static final String CALIBRATION_FILE = "test_data/calibration.txt";
  @Test
  public void testConstructorFromFile() {
    CalibrationExample ce = new CalibrationExample(CALIBRATION_FILE);
    assertEquals(CalibMethod.UNDISTORT, ce.calibMethod());
  }

}
