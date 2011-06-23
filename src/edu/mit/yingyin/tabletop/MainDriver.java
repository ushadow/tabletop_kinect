package edu.mit.yingyin.tabletop;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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
  private DebugFrames debugFrames;
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
    HandAnalyzer processor = new HandAnalyzer(depthWidth, depthHeight);
    packet = new ProcessPacket(depthWidth, depthHeight);
    
    debugFrames = new DebugFrames(depthWidth, depthHeight);
    debugFrames.addKeyListener(new KeyController());
    
    Table table = new Table();
    
    while (debugFrames.isVisible()) {
      if (pause == true)
        continue;
      openni.waitAnyUpdateAll();
      openni.getDepthMap(packet.depthRawData);
      if (!table.isInitialized())
        table.init(packet.depthRawData, depthWidth, depthHeight);
      processor.analyzeData(packet);
      debugFrames.show(packet);
    }
    openni.cleanUp();
    processor.cleanUp();
    packet.cleanUp();
    debugFrames.cleanUp();
    System.exit(0);
  }
  
  public static void main(String[] args) {
      new MainDriver();
  }
}
