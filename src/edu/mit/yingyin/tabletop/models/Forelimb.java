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

  /**
   * Fingertip locations in the image coordinate.
   */
  private List<ValConfiPair<Point3f>> fingertipsI;
  
  /**
   * Arm joint location in the world coordinate. Can be null.
   */
  private Point3f armJointW;
  
  private Point3f armJointI;
  public List<Point3f> filteredFingertips = new ArrayList<Point3f>();
  
  public Forelimb(List<ValConfiPair<Point3f>> fingertipsI, List<Point3f> armJoints) {
    if (fingertipsI == null) {
      fingertipsI = new ArrayList<ValConfiPair<Point3f>>();
    }
    this.fingertipsI = fingertipsI;
    
    if (armJoints != null && armJoints.size() >= 2) {
      this.armJointI = armJoints.get(0);
      this.armJointW = armJoints.get(1);
    }
  }
  
  public Forelimb(Forelimb other) {
    armJointW = new Point3f(other.armJointW);
    fingertipsI.clear();
    for (ValConfiPair<Point3f> p : other.fingertipsI) {
      fingertipsI.add(new ValConfiPair<Point3f>(
          new Point3f(p.value), p.confidence));
    }
  }
  
  public List<Point3f> getFingertips() {
    List<Point3f> res = new ArrayList<Point3f>();
    for (ValConfiPair<Point3f> p : fingertipsI) {
      if (p.confidence > 0.5)
        res.add(new Point3f(p.value));
    }
    return res;
  }
  
  public int numFingertips() {
    return fingertipsI.size();
  }
  
  /**
   * @return the 3D position of the arm joint in the image coordinate. Can be null.
   */
  public Point3f armJointI() { 
    if (armJointI == null)
      return null;
    return new Point3f(armJointI); 
  }
  
  public Point3f armJointW() {
    if (armJointW == null)
      return null;
    return new Point3f(armJointW);
  }
}
