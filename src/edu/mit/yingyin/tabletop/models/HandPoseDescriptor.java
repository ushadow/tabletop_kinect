package edu.mit.yingyin.tabletop.models;

import java.nio.FloatBuffer;

import com.googlecode.javacv.cpp.opencv_core.CvMat;

/**
 * A cylindrical descriptor.
 * @author yingyin
 *
 */
public class HandPoseDescriptor {
  private final int NUM_CIRCLES = 5;
  private final int NUM_SECTORS = 8;
  private final int NUM_DEPTH_SECTIONS = 5;
  private final float radius, radiusWidthInv, sectorWidthInv, depthWidthInv;
  private float minDepth, maxDepth;
  private float[] histogram = 
      new float[NUM_CIRCLES * NUM_SECTORS * NUM_DEPTH_SECTIONS]; 
  
  /**
   * Creates a descriptor.
   * @param points in physical coordinates.
   */
  public HandPoseDescriptor(CvMat points) {
    if (points.cols() != 3)
      throw new IllegalArgumentException(
          "The points matrix should have 3 colums.");
    radius = findRadius(points);
    radiusWidthInv = NUM_CIRCLES / radius;
    sectorWidthInv = (float) (NUM_SECTORS / (Math.PI * 2));
    findDepthMinMax(points);
    depthWidthInv = NUM_DEPTH_SECTIONS / (maxDepth - minDepth);
    computeDescriptor(points);
  }

  /**
   * Radius of the descriptor. It is calculated according to CAMSHIFT window
   * size.
   * @return
   */
  public double radius() { return radius; }
  
  public float descriptorValue(int r, int s, int d) {
    return histogram[r * NUM_SECTORS * NUM_DEPTH_SECTIONS + 
                     s * NUM_DEPTH_SECTIONS + d];
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < histogram.length - 1; i++) {
      sb.append(histogram[i]);
      sb.append(",");
    }
    sb.append(histogram[histogram.length - 1]);
    return sb.toString();
  }
  
  /**
   * Radius of the window enclosing the hand, excluding outliers.
   * @param points centered at the origin.
   * @return
   */
  private float findRadius(CvMat points) {
    // 1 pixel is roughly 2mm.
    return (float) Math.sqrt(points.rows()) * 2;
  }
  
  /**
   * 
   * @param points a matrix of float numbers. Each row is a 3 dimensional point.
   */
  private void findDepthMinMax(CvMat points) {
    FloatBuffer fb = points.getFloatBuffer();
    fb.rewind();
    minDepth = Float.POSITIVE_INFINITY;
    maxDepth = Float.NEGATIVE_INFINITY;
    for (int i = 0; i < points.rows(); i++) {
      float d = fb.get(i * 3 + 2);
      minDepth = Math.min(d, minDepth);
      maxDepth = Math.max(d, maxDepth);
    }
  }
  
  private void computeDescriptor(CvMat points) {
    float[] p = new float[3];
    FloatBuffer fb = points.getFloatBuffer();
    fb.rewind();
    int total = 0;
    while (fb.remaining() > 0) {
      fb.get(p);
      float r = (float) Math.sqrt(p[0] * p[0] + p[1] * p[1]);
      if (r <= radius) {
        // From -pi to pi.
        float theta = (float) (Math.atan2(p[1], p[0]) + Math.PI);
        int rIndex = (int) (r * radiusWidthInv); 
        rIndex = Math.min(rIndex, NUM_CIRCLES - 1);
        int sIndex = (int) (theta * sectorWidthInv);
        sIndex = Math.min(sIndex, NUM_SECTORS - 1);
        int dIndex = (int) ((p[2] - minDepth) * depthWidthInv);
        dIndex = Math.min(dIndex, NUM_DEPTH_SECTIONS - 1);
        histogram[rIndex * NUM_SECTORS * NUM_DEPTH_SECTIONS + 
                  sIndex * NUM_DEPTH_SECTIONS + dIndex]++;
        total++;
      }
    }
    for (int i = 0; i < histogram.length; i++) 
      histogram[i] /= total;
  }
}
