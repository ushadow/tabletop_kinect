package edu.mit.yingyin.tabletop.app;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import edu.mit.yingyin.tabletop.DebugView;
import edu.mit.yingyin.tabletop.HandAnalyzer;
import edu.mit.yingyin.tabletop.OpenNIWrapper;
import edu.mit.yingyin.tabletop.ProcessPacket;
import edu.mit.yingyin.tabletop.Table;
import edu.mit.yingyin.tabletop.Tracker;
import edu.mit.yingyin.tabletop.Tracker.FingerEvent;
import edu.mit.yingyin.tabletop.Tracker.TrackerListener;

public class MainDriver {
  private class KeyController extends KeyAdapter {
    public void keyPressed(KeyEvent ke) {
      switch (ke.getKeyCode()) {
      case KeyEvent.VK_P:
        pause = !pause;
        break;
      case KeyEvent.VK_ESCAPE:
      case KeyEvent.VK_Q:
        debugView.dispose();
      default: 
        break;
      }
    }
  }
  
  private class TrackerController implements TrackerListener {
    @Override
    public void fingerPressed(FingerEvent fe) {
      debugView.drawCircle(fe.x, fe.y);
    }
  }
  
  private OpenNIWrapper openni;
  private DebugView debugView;
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
    
    debugView = new DebugView(depthWidth, depthHeight);
    debugView.addKeyListener(new KeyController());
    
    Table table = new Table();
    Tracker tracker = new Tracker(table);
    tracker.addListener(new TrackerController());
    
    while (debugView.isVisible()) {
      if (pause == true)
        continue;
      openni.waitAnyUpdateAll();
      openni.getDepthMap(packet.depthRawData);
      if (!table.isInitialized())
        table.init(packet.depthRawData, depthWidth, depthHeight);
      analyzer.analyzeData(packet);
      debugView.show(packet);
      tracker.update(packet.foreLimbsFeatures);
    }
    openni.cleanUp();
    analyzer.cleanUp();
    packet.cleanUp();
    debugView.cleanUp();
    System.exit(0);
  }
  
  public static void main(String[] args) {
      new MainDriver();
  }
}
