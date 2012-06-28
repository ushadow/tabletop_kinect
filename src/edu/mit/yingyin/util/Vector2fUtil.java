package edu.mit.yingyin.util;

import javax.vecmath.Point2f;
import javax.vecmath.Tuple2f;
import javax.vecmath.Vector2f;

/**
 * Utility class for 2D vector math.
 * @author yingyin
 *
 */
public class Vector2fUtil {
  private static float EPS = (float)1e-5;
  
  public static float dot(Vector2f a, Vector2f b) {
    return a.x * b.x + a.y * b.y;
  }
  
  public static float cross(Vector2f a, Vector2f b) {
    return a.x * b.y - a.y * b.x;
  }
  
  public static float lengthSquared(Tuple2f a) {
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
    float denom = (float)Math.sqrt(Vector2fUtil.lengthSquared(a) * 
                                   Vector2fUtil.lengthSquared(b));
    float result = Vector2fUtil.dot(a, b) / denom;
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
    float cp = Vector2fUtil.cross(ab, cd);
    if (cp < EPS && cp > -EPS)
      return null;
    float s = Vector2fUtil.cross(ac, cd) / cp;
    result.scaleAdd(s, ab, a);
    return result;
  }
}
