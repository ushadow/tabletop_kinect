package edu.mit.yingyin.tabletop;

public class Table {
  private int[] depthMap;
  private int width, height;
  
  public boolean isInitialized() { return depthMap != null; }
  
  public void init(int[] depthMap, int width, int height) {
    this.depthMap = depthMap.clone();
    this.width = width;
    this.height = height;
  }
  
  /**
   * Gets the height of the table at point(x, y);
   * @param x x coordinate of the point.
   * @param y y coordinate of the point.
   * @return height of the table at (x, y).
   */
  public int getHeight(int x, int y) {
    return depthMap[y * width + x];
  }
}
