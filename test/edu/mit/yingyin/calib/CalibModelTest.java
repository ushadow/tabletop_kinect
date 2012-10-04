package edu.mit.yingyin.calib;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2f;

import org.junit.Test;

import edu.mit.yingyin.calib.CalibModel.CalibMethodName;

public class CalibModelTest {

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
}
