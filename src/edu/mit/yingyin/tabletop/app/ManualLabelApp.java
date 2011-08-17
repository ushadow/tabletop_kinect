package edu.mit.yingyin.tabletop.app;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import edu.mit.yingyin.gui.ImageViewer;
import edu.mit.yingyin.image.ImageConvertUtils;
import edu.mit.yingyin.tabletop.OpenNIWrapper;

public class ManualLabelApp {
  private static String openniConfigFile = "config/config.xml";
  
  public static void main(String[] args) {
    OpenNIWrapper openni = new OpenNIWrapper(openniConfigFile);
    int width = openni.getDepthWidth();
    int height = openni.getDepthHeight();
    int[] depthRawData = new int[width * height];
    BufferedImage image = null;
    ImageViewer viewer = new ImageViewer("Label", new Dimension(width, height));
    while (viewer.isVisible()) {
      openni.waitAnyUpdateAll();
      openni.getDepthMap(depthRawData);
      image = ImageConvertUtils.depthToGrayBufferedImage(depthRawData, 
          width, height);
      viewer.show(image);
    }
    openni.cleanUp();
    System.exit(0);
  }
}
