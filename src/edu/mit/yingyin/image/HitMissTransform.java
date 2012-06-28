package edu.mit.yingyin.image;

import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;

/**
 * HitMissTransform is an algorithm to 'hit and miss' a binary image using a 
 * 3x3 kernel.
 *
 * Modified from the code at 
 * http://homepages.inf.ed.ac.uk/rbf/HIPR2/flatjavasrc/HitMiss.java
 */
public class HitMissTransform {
  /**
   * Returns true if the 8 neighbours of p match the kernel 0 is background 1 is
   * foreground 2 is don't care.
   * 
   * @param p the point at the centre of the 9 pixel neighbourhood
   * @param pixels the 2D array of the image
   * @param w the width of the image
   * @param h the height of the image
   * @param kernel the array of the kernel values
   * @return True if the kernel and image match.
   */
  public static boolean kernelMatch(Point p, byte[][] pixels, int w, int h,
                                    int[] kernel) {
    int matched = 0;
    for (int j = -1; j < 2; ++j) {
      for (int i = -1; i < 2; ++i) {
        if (kernel[((j + 1) * 3) + (i + 1)] == 2) {
          ++matched;
        } else if (p.x + i >= 0 && p.x + i < w && p.y + j >= 0 && p.y + j < h && 
                   ((pixels[p.y + j][p.x + i] == BinaryFast.foreground && 
                     kernel[((j + 1) * 3) + (i + 1)] == 1) || 
                    (pixels[p.y + j][p.x + i] == BinaryFast.background && 
                     kernel[((j + 1) * 3) + (i + 1)] == 0))) {
          ++matched;
        }
      }
    }
    return matched == 9;
  }

  /**
   * Applies the hitmiss operation to a set of pixels stored in a hash table.
   * 
   * @param b  the BinaryFast input image
   * @param input the set of pixels requiring matching
   * @param kernel the kernel to match them with
   * @return A hash table containing all the successful matches.
   */
  public static HashSet<Point> HitMissHashSet(BinaryFast b,
      HashSet<Point> input, int[] kernel) {
    HashSet<Point> output = new HashSet<Point>();
    Iterator<Point> it = input.iterator();
    while (it.hasNext()) {
      Point p = (Point) it.next();
      if (kernelMatch((Point) p, b.pixels, b.w, b.h, kernel)) {
        output.add(p);
      }
    }
    return output;
  }

  /**
   * Returns true if the 3x3 kernel has no 0s.
   * 
   * @param kernel
   *          the array storing the 9 values
   * @return True if no 0s (false otherwise)
   */
  public static boolean kernelNo0s(int[] kernel) {
    for (int i = 0; i < 9; ++i) {
      if (kernel[i] == 0)
        return false;
    }
    return true;
  }
}
