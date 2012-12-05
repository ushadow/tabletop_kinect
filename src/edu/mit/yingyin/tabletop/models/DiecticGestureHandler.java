package edu.mit.yingyin.tabletop.models;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;

import edu.mit.yingyin.util.Geometry;

/**
 * Handles deictic gestures. Tasks this object performs include computing 
 * the point on the surface this hand is pointing at.
 * @author yingyin
 *
 */
public class DiecticGestureHandler {
  
  /**
   * @param forelimbs
   * @return a list of intersections of diectic gestures.
   */
  public List<Point3f> update(List<Forelimb> forelimbs) {
    InteractionSurface is = InteractionSurface.instance();
    List<Point3f> res = new ArrayList<Point3f>();
    if (is == null)
      return res;
    
    for (Forelimb fl : forelimbs) {
      Point3f p = computeIntersection(fl, is);
      if (p != null) {
        res.add(p);
      }
    }
    return res;
  }
  
  /**
   * 
   * @param fl
   * @param is
   * @return null if there is no fingertip or arm joint in {@code fl}.
   */
  private Point3f computeIntersection(Forelimb fl, InteractionSurface is) {
    if (fl.numFingertips() <= 0) 
      return null;
    
    // TODO: assumes the first fingertip is the pointing finger. Need to have a
    // more generalized version.
    Point3f fingertip = fl.getFingertipsW().get(0);
    Point3f armJoint = fl.armJointW();
    if (armJoint == null)
      return null;
    
    if (is.center().isNone())
      return null;
    
    return Geometry.linePlaneIntersection(armJoint, fingertip, 
        is.center().value(), is.surfaceNormal());
  }
}
