package edu.mit.yingyin.tabletop;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ShortBuffer;

import edu.mit.yingyin.gui.ImageViewer;
import edu.mit.yingyin.tabletop.HandProcessor.DebugFrame;
import edu.mit.yingyin.tabletop.HandProcessor.DebugImage;

public class MainDriver {
  private class Controller extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
      running = false;
    }
  }
  private OpenNIWrapper openni;
  private ImageViewer viewer;
  private boolean running = true;
  
  public MainDriver() {
    System.out.println("java.library.paht = " + 
                       System.getProperty("java.library.path"));
    openni = new OpenNIWrapper();
    openni.initFromXmlFile("config/config.xml");
    int depthWidth = openni.getDepthWidth();
    int depthHeight = openni.getDepthHeight();
    HandProcessor processor = new HandProcessor(depthWidth, depthHeight);
    int[] depthMap = new int[depthWidth * depthHeight];
    BufferedImage image = new BufferedImage(depthWidth, depthHeight, 
                                            BufferedImage.TYPE_INT_RGB);
    
    DebugFrame debugFrame = new DebugFrame(depthWidth, depthHeight);
    viewer = new ImageViewer("Depth Map", 
                             new Dimension(depthWidth, depthHeight));
    viewer.addWindowListener(new Controller());
    
    while (running) {
      openni.waitAnyUpdateAll();
      openni.getDepthMap(depthMap);
      processor.processData(depthMap);
//      int[] dstArray = 
//          ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
//      index = 0;
//      for (int h = 0; h < depthHeight; h++) {
//        for (int w = 0; w < depthWidth; w++, index++) 
//          dstArray[index] = (char)depthArray.get(index) * 256 / 
//                            maxDepth & 0x000000ff;
//      }
      //viewer.show(debugImage.draw(processor.getContours()));
    }
    openni.cleanUp();
    viewer.dispose();
    System.exit(0);
  }
  
  public static void main(String[] args) {
    new MainDriver();
  }
}
