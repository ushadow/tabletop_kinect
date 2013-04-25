package edu.mit.yingyin.util;

import static com.googlecode.javacv.cpp.opencv_core.CV_32FC1;
import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import org.junit.Test;

import com.googlecode.javacv.cpp.opencv_core.CvMat;

public class GeometryTest {
  private static final double EPS = 1e-5;
  
  @Test
  public void testClosestPointsLineToLineParallel() {
    Point3f p1 = new Point3f(0, 0, 0);
    Vector3f u = new Vector3f(1, 0, 0);
    Point3f p2 = new Point3f(0, 1, 0);
    Vector3f v = new Vector3f(1, 0, 0);
    List<Point3f> res = Geometry.closestPointsLineToLine(p1, u, p2, v);
    assertEquals(new Point3f(0, 0, 0), res.get(0));
    assertEquals(new Point3f(0, 1, 0), res.get(1));
  }

  @Test
  public void testClosestPointsLineToLineIntersect() {
    Point3f p1 = new Point3f(0, 0, 0);
    Vector3f u = new Vector3f(1, 0, 0);
    Point3f p2 = new Point3f(0, 1, 0);
    Vector3f v = new Vector3f(0, 1, 0);
    List<Point3f> res = Geometry.closestPointsLineToLine(p1, u, p2, v);
    assertEquals(new Point3f(0, 0, 0), res.get(0));
    assertEquals(new Point3f(0, 0, 0), res.get(1));
  }
  
  @Test
  public void testClosestPointsLineToLineNoIntersection() {
    Point3f p1 = new Point3f(0, 0, 0);
    Vector3f u = new Vector3f(1, 0, 0);
    Point3f p2 = new Point3f(0, 1, 1);
    Vector3f v = new Vector3f(0, 1, 0);
    List<Point3f> res = Geometry.closestPointsLineToLine(p1, u, p2, v);
    assertEquals(new Point3f(0, 0, 0), res.get(0));
    assertEquals(new Point3f(0, 0, 1), res.get(1));
  }
  
  @Test
  public void testMidpoint() {
    Point3f p1 = new Point3f(0, 0, 0);
    Point3f p2 = new Point3f(1, 1, 1);
    Point3f pmid = Geometry.midpoint(p1, p2);
    assertEquals(new Point3f(0.5f, 0.5f, 0.5f), pmid);
  }
  
  @Test
  public void testPointOnPlaneZ() {
    Point3f p = Geometry.pointOnPlaneZ(0, 0, new Point3f(0, 0, 0), 
        new Vector3f(0, 0, 1));
    assertEquals(new Point3f(0, 0, 0), p);
    
    p = Geometry.pointOnPlaneZ(1, 1, new Point3f(0, 0, 0), 
        new Vector3f(0, 0, 1));
    assertEquals(new Point3f(1, 1, 0), p);
    
    p = Geometry.pointOnPlaneZ(1, 1, new Point3f(0, 0, 0), 
        new Vector3f(0, 1, 1));
    assertEquals(new Point3f(1, 1, -1), p);
    
    p = Geometry.pointOnPlaneZ(1, 1, new Point3f(0, 0, 0), 
        new Vector3f(0, 1, 0));
    assertEquals(null, p);
    
    p = Geometry.pointOnPlaneZ(1, 0, new Point3f(0, 0, 0), 
        new Vector3f(0, 1, 0));
    assertEquals(new Point3f(1, 0, 0), p);
  }
  
  @Test
  public void testRotMatrixToEuler() {
    CvMat rot = CvMat.create(3, 3, CV_32FC1);
    rot.put(0, 1, 0, 0, 0, 0, -1, 0, 1, 0);
    Tuple3f euler = Geometry.rotMatrixToEuler(rot);
    assertEquals(new Point3f((float) Math.PI / 2, 0, 0), euler);
    
    rot.put(0, 0, 0, 1, 0, 1, 0, -1, 0, 0);
    euler = Geometry.rotMatrixToEuler(rot);
    assertEquals(new Point3f(0, (float) Math.PI / 2, 0), euler);
    
    float a = (float) (1 / Math.sqrt(2));
    rot.put(0, 0.5, a * 0.5 - 0.5, a * 0.5 + 0.5, 
               0.5, a * 0.5 + 0.5, a * 0.5 - 0.5, 
               -a, 0.5, 0.5);
    euler = Geometry.rotMatrixToEuler(rot);
    assertEquals((float) Math.PI / 4, euler.x, EPS);
    assertEquals((float) Math.PI / 4, euler.y, EPS);
    assertEquals((float) Math.PI / 4, euler.z, EPS);
  }
  
  @Test
  public void testDistPointToPlane() {
    float dist = Geometry.distancePointToPlane(new Vector3f(0, 0, 1), 
        new Point3f(0, 0, 0), new Point3f(0, 0, 1));
    assertEquals(1, dist, EPS);
    dist = Geometry.distancePointToPlane(new Vector3f(0, 0, 1),
        new Point3f(0, 0, 0), new Point3f(0, 0, -1));
    assertEquals(-1, dist, EPS);
    dist = Geometry.distancePointToPlane(new Vector3f(0, 0, 1),
        new Point3f(0, 0, 0), new Point3f(2, 1, 0));
    assertEquals(0, dist, EPS);
    dist = Geometry.distancePointToPlane(new Vector3f(0, 0, 1),
        new Point3f(0, 0, 0), new Point3f(2, 1, 1));
    assertEquals(1, dist, EPS);
  }
}
