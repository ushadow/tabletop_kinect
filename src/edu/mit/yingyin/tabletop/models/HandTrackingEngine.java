package edu.mit.yingyin.tabletop.models;

import java.awt.Point;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.OpenNI.GeneralException;

import edu.mit.yingyin.tabletop.models.HandTracker.IHandEventListener;

import rywang.util.ObjectIO;

public class HandTrackingEngine {
  private OpenNIDevice openni;
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
    packet = new ProcessPacket(depthWidth, depthHeight);

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
      e.printStackTrace();
      System.exit(-1);
    }

    analyzer.analyzeData(packet);
    
    tracker.update(packet.forelimbs, packet.depthFrameID);
  }
}