package edu.mit.yingyin.util;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Tuple2f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

public class Geometry {
  private static final double EPS = 1e-8;
  private static final Logger logger = Logger.getLogger(
      Geometry.class.getName());

  /**
   * Calculates the Euler angles from the quaternion, positive direction of the
   * rotation follows the right hand rule: if the thumb of the right hand is
   * pointed in the direction of the axis, the positive direction of rotation is
   * given by the curl of the fingers.
   * http://www.euclideanspace.com/maths/geometry
   * /rotations/conversions/quaternionToEuler/index.htm
   * 
   * @param q1 quaternion has to be normalized
   * @return Tuple3f.x = roll (rotation about x axis) Tuple3f.y = yaw (rotation
   *         about y axis) Tuple3f.z = pitch (rotation about z axis)
   */
  static public Tuple3f getEulerAngles(Quat4f q1) {
    /** assumes q1 is a normalized quaternion */
    Tuple3f tuple3f = new Vector3f();
    float heading, attitude, bank;
    double test = q1.x * q1.y + q1.z * q1.w;
    if (test > 0.499) { // singularity at north pole
      heading = (float) (2 * Math.atan2(q1.x, q1.w));
      attitude = (float) Math.PI / 2;
      bank = 0;
    } else if (test < -0.499) { // singularity at south pole
      heading = (float) (-2 * Math.atan2(q1.x, q1.w));
      attitude = -(float) Math.PI / 2;
      bank = 0;
    } else {
      double sqx = q1.x * q1.x;
      double sqy = q1.y * q1.y;
      double sqz = q1.z * q1.z;
      heading = (float) Math.atan2(2 * q1.y * q1.w - 2 * q1.x * q1.z, 1 - 2
          * sqy - 2 * sqz);
      attitude = (float) Math.asin(2 * test);
      bank = (float) Math.atan2(2 * q1.x * q1.w - 2 * q1.y * q1.z, 1 - 2 * sqx
          - 2 * sqz);
    }
    tuple3f.x = bank;
    tuple3f.y = heading;
    tuple3f.z = attitude;
    return tuple3f;
  }

  /**
   * Calculates angle C of triangle ABC.
   * 
   * @param A Point A of the triangle.
   * @param B Point B of the triangle.
   * @param C Point C of the triangle.
   * @return angle of C.
   */
  static public double getAngleC(Point A, Point B, Point C) {
    Point vCA = new Point(A.x - C.x, A.y - C.y);
    double distSqCA = vCA.distanceSq(0, 0);
    if (distSqCA == 0)
      return 0;
    Point vCB = new Point(B.x - C.x, B.y - C.y);
    double distSqCB = vCB.distanceSq(0, 0);
    if (distSqCB == 0)
      return 0;
    // Cosine rule: c^2 = a^2 + b^2 - 2abcosC
    return Math.acos((vCA.x * vCB.x + vCA.y * vCB.y)
        / Math.sqrt(distSqCA * distSqCB));
  }

  /**
   * Returns a pair of points that are the closest pair of points on two lines.
   * The implementation is based on
   * http://softsurfer.com/Archive/algorithm_0106/algorithm_0106.htm#dist3D_Line_to_Line()
   * 
   * @param p1 a point on line 1.
   * @param u direction vector of line 1.
   * @param p2 a point on line 2.
   * @param v direction vector of line 2.
   * @return a list of two points that are the closet pair of points. The first
   *         point is on line 1 and the second point is on line 2.
   */
  static public List<Point3f> closestPointsLineToLine(Point3f p1, Vector3f u,
      Point3f p2, Vector3f v) {
    Vector3f w = new Vector3f();
    w.sub(p1, p2);
    // a * sc - b * tc = -d
    // b * sc - c * tc = -e
    float a = u.dot(u); // always >= 0
    float b = u.dot(v);
    float c = v.dot(v); // always >= 0
    float d = u.dot(w);
    float e = v.dot(w);
    // |u|^2|v|^2 - (|u||v|cos q)^2 = (|u||v|sin q)^2 >= 0
    float D = a * c - b * b; 
    float sc, tc;

    // Compute the line parameters of the two closest points
    if (D < EPS) { // the lines are almost parallel
      sc = 0;
      tc = b > c ? d / b : e / c; // use the largest denominator
    } else {
      sc = (b * e - c * d) / D;
      tc = (a * e - b * d) / D;
    }

    Point3f c1 = new Point3f(), c2 = new Point3f();
    c1.scale(sc, u);
    c1.add(p1);
    c2.scale(tc, v);
    c2.add(p2);
    List<Point3f> res = new ArrayList<Point3f>(2);
    res.add(c1);
    res.add(c2);
    return res;
  }
  
