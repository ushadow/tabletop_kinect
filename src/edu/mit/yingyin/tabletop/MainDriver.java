package edu.mit.yingyin.tabletop;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import edu.mit.yingyin.tabletop.ProcessPacket.DebugFrame;

public class MainDriver {
  private class Controller extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
      running = false;
    }
  }
  private OpenNIWrapper openni;
  private boolean running = true;
  private DebugFrame debugFrame;
  private int depthWidth, depthHeight;
  private ProcessPacket packet;
  
  public MainDriver() throws InterruptedException, InvocationTargetException {
    System.out.println("java.library.paht = " + 
                       System.getProperty("java.library.path"));
    openni = new OpenNIWrapper();
    openni.initFromXmlFile("config/config.xml");
    depthWidth = openni.getDepthWidth();
    depthHeight = openni.getDepthHeight();
    HandProcessor processor = new HandProcessor(depthWidth, depthHeight);
    packet = new ProcessPacket(depthWidth, depthHeight);
    
    debugFrame = new DebugFrame(depthWidth, depthHeight);
    
    while (debugFrame.isVisible()) {
      openni.waitAnyUpdateAll();
      openni.getDepthMap(packet.depthRawData);
      processor.processData(packet);
      debugFrame.show(packet);
    }
    openni.cleanUp();
    processor.cleanUp();
    packet.deallocate();
    debugFrame.dispose();
    System.exit(0);
  }
  
  public static void main(String[] args) {
    try {
      new MainDriver();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
