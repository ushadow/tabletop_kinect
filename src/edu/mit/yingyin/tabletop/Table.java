package edu.mit.yingyin.tabletop;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

/**
 * Model of the table.
 * @author yingyin
 *
 */
public class Table {
  private int[] depthMap;
  /**
   * Table width and height in depth image.
   */
  private int width, height;
  private IplImage avg, diff;

  public Table(Background background) {
    avg = background.avg();
    diff = background.diff();
  }
  
  /**
   * Gets the height of the table at point(x, y);
   * @param x x coordinate of the point.
   * @param y y coordinate of the point.
   * @return height of the table at (x, y).
   */
  public int getDepth(int x, int y) {
    return depthMap[y * width + x];
  }
  
  public int getWidth() { return width; }
  
  public int getHeight() { return height; }
  
  /**
   * @return average depth of the table after it is initialized.
   */
  public int getAverageDepth() { 
    long sum = 0;
    for (int i = 0; i < depthMap.length; i++) 
      sum += depthMap[i];
    return (int)(sum / depthMap.length);
  }
}
