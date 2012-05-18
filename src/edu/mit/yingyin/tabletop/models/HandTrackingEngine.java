package edu.mit.yingyin.tabletop.models;

import java.awt.Point;	 
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.OpenNI.GeneralException;

import rywang.util.ObjectIO;
import edu.mit.yingyin.image.ImageConvertUtils;
import edu.mit.yingyin.tabletop.models.HandTracker.IHandEventListener;

/**
 * Tracks the hand events and updates the hand event listeners with the events.
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
  private HashMap<Integer, List<Point>> labels;
  private HandTracker tracker;
  private HandAnalyzer analyzer;

  @SuppressWarnings("unchecked")
  public HandTrackingEngine(String labelFile, String openniConfigFile, 
      String calibrationFile) throws GeneralException {
    
    try {
      if (labelFile != null)
        labels = (HashMap<Integer, List<Point>>) ObjectIO.readObject(labelFile);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.exit(-1);
    }
    
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
      
      if (labels != null)
        packet.labels = labels.get(packet.depthFrameID);
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