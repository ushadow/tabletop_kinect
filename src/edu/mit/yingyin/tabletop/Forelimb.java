package edu.mit.yingyin.tabletop;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;

public class Forelimb {
  static public class ValConfiPair<T> {
    public T value;
    public float confidence;
    
    public ValConfiPair(T v, float c) {
      value = v;
      confidence = c;
    }
  }
  
  public List<ValConfiPair<Point3f>> fingertips = 
      new ArrayList<ValConfiPair<Point3f>>();
  public List<List<Point3f>> fingers = new ArrayList<List<Point3f>>();
  public Point center;
  
  public Forelimb() {}
  
  public Forelimb(Forelimb other) {
    center = new Point(other.center);
    for (ValConfiPair<Point3f> p: other.fingertips) {
      fingertips.add(new ValConfiPair<Point3f>(
          new Point3f(p.value), p.confidence));
    }
  }
}
