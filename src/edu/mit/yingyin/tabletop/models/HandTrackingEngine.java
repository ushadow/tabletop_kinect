package edu.mit.yingyin.tabletop.models;

import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import org.OpenNI.GeneralException;

import edu.mit.yingyin.image.ImageConvertUtils;
import edu.mit.yingyin.tabletop.models.HandTracker.IHandEventListener;

/**
 * Main interface to the handtracking module that tracks the hand events and 
 * updates the hand event listeners with the events.
 * @author yingyin
 *
 */
public class HandTrackingEngine {
  private static Logger logger = Logger.getLogger(
      HandTrackingEngine.class.getName());
  
  private FullOpenNIDevice openni;
  private int depthWidth, depthHeight;
  private ProcessPacket packet;
  private int prevDepthFrameID = -1;
  private HandTracker tracker;
  private HandAnalyzer analyzer;

  public HandTrackingEngine(String openniConfigFile, 
      String calibrationFile) throws GeneralException {
    
    openni = new FullOpenNIDevice(openniConfigFile);
    
    depthWidth = openni.getDepthWidth();
    depthHeight = openni.getDepthHeight();
    analyzer = new HandAnalyzer(depthWidth, depthHeight);
    packet = new ProcessPacket(depthWidth, depthHeight, this);

    tracker = new HandTracker(new CalibrationExample(calibrationFile));
  }
  
  public int depthWidth() { return depthWidth; }
  
  public int depthHeight() { return depthHeight; }
  
  public void release() {
    openni.release();
    analyzer.release();
    packet.release();
  }

  public void addListener(IHandEventListener l) {
    tracker.addListener(l);
  }
  
  public ProcessPacket packet() { return packet; }
  
  public boolean isDone() {
    return packet.depthFrameID < prevDepthFrameID;
  }
  
  public void step() {
    try {
      openni.waitDepthUpdateAll();
      openni.getDepthArray(packet.depthRawData);
      prevDepthFrameID = packet.depthFrameID;
      packet.depthFrameID = openni.getDepthFrameID();
    } catch (Exception e) {
      logger.severe(e.getMessage());
      System.exit(-1);
    }

    analyzer.analyzeData(packet);
    
    tracker.update(packet.forelimbs, packet.depthFrameID);
  }
  
  public void getRgbImage(BufferedImage bi) throws GeneralException {
    ImageConvertUtils.byteBuffer2BufferedImage(openni.getImageBuffer(), bi);
  }
}