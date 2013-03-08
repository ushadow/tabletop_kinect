package edu.mit.yingyin.tabletop.models;

import java.util.List;	
import java.util.logging.Logger;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

/**
 * Hand model.
 * @author yingyin
 *
 */
public class Hand {
  /**
   * Hand finger thickness in mm.
   */
  public static final int FINGER_THICKNESS = 10;
  private static final Logger LOGGER  = Logger.getLogger(Hand.class.getName());

  private final float distAboveSurface;
  private final float width;
  private final Point3f position;
  private final Vector3f v, a;
  /**
   * Euler rotation angles.
   */
  private final Tuple3f rotation;
  private final List<Point3f> pointCloud;
 
  /**
   * 
   * @param distAboveSurface
   * @param width
   * @param position
   * @param v velocity cannot be null.
   * @param a acceleration cannot be null.
   * @param theta
   * @param pointCloud
   */
  public Hand(float distAboveSurface, float width, Point3f position, Vector3f v, 
      Vector3f a, Tuple3f theta, List<Point3f> pointCloud) {
    if (v == null || a == null) {
      LOGGER.severe("velocity and acceleration cannot be null.");
      System.exit(-1);
    }
    this.distAboveSurface = distAboveSurface;
    this.width = width;
    this.position = position;
    this.v = v;
    this.a = a;
    this.rotation = theta;
    this.pointCloud = pointCloud;
  }
  
  public Point3f position() { return new Point3f(position); }
  
  public float distAboveSurface() { return distAboveSurface; }
  
  public float width() { return width; }
  
  public Vector3f velocity() { return (Vector3f) v.clone(); }
  
  public Vector3f acceleration() { return (Vector3f) a.clone(); }
  
  public Tuple3f rotation() { return (Tuple3f) rotation.clone(); }
  
  public List<Point3f> pointCloud() { return pointCloud; }
  
  public String pointCloudToString(List<Point3f> points) {
    StringBuffer sb = new StringBuffer();
    for (Point3f p : points) {
      sb.append(tupleToString(p));
    }
    return sb.toString();
  }
  
  private String tupleToString(Tuple3f t) {
    return String.format("%.3f,%.3f,%.3f,", t.x, t.y, t.z);
  }
}
