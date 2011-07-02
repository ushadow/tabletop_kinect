package edu.mit.yingyin.tabletop;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;

import rywang.util.DirectBufferUtils;

public class OpenNIWrapper {
  private static final int DEPTH_WIDTH = 640;
  private static final int DEPTH_HEIGHT = 480;

  static { 
    System.loadLibrary("openni-java-wrapper"); 
  }
  
  public static void main(String[] args) {
    OpenNIWrapper openniWrapper = new OpenNIWrapper();
    boolean ret = openniWrapper.initFromXmlFile("config/config.xml");
    System.out.println("initFromXmlFile: " + ret);
    ret = openniWrapper.waitAnyUpdateAll();
    System.out.println("waitAnyUpdateAll: " + ret);
    int[] depthMap = 
        new int[openniWrapper.getDepthWidth() * openniWrapper.getDepthHeight()];
    openniWrapper.getDepthMap(depthMap);
    int index = 0, maxDepth = 0;
    for (int h = 0; h < openniWrapper.getDepthHeight(); h++) {
      for (int w = 0; w < openniWrapper.getDepthWidth(); w++, index++)
        maxDepth = maxDepth > depthMap[index] ? maxDepth : depthMap[index];
        System.out.print(depthMap[index] + " ");
      System.out.println();
    }
    openniWrapper.cleanUp();
  }
  
  public static void loadFile(String fileName, int[] depthArray) {
    IntBuffer buffer = DirectBufferUtils.allocateIntBuffer(depthArray.length);
    loadFile(fileName, buffer, depthArray.length);
    buffer.get(depthArray);
  }
  
  private static native void loadFile(String fileName, IntBuffer buffer, 
                                      int size);
  
  private IntBuffer ctrlBlock;
  private IntBuffer depthBuf;
  
  public OpenNIWrapper() {
    ctrlBlock = DirectBufferUtils.allocateIntBuffer(2);
    depthBuf = DirectBufferUtils.allocateIntBuffer(DEPTH_HEIGHT * DEPTH_WIDTH);
  }
  
  public int getDepthWidth() { return DEPTH_WIDTH; }
  
  public int getDepthHeight() { return DEPTH_HEIGHT; }
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
  
  public void getDepthMap(int[] depthArray) {
    getDepthMap(ctrlBlock, depthBuf);
    depthBuf.rewind();
    depthBuf.get(depthArray);
  }
  
  public void cleanUp() {
    cleanUp(ctrlBlock);
  }
  
  public List<Point3f> converDepthProjectiveToWorld(List<Point3f> points) {
    FloatBuffer fb = DirectBufferUtils.allocateFloatBuffer(3 * points.size());
    List<Point3f> converted = new ArrayList<Point3f>(points.size());
    float[] pointArray = new float[3];
    for (int i = 0; i < points.size(); i++) {
      fb.get(pointArray, i * 3, 3);
      converted.add(new Point3f(pointArray));
    }
    return converted;
  }
  
  private native boolean initFromXmlFile(IntBuffer ctrlBlock, 
                                         String configFile); 
  private native boolean waitAnyUpdateAll(IntBuffer ctrlBlock);
  private native void getDepthMap(IntBuffer ctrlBlock, IntBuffer depthBuf);
  private native void cleanUp(IntBuffer ctrlBlock); 
  private native void convertDepthProjectiveToWorld(IntBuffer ctrlBlock, 
                                                    FloatBuffer points);
}
