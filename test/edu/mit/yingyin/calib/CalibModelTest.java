package edu.mit.yingyin.calib;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2f;

import org.junit.Test;

import edu.mit.yingyin.calib.CalibModel.CalibMethodName;

public class CalibModelTest {
  private static final String CALIB_FILE = "test_data/calibration.txt";
  
  @Test(expected=IllegalArgumentException.class)
  public void testContructorWithException() {
    List<Point2f> objectPoints = new ArrayList<Point2f>();
    List<Point2f> imagePoints = new ArrayList<Point2f>();
    objectPoints.add(new Point2f(0, 0));
    imagePoints.add(new Point2f(0, 0));
    CalibModel calibModel = new CalibModel(objectPoints, imagePoints, 
        CalibMethodName.UNDISTORT);
    calibModel.checkRI();
  }
  
  @Test
  public void testContructor() {
    List<Point2f> objectPoints = new ArrayList<Point2f>();
    List<Point2f> imagePoints = new ArrayList<Point2f>();
    objectPoints.add(new Point2f(0, 0));
    objectPoints.add(new Point2f(0, 1));
    objectPoints.add(new Point2f(1, 0));
    objectPoints.add(new Point2f(1, 1));
    imagePoints.add(new Point2f(0, 0));
    imagePoints.add(new Point2f(0, 1));
    imagePoints.add(new Point2f(1, 0));
    imagePoints.add(new Point2f(0, 1));
    CalibModel calibModel = new CalibModel(objectPoints, imagePoints, 
        CalibMethodName.UNDISTORT);
    calibModel.checkRI();
  }
  
  @Test
  public void testContructorFromFile() {
    CalibModel calibModel = new CalibModel(CALIB_FILE);
    String expectedString = "Homography matrix: [" +
    		"2620.670654296875 -185.47853088378906 1449.0343017578125 " +
    		"-13.530089378356934 2564.4052734375 958.1102294921875 " +
    		"-0.022282885387539864 -0.1364225298166275 1.0 ]";
    
    assertEquals(expectedString, calibModel.toString());
  }
}
