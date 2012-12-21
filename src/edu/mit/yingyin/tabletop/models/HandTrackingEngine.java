package edu.mit.yingyin.tabletop.models;

import java.util.List;
import java.util.logging.Logger;

import org.OpenNI.GeneralException;

import edu.mit.yingyin.calib.CalibModel;
import edu.mit.yingyin.tabletop.models.HandTracker.DiecticEvent;
import edu.mit.yingyin.tabletop.models.HandTracker.ManipulativeEvent;

/**
 * Main interface to the hand tracking backend module that tracks the hand 
 * events and updates the hand event listeners with the events.
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
    public void fingerPressed(List<ManipulativeEvent> feList);
    public void fingerPointed(DiecticEvent de);
  }
  
  private static Logger logger = Logger.getLogger(
      HandTrackingEngine.class.getName());
  
  private final OpenNIDevice openni;
  private int depthWidth, depthHeight;
  private int prevDepthFrameID = -1, currentDepthFrameID = -1;
  private HandTracker tracker;
  private ForelimbFeatureDetector analyzer;

  /**
   * Creates a new <code>HandTrackingEngine</code>.
   * 
   * @param openniConfigFile
   * @param calibrationFile
   * @throws GeneralException
   */
  public HandTrackingEngine(String openniConfigFile, 
      String calibrationFile) throws GeneralException {
    
    openni = new OpenNIDevice(openniConfigFile);
    
    depthWidth = openni.getDepthWidth();
    depthHeight = openni.getDepthHeight();
    analyzer = new ForelimbFeatureDetector(depthWidth, depthHeight, openni);

    tracker = new HandTracker(new CalibModel(calibrationFile), openni);
  }
  
  public int depthWidth() { return depthWidth; }
  
  public int depthHeight() { return depthHeight; }
  
  public void release() {
    openni.release();
    analyzer.release();
  }

  public void addHandEventListener(IHandEventListener l) {
    tracker.addListener(l);
  }
  
  public void removeListener(IHandEventListener l) {
    tracker.removeListener(l);
  }
  
  /**
   * Checks if all the frames are played.
   * @return true if all the frames are played.
   */
  public boolean isDone() {
    return currentDepthFrameID < prevDepthFrameID;
  }
  
  /**
   * Steps one frame.
   * @return a new {@code ProcessPacket}. The caller should release the memory 
   *    of the returned object when finished using the object.
   */
  public ProcessPacket step() {
    ProcessPacket packet = null;
    try {
      openni.waitDepthUpdateAll();
      packet = new ProcessPacket(depthWidth, depthHeight, openni);
      openni.getDepthArray(packet.depthRawData);
      prevDepthFrameID = currentDepthFrameID;
      packet.depthFrameID = openni.getDepthFrameID();
      currentDepthFrameID = packet.depthFrameID;

      analyzer.detect(packet);
      
      if (interactionSurfaceInitialized())
        tracker.update(packet.forelimbs, packet.depthFrameID);
    } catch (Exception e) {
      logger.severe(e.getMessage());
      e.printStackTrace();
      System.exit(-1);
    }
    return packet;
  }
  
  public boolean interactionSurfaceInitialized() {
    return InteractionSurface.instanceInitialized();
  }
  
  public InteractionSurface interactionSurface() {
    return InteractionSurface.instance();
  }
}