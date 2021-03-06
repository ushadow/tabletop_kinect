package edu.mit.yingyin.tabletop.controllers;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.OpenNI.GeneralException;

import edu.mit.yingyin.tabletop.models.HandTracker.DiecticEvent;
import edu.mit.yingyin.tabletop.models.HandTracker.ManipulativeEvent;
import edu.mit.yingyin.tabletop.models.HandTrackingEngine.IHandEventListener;
import edu.mit.yingyin.tabletop.models.ProcessPacket;
import edu.mit.yingyin.tabletop.views.ProcessPacketView;
import edu.mit.yingyin.tabletop.views.ProcessPacketView.Toggles;
import edu.mit.yingyin.util.FPSCounter;
import edu.mit.yingyin.util.Option;
import edu.mit.yingyin.util.Option.None;
import edu.mit.yingyin.util.Option.Some;

/**
 * Controls the interaction on the visualization for the ProcessPacket.
 * @author yingyin
 *
 */
public class ProcessPacketController extends KeyAdapter 
    implements IHandEventListener {
  public static enum Options {LABEL, CLASSIFICATION};
  
  private static final Logger LOGGER = Logger.getLogger(
      ProcessPacketController.class.getName());
  private FPSCounter fpsCounter;
  private Option<HashMap<Integer, List<Point>>> allLabels;
  private Option<HashMap<Integer, Integer>> classifications;
  private ProcessPacketView packetView;
  
  /**
   * Initializes the models and the view.
   * @param width width of the streamed data frame.
   * @param height height of the stream data frame.
   * @param lables ground truth label. Can be null.
   */
  @SuppressWarnings("unchecked")
  public ProcessPacketController(int width, int height, 
      HashMap<Options, Object> options) {
    allLabels = new None<HashMap<Integer, List<Point>>>();
    classifications = new None<HashMap<Integer, Integer>>();
    
    if (options != null) {
      Object o = options.get(Options.LABEL);
      if (o != null)
        allLabels = new Some<HashMap<Integer, List<Point>>>(
            (HashMap<Integer, List<Point>>) o);
      
      o = options.get(Options.CLASSIFICATION);
      if (o != null)
        classifications = new Some<HashMap<Integer, Integer>>(
            (HashMap<Integer, Integer>) o);
    }
    packetView = new ProcessPacketView(width, height);
    fpsCounter = new FPSCounter(ProcessPacketView.ANALYSIS_FRAME_TITLE, 
                                packetView.analysisFrame());
    packetView.addKeyListener(this);
  }
  
  public void showDepthImage(boolean show) {
    packetView.setToggle(Toggles.SHOW_DEPTH_VIEW,show);
  }
  
  public void keyPressed(KeyEvent ke) {
    switch (ke.getKeyCode()) {
    case KeyEvent.VK_B:
      packetView.toggle(Toggles.SHOW_BOUNDING_BOX);
      break;
    case KeyEvent.VK_D:
      packetView.toggle(Toggles.SHOW_CONVEXITY_DEFECTS);
      break;
    case KeyEvent.VK_F:
      // Shows all the detected fingertips.
      packetView.toggle(Toggles.SHOW_FINGERTIP);
      break;
    case KeyEvent.VK_H:
      packetView.toggle(Toggles.SHOW_HULL);
      break;
    case KeyEvent.VK_M:
      packetView.toggle(Toggles.SHOW_MORPHED);
      break;
    case KeyEvent.VK_L:
      packetView.toggle(Toggles.SHOW_LABELS);
      break;
    default: 
      break;
    }
  }
  
  /**
   * Shows different visualizations of the ProcessPacket.
   * @param packet
   * @throws GeneralException 
   */
  public void show(ProcessPacket packet) throws GeneralException {
    List<Point> labels = allLabels.isSome() ? 
        allLabels.value().get(packet.depthFrameID) : null;
    
    Integer classLabel = -1;
    if (classifications.isSome()) {
      classLabel = classifications.value().get(packet.depthFrameID);
      if (classLabel == null)
        classLabel = -1;
    }
    packetView.show(packet, labels, classLabel);
    fpsCounter.computeFPS();
  }
  
  public Rectangle getViewBounds() {
    return packetView.getBounds();
  }
  
  /**
   * Releases memory.
   */
  public void release() {
    packetView.release();
    LOGGER.info(ProcessPacketController.class.getName() + " released.");
  }
  
  public boolean isVisible() {
    return packetView.isVisible();
  }
  
  public void hide() {
    packetView.hide();
  }
  
  public void addKeyListener(KeyListener kl) {
    packetView.addKeyListener(kl);
  }
  
  /**
   * Draws a circle at (x, y).
   * @param x
   * @param y
   */
  public void drawCircle(int x, int y) {
    packetView.drawCircle(x, y);
  }
  
  public BufferedImage depthImage() {
    return packetView.depthImage();
  }

  @Override
  public void fingerPressed(List<ManipulativeEvent> feList) {
    
  }

  @Override
  public void fingerPointed(DiecticEvent de) {
    packetView.redrawIntersections(de.pointingLocationsW());
  }
}    