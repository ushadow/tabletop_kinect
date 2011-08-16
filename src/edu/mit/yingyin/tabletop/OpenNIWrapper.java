package edu.mit.yingyin.tabletop;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;

import rywang.util.DirectBufferUtils;

public class OpenNIWrapper {

  static { 
    System.loadLibrary("openni-java-wrapper"); 
  }
  
  public static int MAX_DEPTH = 65535;
  public static int DEFAULT_DEPTH_WIDTH = 640;
  public static int DEFAULT_DEPTH_HEIGHT = 480;
  
  static public BufferedImage rawDepthToBufferedImage(String fileName) {
    return rawDepthToBufferedImage(fileName, DEFAULT_DEPTH_WIDTH, 
                                   DEFAULT_DEPTH_HEIGHT);
  }
  
  /**
   * Converts a 2 dimensional array of depth values to a gray BufferedImage.
   * @param fileName file name of the .raw file with depth values.
   * @param width width of the BufferedImage.
   * @param height height of the BufferedImage.
   * @return a gray BufferedImage with colors proportional to the depth values.
   */
  static public BufferedImage rawDepthToBufferedImage(String fileName, 
      int width, int height) {
    BufferedImage image = new BufferedImage(width, height, 
        BufferedImage.TYPE_USHORT_GRAY);
    short[] depthArray = ((DataBufferUShort)image.getRaster().getDataBuffer()).
        getData();
    int totalPixels = width * height;
    int[] intArray = new int[totalPixels];
    OpenNIWrapper.loadFile(fileName, intArray);
    int max = 0;
    int min = MAX_DEPTH;
    for (int i = 0; i < totalPixels; i++) {
      int value = intArray[i];
      if (value != 0 ) { 
        max = Math.max(max, value);
        min = Math.min(min, value);
      }
    }
    System.out.println("OpenNIWrapper#rawDepthToBufferedImage: " +
    		"max depth = " + max + " min depth = " + min);
    for (int i = 0; i < totalPixels; i++) {
      int value = intArray[i];
      depthArray[i] = value == 0 ? 0 : 
          (short)((value - min) * MAX_DEPTH / (max - min));
    }
    return image;
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
  private int width, height;
  private boolean initialized = false;
  
  public OpenNIWrapper(String configFile) {
    initialized = initFromXmlFile(configFile);
  }
  
  public int getDepthWidth() { return width; }
  
  public int getDepthHeight() { return height; }
  
  /**
   * Initializes the instance from a OpenNI configuration file.
   * 
   * @param configFile xml file name to initialize the OpenNI context.
   * @return true if the initialization is successful; false otherwise.
   */
  private boolean initFromXmlFile(String configFile) {
    ctrlBlock = DirectBufferUtils.allocateIntBuffer(2);
    IntBuffer widthBuf = DirectBufferUtils.allocateIntBuffer(1);
    IntBuffer heightBuf = DirectBufferUtils.allocateIntBuffer(1);
    boolean ret = initFromXmlFile(ctrlBlock, configFile, widthBuf, heightBuf);
    width = widthBuf.get(0);
    height = heightBuf.get(0);
    depthBuf = DirectBufferUtils.allocateIntBuffer(width * height);
    return ret;
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
  
  public boolean initialized() {
    return initialized;
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
                                         String configFile, 
                                         IntBuffer width, IntBuffer height); 
  private native boolean waitAnyUpdateAll(IntBuffer ctrlBlock);
  private native void getDepthMap(IntBuffer ctrlBlock, IntBuffer depthBuf);
  private native void cleanUp(IntBuffer ctrlBlock); 
  private native void convertDepthProjectiveToWorld(IntBuffer ctrlBlock, 
                                                    FloatBuffer points);
}
