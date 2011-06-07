package edu.mit.yingyin.tabletop;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import edu.mit.yingyin.tabletop.ProcessPacket.DebugFrame;

public class MainDriver {
  private class KeyController extends KeyAdapter {
    public void keyPressed(KeyEvent ke) {
      switch (ke.getKeyChar()) {
      case 'p':
        pause = !pause;
        break;
      default: 
        break;
      }
    }
  }
  private OpenNIWrapper openni;
  private DebugFrame debugFrame;
  private int depthWidth, depthHeight;
  private ProcessPacket packet;
  boolean pause = false;
  
  public MainDriver() {
    System.out.println("java.library.path = " + 
                       System.getProperty("java.library.path"));
    openni = new OpenNIWrapper();
    openni.initFromXmlFile("config/config.xml");
    depthWidth = openni.getDepthWidth();
    depthHeight = openni.getDepthHeight();
    HandProcessor processor = new HandProcessor(depthWidth, depthHeight);
    packet = new ProcessPacket(depthWidth, depthHeight);
    
    debugFrame = new DebugFrame(depthWidth, depthHeight);
    debugFrame.addKeyListener(new KeyController());
    
    while (debugFrame.isVisible()) {
      if (pause == true)
        continue;
      openni.waitAnyUpdateAll();
      openni.getDepthMap(packet.depthRawData);
      processor.processData(packet);
      debugFrame.show(packet);
    }
    openni.cleanUp();
    processor.cleanUp();
    packet.cleanUp();
    debugFrame.cleanUp();
    System.exit(0);
  }
  
  public static void main(String[] args) {
      new MainDriver();
  }
}
