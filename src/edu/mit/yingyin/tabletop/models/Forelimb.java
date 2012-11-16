package edu.mit.yingyin.tabletop.models;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;

/**
 * Information for a forelimb model.
 * @author yingyin
 *
 */
public class Forelimb {
 
  static public class ValConfiPair<T> {
    public T value;
    public float confidence;
    
    public ValConfiPair(T v, float c) {
      value = v;
      confidence = c;
    }
  }

  private List<ValConfiPair<Point3f>> fingertips;
  
  /**
   * Can be null.
   */
  private Point3f armJoint;
  public List<Point3f> filteredFingertips = new ArrayList<Point3f>();
  
  public Forelimb(List<ValConfiPair<Point3f>> fingertips, Point3f armJoint) {
    if (fingertips == null) {
      fingertips = new ArrayList<ValConfiPair<Point3f>>();
    }
    this.fingertips = fingertips;
    this.armJoint = armJoint;
  }
  
  public Forelimb(Forelimb other) {
    armJoint = new Point3f(other.armJoint);
    fingertips.clear();
    for (ValConfiPair<Point3f> p : other.fingertips) {
      fingertips.add(new ValConfiPair<Point3f>(
          new Point3f(p.value), p.confidence));
    }
  }
  
  public List<Point3f> getFingertips() {
    List<Point3f> res = new ArrayList<Point3f>();
    for (ValConfiPair<Point3f> p : fingertips) {
      if (p.confidence > 0.5)
        res.add(new Point3f(p.value));
    }
    return res;
  }
  
  public int numFingertips() {
    return fingertips.size();
  }
}
