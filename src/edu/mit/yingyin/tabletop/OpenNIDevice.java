package edu.mit.yingyin.tabletop;

public interface OpenNIDevice {
  abstract public int getDepthWidth();
  abstract public int getDepthHeight();
  abstract public void waitDepthUpdateAll() throws Exception;
  abstract public void getDepthArray(int[] depthArray) throws Exception;
  abstract public void release();
}
