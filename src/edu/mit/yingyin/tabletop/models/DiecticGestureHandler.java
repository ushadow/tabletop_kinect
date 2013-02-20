package edu.mit.yingyin.tabletop.models;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;

import org.OpenNI.Point3D;

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
  public List<Point3D> update(List<Forelimb> forelimbs) {
    InteractionSurface is = InteractionSurface.instance();
    List<Point3D> res = new ArrayList<Point3D>();
    if (is == null)
      return res;
    
    for (Forelimb fl : forelimbs) {
      Point3D p = computeIntersection(fl, is);
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
  private Point3D computeIntersection(Forelimb fl, InteractionSurface is) {
    if (fl.numFingertips() <= 0) 
      return null;
    
    Point3f fingertip = new Point3f();
    for (Point3f p : fl.getFingertipsW())
      fingertip.add(p);
    fingertip.scale((float) 1 / fl.getFingertipsW().size());
    
    Point3f armJoint = fl.armJointW();
    if (armJoint == null)
      return null;
    
    if (is.center().isNone())
      return null;
    
    Point3f p = Geometry.linePlaneIntersection(armJoint, fingertip, 
        is.center().value(), is.surfaceNormal());
    return new Point3D(p.x, p.y, p.z);
  }
}
