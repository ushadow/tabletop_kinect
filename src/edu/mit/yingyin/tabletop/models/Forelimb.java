package edu.mit.yingyin.tabletop.models;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * An immutable model for forelimb.
 * @author yingyin
 *
 */
public class Forelimb {
 
  private static final Logger LOGGER = Logger.getLogger(
      Forelimb.class.getName());
  
  /**
   * Fingertip locations in the image coordinate.
   */
  private List<Point3f> fingertipsI, fingertipsW;
  
  /**
   * Arm joint location in the world coordinate. Can be null.
   */
  private Point3f armJointW;
  
  /**
   * Arm joint location in the image coordinate. Can be null;
   */
  private Point3f armJointI;
  private Hand hand;
  
  /**
   * Creates a forelimb model from the parameters. The model references the 
   * parameters.
   * @param fingertipsI can be null;
   * @param fingertipsW can be null;
   * @param armJoints list of 2 arm joints. The first on is in the image 
   *    coordinates and the second one is in the world coordinates.
   */
  public Forelimb(List<Point3f> fingertipsI, List<Point3f> fingertipsW, 
                  List<Point3f> armJoints, Hand hand) {
    if (fingertipsI == null) {
      fingertipsI = new ArrayList<Point3f>();
      fingertipsW = new ArrayList<Point3f>();
    } else if (fingertipsI.size() != fingertipsW.size()) {
      LOGGER.severe("Number of fingertips in fingertipsI and fingertipsW are" +
      		"not equal.");
      System.exit(-1);
    }
    this.fingertipsI = fingertipsI;
    this.fingertipsW = fingertipsW;
    
    if (armJoints != null && armJoints.size() >= 2) {
      this.armJointI = armJoints.get(0);
      this.armJointW = armJoints.get(1);
    }
    this.hand = hand;
  }
  
  /**
   * 
   * @return a list of fingertips in image coordinates. Empty if there are no
   *    fingertips.
   */
  public List<Point3f> fingertipsI() {
    List<Point3f> res = new ArrayList<Point3f>();
    for (Point3f p : fingertipsI) {
      res.add(new Point3f(p));
    }
    return res;
  }
  
  /**
   * 
   * @return a list of fingertips in world coordinates. Empty if there are not 
   *    fingertips.
   */
  public List<Point3f> fingertipsW() {
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
  
  /**
   * @return the reference of the hand.
   */
  public Hand hand() { return hand; }
}
