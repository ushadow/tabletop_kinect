package edu.mit.yingyin.image;

import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;

/**
 * BinaryFast is a representation of a binary image storing the foreground and 
 * background edge pixels in hash tables for efficiency.
 *
 * Modified from the code at 
 * http://homepages.inf.ed.ac.uk/rbf/HIPR2/flatjavasrc/BinaryFast.java
 */
public class BinaryFast {
  /**
   * Background is black.
   */
  public static final byte background = 0;
  /**
   * Foreground is white.
   */
  public static final byte foreground = 1;
  /**
   * Width of the image.
   */
  public int w;
  /**
   * Height of the image.
   */
  public int h;
  /**
   * Size of the image (w*h), number of pixels.
   */
  public int s;
  /**
   * The 2D array of all pixels.
   */
  public byte[][] pixels;
  /**
   * The hash set storing positions of foreground edge pixels as Points.
   */
  public HashSet<Point> foregroundEdgePixels = new HashSet<Point>();
  /**
   * The hash set storing positions of background edge pixels as Points.
   */
  public HashSet<Point> backgroundEdgePixels = new HashSet<Point>();

  /**
   * Constructor taking a 2D array of pixel values.
   * 
   * @param p The 2D array of pixel values.
   * @param width The width of the image.
   * @param height The height of the image.
   */
  public BinaryFast(byte[][] p, int width, int height) { 
    pixels = p;
    w = width;
    h = height;
    s = w * h;

    generateForegroundEdge();
    generateBackgroundEdgeFromForegroundEdge();
  }

  public BinaryFast() {
    foregroundEdgePixels = new HashSet<Point>();
    backgroundEdgePixels = new HashSet<Point>();
  }

  public BinaryFast(BinaryFast oldBinary) {
    w = oldBinary.w;
    h = oldBinary.h;
    s = oldBinary.s;

    backgroundEdgePixels = new HashSet<Point>();
    Iterator<Point> it1 = oldBinary.backgroundEdgePixels.iterator();
    while (it1.hasNext()) {
      backgroundEdgePixels.add(it1.next());
    }
    foregroundEdgePixels = new HashSet<Point>();
    Iterator<Point> it2 = oldBinary.foregroundEdgePixels.iterator();
    while (it2.hasNext()) {
      foregroundEdgePixels.add(it2.next());
    }
    pixels = (byte[][]) oldBinary.pixels.clone();
  }

  /**
   * Removes a foreground pixel from the 2D array by setting its value to
   * background.
   * 
   * @param p The point to be removed.
   */
  public void removePixel(Point p) { pixels[p.y][p.x] = background; }

  /**
   * Adds a foreground pixel to the 2D array by setting its value to foreground.
   * 
   * @param p The point to be added.
   */
  public void addPixel(Point p) { pixels[p.x][p.y] = foreground; }

  /**
   * Generates a new 2D array of pixels from a hash set of foreground pixels.
   * 
   * @param pix the hash set of foreground pixels.
   */
  public void generatePixels(HashSet<Point> pix) {
    // Reset all pixels to background
    for (int j = 0; j < h; ++j) {
      for (int i = 0; i < w; ++i) {
        pixels[j][i] = background;
      }
    }
    convertToPixels(pix);
  }

  /**
   * Adds the pixels from a hash set to the 2D array of pixels.
   * 
   * @param pix
   *          The hash set of foreground pixels to be added.
   */
  public void convertToPixels(HashSet<Point> pix) {
    Point p = new Point();
    Iterator<Point> it = pix.iterator();
    while (it.hasNext()) {
      p = (Point) it.next();
      pixels[p.y][p.x] = foreground;
    }
  }

  /**
   * Generates the foreground edge hash set from the 2D array of pixels.
   */
  public void generateForegroundEdge() {
    foregroundEdgePixels.clear();
    Point p;
    for (int n = 0; n < h; ++n) {
      for (int m = 0; m < w; ++m) {
        if (pixels[n][m] == foreground) {
          p = new Point(m, n);
          boolean done = false;
          for (int j = -1; j < 2; ++j) {
            for (int i = -1; i < 2; ++i) {
              if (p.x + i >= 0 && p.x + i < w && p.y + j >= 0 && 
                  p.y + j < h && pixels[p.y + j][p.x + i] == background) {
                foregroundEdgePixels.add(p);
                done = true;
                break;
              }
            }
            if (done) break;
          }
        }
      }
    }
  }

  /**
   * Generates the background edge hash set from the foreground edge hash set
   * and the 2D array of pixels.
   */
  public void generateBackgroundEdgeFromForegroundEdge() {
    backgroundEdgePixels.clear();
    Point p, p2;
    Iterator<Point> it = foregroundEdgePixels.iterator();
    while (it.hasNext()) {
      p = new Point(it.next());
      for (int j = -1; j < 2; ++j) {
        for (int i = -1; i < 2; ++i) {
          if (p.x + i >= 0 && p.x + i < w && p.y + j >= 0 && p.y + j < h) {
            p2 = new Point(p.x + i, p.y + j);
            if (pixels[p2.y][p2.x] == background) 
              backgroundEdgePixels.add(p2);
          }
        }
      }
    }
  }

  /**
   * Generates the foreground edge hash set from the background edge hash set
   * and the 2D array of pixels.
   */
  public void generateForegroundEdgeFromBackgroundEdge() {
    foregroundEdgePixels.clear();
    Point p, p2;
    Iterator<Point> it = backgroundEdgePixels.iterator();
    while (it.hasNext()) {
      p = new Point((Point) it.next());
      for (int j = -1; j < 2; ++j) 
        for (int i = -1; i < 2; ++i) {
          if ((p.x + i >= 0) && (p.x + i < w) && (p.y + j >= 0) && 
              (p.y + j < h)) {
            p2 = new Point(p.x + i, p.y + j);
            if (pixels[p2.y][p2.x] == foreground) 
              foregroundEdgePixels.add(p2);
          }
        }
    }
  }
}
