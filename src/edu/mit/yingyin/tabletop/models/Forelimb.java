package edu.mit.yingyin.tabletop.models;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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

  private static final Logger logger = Logger.getLogger(
      Forelimb.class.getName());
  
  /**
   * Fingertip locations in the image coordinate.
   */
  private List<ValConfiPair<Point3f>> fingertipsI;
  private List<Point3f> fingertipsW;
  
  /**
   * Arm joint location in the world coordinate. Can be null.
   */
  private Point3f armJointW;
  
  /**
   * Arm joint location in the image coordinate. Can be null;
   */
  private Point3f armJointI;
  public List<Point3f> filteredFingertips = new ArrayList<Point3f>();
  
  /**
   * Creates a forelimb model from the parameters. The model references the 
   * parameters.
   * @param fingertipsI
   * @param armJoints
   */
  public Forelimb(List<ValConfiPair<Point3f>> fingertipsI, 
      List<Point3f> fingertipsW, List<Point3f> armJoints) {
    if (fingertipsI == null) {
      fingertipsI = new ArrayList<ValConfiPair<Point3f>>();
      fingertipsW = new ArrayList<Point3f>();
    } else if (fingertipsI.size() != fingertipsW.size()) {
      logger.severe("Number of fingertips in fingertipsI and fingertipsW are" +
      		"not equal.");
      System.exit(-1);
    }
    this.fingertipsI = fingertipsI;
    this.fingertipsW = fingertipsW;
    
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
  
  public List<Point3f> getFingertipsI() {
    List<Point3f> res = new ArrayList<Point3f>();
    for (ValConfiPair<Point3f> p : fingertipsI) {
      if (p.confidence > 0.5)
        res.add(new Point3f(p.value));
    }
    return res;
  }
  
  public List<Point3f> getFingertipsW() {
    List<Point3f> res = new ArrayList<Point3f>();
    for (Point3f p : fingertipsW)
      res.add(p);
    return res;
  }
  
  public int numFingertips() {
    return fingertipsI.size();
  }
  
  /**
   * @return the 3D position of the arm joint in the image coordinate. Can be 
   *    null.
   */
  public Point3f armJointI() { 
    if (armJointI == null)
      return null;
    return new Point3f(armJointI); 
  }
  
  /**
   * @return the 3D position of the arm joint in the world coordinate. Can be
   *    null.
   */
  public Point3f armJointW() {
    if (armJointW == null)
      return null;
    return new Point3f(armJointW);
  }
}
