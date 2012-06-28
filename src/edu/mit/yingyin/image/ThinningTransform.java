package edu.mit.yingyin.image;

import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Thin is an algorithm to thin a binary image using a 3x3 kernel.
 *
 * If the foreground and background pixels in the kernel exactly match 
 * foreground and background pixels in the image, the image pixel underneath 
 * the origin of the kernel element is set to background.
 * 
 * Modified from the code at 
 * http://homepages.inf.ed.ac.uk/rbf/HIPR2/flatjavasrc/Thin.java
 */
public class ThinningTransform {
  /**
   * Takes an image and a kernel and thins it once.
   * 
   * @param b the BinaryFast input image
   * @param kernel the thinning kernel
   * @return the thinned BinaryFast image
   */
  public static BinaryFast thinBinaryOnce(BinaryFast b, int[] kernel) {
    Point p;
    HashSet<Point> result = new HashSet<Point>();
    HashSet<Point> inputHashSet = new HashSet<Point>();
    if (HitMissTransform.kernelNo0s(kernel)) {
      for (int j = 0; j < b.h; ++j) {
        for (int i = 0; i < b.w; ++i) {
          if (b.pixels[i][j] == BinaryFast.foreground) {
            inputHashSet.add(new Point(i, j));
          }
        }
      }
    } else {
      Iterator<Point> it = b.foregroundEdgePixels.iterator();
      while (it.hasNext()) {
        inputHashSet.add(it.next());
      }
    }
    result = HitMissTransform.HitMissHashSet(b, inputHashSet, kernel);
    Iterator<Point> it = result.iterator();
    while (it.hasNext()) {
      p = new Point((Point) it.next());
      // make p a background pixel and update the edge sets
      b.removePixel(p);
      b.foregroundEdgePixels.remove(p);
      b.backgroundEdgePixels.add(p);
      // check if new foreground pixels are exposed as edges
      for (int j = -1; j < 2; ++j) 
        for (int i = -1; i < 2; ++i) 
          if (p.x + i >= 0 && p.y + j > 0 && p.x + i < b.w && p.y + j < b.h
              && b.pixels[p.y + j][p.x + i] == BinaryFast.foreground) {
            Point p2 = new Point(p.x + i, p.y + j);
            b.foregroundEdgePixels.add(p2);
          }
    }
    return b;
  }

  /**
   * Takes an image and a kernel and thins it the specified number of times.
   * 
   * @param b
   *          the BinaryFast input image
   * @param kernel
   *          the thinning kernel
   * @param iterations
   *          required
   * @return the thinned BinaryFast image
   */
  public static BinaryFast thinImage(BinaryFast binary, int[] kernel,
      int iterations) {
    for (int i = 0; i < iterations; ++i) {
      binary = thinBinaryOnce(binary, kernel);
    }
    binary.generateBackgroundEdgeFromForegroundEdge();
    return binary;
  }
}
