package edu.mit.yingyin.tabletop;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.mit.yingyin.tabletop.CalibrationExample.CalibMethodName;

public class CalibrationExampleTest {
  private static final String CALIBRATION_FILE = "test_data/calibration.txt";
  @Test
  public void testConstructorFromFile() {
    CalibrationExample ce = new CalibrationExample(CALIBRATION_FILE);
    assertEquals(CalibMethodName.UNDISTORT, ce.calibMethod());
    String expected = "Homography matrix: [2620.670654296875 " +
    		"-185.47853088378906 1449.0343017578125 -13.530089378356934 " +
    		"2564.4052734375 958.1102294921875 -0.022282885387539864 " +
    		"-0.1364225298166275 1.0 ]";
    assertEquals(expected, ce.toString());
  }

}
