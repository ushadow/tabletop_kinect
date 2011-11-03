package edu.mit.yingyin.tabletop.app;

import edu.mit.yingyin.tabletop.PartialOpenNIDevice;

public class DepthCalibrationApp {
  
  public static void main(String args[]) {
    String dir = "data/depth_calibration/";
    String fileFormat = dir + "Depth_%d.raw";
    int numFiles = 5;
    int depthArray[] = new int[640 * 480];
    for (int i = 0; i < numFiles; i++) {
      PartialOpenNIDevice.loadFile(String.format(fileFormat, i), depthArray);
      int count = 0;
      long sum = 0;
      for(int v : depthArray) {
        if (v != 0) {
          sum += v;
          count++;
        }
      }
      System.out.println(String.format("index: %d, depth: %d", i, sum / count));
    }
  }
}