  public static float cross(Vector2f a, Vector2f b) {
    return a.x * b.y - a.y * b.x;
  }
  
  public static float length2(Tuple2f a) {
    float dx = a.x;
    float dy = a.y;
    return (dx * dx + dy * dy);
  }
  
  /**
   * Calculates angle in radian between two vectors.
   * @param a vector with nonzero length
   * @param b vector with nonzero length
   * @return
   */
  public static float angle(Vector2f a, Vector2f b) {
    float denom = (float)Math.sqrt(Geometry.length2(a) * 
                                   Geometry.length2(b));
    float result = a.dot(b) / denom;
    return (float)Math.acos(result);
  }
  
  /**
   * Calculates the intersection between 2 lines ab and cd.
   * @param a point on line ab.
   * @param b point on line ab.
   * @param c point on line cd.
   * @param d point on line cd.
   * @return null if ab and cd are parallel, otherwise the intersection of line 
   *    ab and line cd.
   */
  public static Point2f intersection(Point2f a, Point2f b, Point2f c, Point2f d) 
  {
    Vector2f ac = new Vector2f();
    Vector2f cd = new Vector2f();
    Vector2f ab = new Vector2f();
    Point2f result = new Point2f();
    ac.sub(c, a);
    cd.sub(d, c);
    ab.sub(b, a);
    float cp = Geometry.cross(ab, cd);
    if (cp < EPS && cp > -EPS)
      return null;
    float s = Geometry.cross(ac, cd) / cp;
    result.scaleAdd(s, ab, a);
    return result;
  }
  
  public static Point3f midpoint(Point3f p1, Point3f p2) {
    return new Point3f((p1.x + p2.x) / 2, (p1.y + p2.y) / 2, (p1.z + p2.z) / 2);
  }
  
  /**
   * Given a plane, find z coordinate of a point on the plane with known x, y
   * coordinates.
   * (px - x) * nx + (py - y) * ny + (pz - z) * nz = 0
   * @param x x coordinate of the point.
   * @param y y coordinate of the point.
   * @param p a point on the plane.
   * @param n the normal of the plane.
   * @return if z coordinate of the normal vector is 0, and x, y are valid, z
   *    can take any value and the point returned is (x, y, 0); if x, y are not
   *    valid, returns null. If z coordinate of the normal vector is not 0, 
   *    returns a point of a specific z coordinate.
   */
  public static Point3f pointOnPlaneZ(float x, float y, Point3f p, 
      Vector3f n) {
    float a = -n.x * (p.x - x) - n.y * (p.y - y);
    if (n.z < EPS && n.z > -EPS) {
      if (a < EPS && a > -EPS) {
        return new Point3f(x, y, 0);
      } else {
        logger.warning("x, y values are invalid for the plane.");
        return null;
      }
    } else {
      float z = p.z - a / n.z;
      return new Point3f(x, y, z);
    }
  }
  
  /**
   * 
   * @param p0 a point on the line.
   * @param p1 a point on the line.
   * @param v0 a point on the plane.
   * @param n normal of the plane.
   * @return null if the line p0p1 is parallel to the plane.
   */
  public static Point3f linePlaneIntersection(Point3f p0, Point3f p1, 
      Point3f v0, Vector3f n) {
    Vector3f u = new Vector3f();
    u.sub(p1, p0);
    Vector3f w = new Vector3f();
    w.sub(p0, v0);
    
    float d1 = n.dot(u);
    if (Math.abs(d1) < EPS)
      return null;
    
    float d2 = -n.dot(w);
    float s = d2 / d1;
    
    Point3f res = new Point3f();
    res.scale(s, u);
    res.add(p0);
    return res;
  }
}
