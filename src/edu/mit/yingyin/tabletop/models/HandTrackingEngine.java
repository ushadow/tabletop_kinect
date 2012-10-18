package edu.mit.yingyin.tabletop.models;

import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.logging.Logger;

import org.OpenNI.GeneralException;

import edu.mit.yingyin.calib.CalibModel;
import edu.mit.yingyin.image.ImageConvertUtils;
import edu.mit.yingyin.tabletop.models.HandTracker.FingerEvent;

/**
 * Main interface to the hand tracking module that tracks the hand events and 
 * updates the hand event listeners with the events.
 * @author yingyin
 *
 */
public class HandTrackingEngine {
  /**
   * The listener interface for receiving finger events.
   * @author yingyin
   *
   */
  public static interface IHandEventListener {
    public void fingerPressed(List<FingerEvent> feList);
  }
  
  private static Logger logger = Logger.getLogger(
      HandTrackingEngine.class.getName());
  
  private FullOpenNIDevice openni;
  private int depthWidth, depthHeight;
  private ProcessPacket packet;
  private int prevDepthFrameID = -1;
  private HandTracker tracker;
  private HandAnalyzer analyzer;

  /**
   * Creates a new <code>HandTrackingEngine</code>.
   * 
   * @param openniConfigFile
   * @param calibrationFile
   * @param maxDepth
   * @throws GeneralException
   */
  public HandTrackingEngine(String openniConfigFile, 
      String calibrationFile, int maxDepth) throws GeneralException {
    
    openni = new FullOpenNIDevice(openniConfigFile);
    
    depthWidth = openni.getDepthWidth();
    depthHeight = openni.getDepthHeight();
    analyzer = new HandAnalyzer(depthWidth, depthHeight, maxDepth);
    packet = new ProcessPacket(depthWidth, depthHeight, maxDepth, this);

    tracker = new HandTracker(new CalibModel(calibrationFile));
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
  
  public void removeListener(IHandEventListener l) {
    tracker.removeListener(l);
  }
  
  public ProcessPacket packet() { return packet; }
  
  /**
   * Checks if all the frames are played.
   * @return true if all the frames are played.
   */
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
  
  public boolean isBgInitialize() {
    return analyzer.isBgInitialized();
  }
  
  public FloatBuffer aveBg() {
    return analyzer.aveBg();
  }
  
  public int aveBgWidth() {
    return analyzer.aveBgWidth();
  }
  
  public FloatBuffer diffBg() {
    return analyzer.diffBg();
  }
  
  public int diffBgWidth() {
    return analyzer.diffBgWidth();
  }
}