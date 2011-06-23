package edu.mit.yingyin.tabletop;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import edu.mit.yingyin.tabletop.Tracker.FingerEvent;
import edu.mit.yingyin.tabletop.Tracker.TrackerListener;

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
  
  private class TrackerController implements TrackerListener {
    @Override
    public void fingerPressed(FingerEvent fe) {
      debugFrames.drawCircle(fe.x, fe.y);
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
    HandAnalyzer analyzer = new HandAnalyzer(depthWidth, depthHeight);
    packet = new ProcessPacket(depthWidth, depthHeight);
    
    debugFrames = new DebugFrames(depthWidth, depthHeight);
    debugFrames.addKeyListener(new KeyController());
    
    Table table = new Table();
    Tracker tracker = new Tracker(table);
    tracker.addListener(new TrackerController());
    
    while (debugFrames.isVisible()) {
      if (pause == true)
        continue;
      openni.waitAnyUpdateAll();
      openni.getDepthMap(packet.depthRawData);
      if (!table.isInitialized())
        table.init(packet.depthRawData, depthWidth, depthHeight);
      analyzer.analyzeData(packet);
      debugFrames.show(packet);
      tracker.update(packet.foreLimbsFeatures);
    }
    openni.cleanUp();
    analyzer.cleanUp();
    packet.cleanUp();
    debugFrames.cleanUp();
    System.exit(0);
  }
  
  public static void main(String[] args) {
      new MainDriver();
  }
}
