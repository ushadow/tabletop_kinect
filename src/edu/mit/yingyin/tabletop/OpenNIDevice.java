package edu.mit.yingyin.tabletop;

public interface OpenNIDevice {
  public int getDepthWidth();
  public int getDepthHeight();
  public void waitDepthUpdateAll() throws Exception;
  public void getDepthArray(int[] depthArray) throws Exception;
  public void release();
  /**
   * This method is optional.
   * @return ID of the current depth frame.
   */
  public int getDepthFrameID();
}
