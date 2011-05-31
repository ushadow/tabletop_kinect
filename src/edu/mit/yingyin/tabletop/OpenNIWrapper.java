package edu.mit.yingyin.tabletop;

import java.nio.IntBuffer;

import rywang.util.DirectBufferUtils;

public class OpenNIWrapper {
  public static final int DEPTH_WIDTH = 640;
  public static final int DEPTH_HEIGHT = 480;

  static { 
    System.loadLibrary("openni-java-wrapper"); 
  }
  
  public static void main(String[] args) {
    OpenNIWrapper openniWrapper = new OpenNIWrapper();
    boolean ret = openniWrapper.initFromXmlFile("config/config.xml");
    System.out.println(ret);
  }
  
  private IntBuffer ctrlBlock;
  private IntBuffer depthBuf;
  
  public OpenNIWrapper() {
    ctrlBlock = DirectBufferUtils.allocateIntBuffer(2);
    depthBuf = DirectBufferUtils.allocateIntBuffer(DEPTH_HEIGHT * DEPTH_WIDTH);
  }
  
  /**
   * 
   * @param configFile xml file name to initialize the OpenNI context.
   * @return true if the initialization is successful; false otherwise.
   */
  public boolean initFromXmlFile(String configFile) {
    return initFromXmlFile(ctrlBlock, configFile);
  }
  
  public boolean waitAnyUpdateAll() {
    return waitAnyUpdateAll(ctrlBlock);
  }
  
  public void getDepthMap() {
    getDepthMap(ctrlBlock, depthBuf, DEPTH_WIDTH, DEPTH_HEIGHT);
  }
  
  private native boolean initFromXmlFile(IntBuffer ctrlBlock, 
                                         String configFile); 
  private native boolean waitAnyUpdateAll(IntBuffer ctrlBlock);
  private native void getDepthMap(IntBuffer ctrlBlock, IntBuffer depthBuf,
                                  int width, int height);
}
