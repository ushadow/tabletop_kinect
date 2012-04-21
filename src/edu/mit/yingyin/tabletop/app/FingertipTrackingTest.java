package edu.mit.yingyin.tabletop.app;

import org.OpenNI.GeneralException;

import edu.mit.yingyin.tabletop.HandEventsController;
import edu.mit.yingyin.tabletop.models.FullOpenNIDevice;
import edu.mit.yingyin.tabletop.models.OpenNIDevice;
import edu.mit.yingyin.tabletop.models.ProcessPacket;

public class FingertipTrackingTest {
  private static String MAIN_DIR = "/afs/csail/u/y/yingyin/research/kinect/";
  private static String CONFIG_FILE = MAIN_DIR + "config/config.xml";
  
  public static void main(String[] args) {
    new HandEventsController();
  }
  
  private OpenNIDevice openni;
  private boolean stop = false;
  private ProcessPacket packet;
  private int depthWidth, depthHeight;
  private int prevDepthFrameID;
  
  public FingertipTrackingTest() {
    try {
      openni = new FullOpenNIDevice(CONFIG_FILE);
    } catch (GeneralException ge) {
      ge.printStackTrace();
      System.exit(-1);
    }
    
    depthWidth = openni.getDepthWidth();
    depthHeight = openni.getDepthHeight();
    packet = new ProcessPacket(depthWidth, depthHeight);
    
    while (!stop)
      step();
  }
  
  private void step() {
    try {
      openni.waitDepthUpdateAll();
      openni.getDepthArray(packet.depthRawData);
      prevDepthFrameID = packet.depthFrameID;
      packet.depthFrameID = openni.getDepthFrameID();
    } catch (Exception e) {
      
    }
  }
}
