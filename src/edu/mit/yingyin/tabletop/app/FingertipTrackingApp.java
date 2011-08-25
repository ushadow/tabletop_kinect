package edu.mit.yingyin.tabletop.app;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import org.OpenNI.GeneralException;
import org.OpenNI.StatusException;

import edu.mit.yingyin.tabletop.DebugView;
import edu.mit.yingyin.tabletop.HandAnalyzer;
import edu.mit.yingyin.tabletop.OpenNIDevice;
import edu.mit.yingyin.tabletop.ProcessPacket;
import edu.mit.yingyin.tabletop.Table;
import edu.mit.yingyin.tabletop.Tracker;
import edu.mit.yingyin.tabletop.Tracker.FingerEvent;
import edu.mit.yingyin.tabletop.Tracker.TrackerListener;

public class FingertipTrackingApp {
  private class KeyController extends KeyAdapter {
    public void keyPressed(KeyEvent ke) {
      switch (ke.getKeyCode()) {
      case KeyEvent.VK_P:
        pause = !pause;
        break;
      case KeyEvent.VK_ESCAPE:
      case KeyEvent.VK_Q:
        debugView.hide();
        break;
      default: 
        break;
      }
    }
  }
  
  private String configFile = "config/config.xml";
  
  private class TrackerController implements TrackerListener {
    @Override
    public void fingerPressed(FingerEvent fe) {
      debugView.drawCircle(fe.x, fe.y);
    }
  }
  
  private OpenNIDevice openni;
  private DebugView debugView;
  private int depthWidth, depthHeight;
  private ProcessPacket packet;
  boolean pause = false;
  
  public FingertipTrackingApp() {
    System.out.println("java.library.path = " + 
                       System.getProperty("java.library.path"));
    try {
      openni = new OpenNIDevice(configFile);
    } catch (GeneralException e1) {
      System.err.println(e1.getMessage());
      System.exit(-1);
    }
    depthWidth = openni.depthWidth();
    depthHeight = openni.depthHeight();
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
      try {
        openni.waitDepthUpdateAll();
        openni.depthArray(packet.depthRawData);
      } catch (StatusException e) {
        System.err.println(e.getMessage());
        System.exit(-1);
      }
      if (!table.isInitialized())
        table.init(packet.depthRawData, depthWidth, depthHeight);
      analyzer.analyzeData(packet);
      debugView.show(packet);
      tracker.update(packet.foreLimbsFeatures);
    }
    openni.release();
    analyzer.cleanUp();
    packet.cleanUp();
    debugView.cleanUp();
    System.exit(0);
  }
  
  public static void main(String[] args) {
      new FingertipTrackingApp();
  }
}
