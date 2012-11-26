package edu.mit.yingyin.tabletop.models;

import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.logging.Logger;

import org.OpenNI.GeneralException;
import org.OpenNI.Point3D;
import org.OpenNI.StatusException;

import edu.mit.yingyin.calib.CalibModel;
import edu.mit.yingyin.image.ImageConvertUtils;
import edu.mit.yingyin.tabletop.models.HandTracker.FingerEvent;

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
  private Table table;

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
    analyzer = new HandAnalyzer(depthWidth, depthHeight, maxDepth, openni);
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

      analyzer.analyzeData(packet);
    
      if (table == null && analyzer.isBgInitialized()) {
        table = new Table(analyzer.aveBg(), analyzer.diffBg(), 
            analyzer.aveBgWidthStep(), analyzer.diffBgWidthStep(), depthWidth,
            depthHeight, openni);
      }
      tracker.update(packet.forelimbs, packet.depthFrameID);
    } catch (Exception e) {
      logger.severe(e.getMessage());
      e.printStackTrace();
      System.exit(-1);
    }
  }
  
  public void getRgbImage(BufferedImage bi) throws GeneralException {
    ImageConvertUtils.byteBuffer2BufferedImage(openni.getImageBuffer(), bi);
  }
  
  public boolean isBgInitialized() {
    return analyzer.isBgInitialized();
  }
  
  public FloatBuffer aveBg() {
    return analyzer.aveBg();
  }
  
  public int aveBgWidth() {
    return analyzer.aveBgWidthStep();
  }
  
  public FloatBuffer diffBg() {
    return analyzer.diffBg();
  }
  
  public int diffBgWidth() {
    return analyzer.diffBgWidthStep();
  }
  
  /**
   * Table is initialized when the background is initialized.
   * @return
   */
  public boolean isTableInitialized() {
    return isBgInitialized();
  }
  
  /**
   * @return the surface normal of the table if it is initialized, otherwise
   * returns null.
   */
  public Table table() {
    if (table == null) {
      logger.severe("Table is not initialized.");
      return null;
    }
    return table;
  }
  
  public Point3D[] convertProjectiveToRealWorld(Point3D[] points) {
    Point3D[] converted = null;
    try {
      converted = openni.convertProjectiveToRealWorld(points);
    } catch (StatusException se) {
      logger.severe(se.getMessage());
      System.exit(-1);
    }
    return converted;
  }
}