package edu.mit.yingyin.tabletop;

import java.nio.IntBuffer;

import rywang.util.DirectBufferUtils;

public class OpenNIWrapper {
  static { 
    System.loadLibrary("openni-java-wrapper"); 
  }
  
  public static void main(String[] args) {
    OpenNIWrapper openniWrapper = new OpenNIWrapper();
    boolean ret = openniWrapper.initFromXmlFile("");
    System.out.println(ret);
  
  }
  
  private IntBuffer controlBlock;
  
  public OpenNIWrapper() {
    controlBlock = DirectBufferUtils.allocateIntBuffer(2);
  }
  
  public boolean initFromXmlFile(String configFile) {
    return initFromXmlFile(controlBlock, configFile);
  }
  
  private native boolean initFromXmlFile(IntBuffer cb, String configFile); 
}
