package edu.mit.yingyin.tabletop;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Calibration {
  private static final int WIDTH = 640;
  private static final int HEIGHT = 480;
  
  static public void convertRawToPng(String fileName) {
    BufferedImage image = new BufferedImage(WIDTH, HEIGHT, 
        BufferedImage.TYPE_USHORT_GRAY);
    short[] depthArray = ((DataBufferUShort)image.getRaster().getDataBuffer()).
        getData();
    int totalPixels = WIDTH * HEIGHT;
    int[] intArray = new int[totalPixels];
    OpenNIWrapper.loadFile(fileName, intArray);
    int max = 0;
    int min = 65535;
    for (int i = 0; i < totalPixels; i++) {
      int value = intArray[i];
      if (value != 0 ) { 
        max = Math.max(max, value);
        min = Math.min(min, value);
      }
    }
    System.out.println("max = " + max + " min = " + min);
    for (int i = 0; i < totalPixels; i++) {
      int value = intArray[i];
      
      depthArray[i] = value == 0 ? 0 : 
          (short)((intArray[i] - min) * 65535 / (max - min));
    }
    String newFileName = fileName.replace(".raw", ".png");
    try {
      ImageIO.write(image, "PNG", new File(newFileName));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  static public void main(String args[]) {
    convertRawToPng("data/CapturedFrames/Depth_0.raw");
  }
}
