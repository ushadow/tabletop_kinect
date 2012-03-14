package edu.mit.yingyin.tabletop;

import java.nio.FloatBuffer;

/**
 * Model of the table.
 * @author yingyin
 *
 */
public class Table {
  static private Table table;
  /**
   * Table width and height in depth image.
   */
  private int avgWidthStep, diffWidthStep;
  private FloatBuffer avg, diff;

  public static Table instance() {
    if (table == null)
      table = new Table();
    return table;
  }
  
  public void init(FloatBuffer avg, FloatBuffer diff, int avgWidthStep,
                   int diffWidthStep) {
    this.avg = avg;
    this.diff = diff;
    this.avgWidthStep = avgWidthStep;
    this.diffWidthStep = diffWidthStep;
  }
  
  /**
   * Gets the height of the table at point(x, y);
   * @param x x coordinate of the point.
   * @param y y coordinate of the point.
   * @return average depth of the table at (x, y).
   */
  public float depthAt(int x, int y) {
    return avg.get(x * avgWidthStep + y);
  }
  
  public float diffAt(int x, int y) {
    return diff.get(x * diffWidthStep + y);
  }
  
  private Table() {}
}
