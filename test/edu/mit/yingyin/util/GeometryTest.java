package edu.mit.yingyin.util;

import static org.junit.Assert.*;

import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.junit.Test;

public class GeometryTest {

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
}
