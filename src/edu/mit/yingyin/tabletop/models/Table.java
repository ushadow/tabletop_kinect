package edu.mit.yingyin.tabletop.models;

import java.nio.FloatBuffer;

/**
 * Model of the table.
 * @author yingyin
 *
 */
public class Table {
  private static final int DIFF_SCALE = 5;
  
  static private Table table;

  /**
   * Average depth and depth difference scaled between 0 and 1.
   */
  private FloatBuffer avg, diff;
  private int avgWidthStep, diffWidthStep;
  private boolean initialized = false;

  public static Table instance() {
    if (table == null)
      table = new Table();
    return table;
  }
  
  /**
   * Initializes the table statistics.
   * @param avg
   * @param diff
   * @param avgWidthStep
   * @param diffWidthStep
   * @param scale used to scale the depth value.
   */
  public void init(FloatBuffer avg, FloatBuffer diff, int avgWidthStep,
                   int diffWidthStep) {
    this.avg = avg;
    this.diff = diff;
    this.avgWidthStep = avgWidthStep;
    this.diffWidthStep = diffWidthStep;
    initialized = true;
  }
  
  public void init(Background background) {
    init(background.avgBuffer(), background.diffBuffer(), 
         background.avgBufferWidthStep(), background.diffBufferWidthStep());
  }
  
  /**
   * Checks if the depth value of z at (x, y) is in contact with the table 
   * surface.
   * @param x
   * @param y
   * @param z physical depth in millimeter.
   * @return
   */
  public boolean isInContact(int x, int y, float z) {
    if (!initialized)
      return false;
    float tableDepth = depthAt(x, y);
    float tableDiff = diffAt(x, y);
    return z < tableDepth + tableDiff * DIFF_SCALE && 
        z > tableDepth - tableDiff * DIFF_SCALE;
  }

  /**
   * Gets the height of the table at point(x, y). The table must be initialized.
   * @param x x coordinate of the point.
   * @param y y coordinate of the point.
   * @return average depth of the table at (x, y).
   */
  private float depthAt(int x, int y) {
    return avg.get(y * avgWidthStep + x);
  }
  
  /**
   * Gets the average height difference of the table at point(x, y). The table
   * must be initialized.
   * @param x
   * @param y
   * @return
   */
  private float diffAt(int x, int y) {
    return diff.get(y * diffWidthStep + x);
  }
  
  private Table() {}
}
