package edu.mit.yingyin.tabletop.models;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import edu.mit.yingyin.calib.CalibModel;
import edu.mit.yingyin.tabletop.models.HandTracker.FingerEvent.FingerEventType;
import edu.mit.yingyin.tabletop.models.HandTrackingEngine.IHandEventListener;

/**
 * <code>HandTracker</code> tracks hand events based on estimated hand model 
 * parameters, and maintains a reference to the table model.
 * @author yingyin
 *
 */
public class HandTracker {
  /**
   * An event generated by a finger.
   * @author yingyin
   *
   */
  public static class FingerEvent {
    public enum FingerEventType {PRESSED, RELEASED};
    
    public int frameID;
    /**
     * Position on depth image and position on display.
     */
    public Point3f posImage;
    public Point2f posDisplay;
    public FingerEventType type;
    
    public FingerEvent(Point3f posImage, Point2f posDisplay,
        int frameID, FingerEventType type) { 
      this.posImage = posImage;
      this.posDisplay = posDisplay;
      this.frameID = frameID;
      this.type = type;
    }
    
    public String toString() {
      return String.format("Image positon = " + posImage + 
          " Display position = " + posDisplay + "\n");
    }
  }

  private static final int DEBOUNCE_COUNT = 3;

  private List<IHandEventListener> listeners = 
      new ArrayList<IHandEventListener>();
  
  /** Counts the duration of contact or noncontact. */
  private int pressedCounter = 0, releasedCounter = 0;
  /** True if finger is pressed, false otherwise. */
  private boolean pressed = false;
  private CalibModel calibExample;
  
  public HandTracker(CalibModel calibExample) {
    this.calibExample = calibExample;
  }
  
  /**
   * Updates forelimbs information and generates events.
   * @param forelimbs information for all the forelimbs detected.
   * @param frameID frame ID for the current update.
   */
  public void update(List<Forelimb> forelimbs, int frameID) {
    List<FingerEvent> fingerEventList = noFilter(forelimbs, frameID);
    if (fingerEventList != null && !fingerEventList.isEmpty()) {
      for (IHandEventListener l : listeners) 
        l.fingerPressed(fingerEventList);
    }
  }
  
  public void addListener(IHandEventListener l) {
    listeners.add(l);
  }
  
  public void removeListener(IHandEventListener l) {
    listeners.remove(l);
  }

  /**
   * No filtering of any detected fingertips.
   * @param forelimbs a list of forelimbs.
   * @param frameID frame ID.
   * @return a list of finger events.
   */
  public List<FingerEvent> noFilter(List<Forelimb> forelimbs, int frameID) {
    List<FingerEvent> fingerEventList = new ArrayList<FingerEvent>();
    for (Forelimb forelimb : forelimbs) 
      for (Point3f tip : forelimb.getFingertipsI())
        fingerEventList.add(createFingerEvent(tip, frameID, 
                                            FingerEventType.PRESSED));
    return fingerEventList;
  }

  /**
   * Filters out finger pressed events.
   * @param forelimbs
   * @param frameID
   * @return
   */
  public List<FingerEvent> filterPressed(List<Forelimb> forelimbs, 
                                         int frameID) {
    InteractionSurface table = InteractionSurface.instance();
    List<FingerEvent> fingerEventList = new ArrayList<FingerEvent>();
    
    if (table == null) 
      return fingerEventList;
    
    for (Forelimb forelimb : forelimbs) {
      for (Point3f tip : forelimb.getFingertipsI()) {
        float tipDepth = tip.z + Hand.FINGER_THICKNESS; 
        boolean inContact = table.isInContact((int)tip.x, (int)tip.y, tipDepth);
        if (inContact) {
          pressedCounter++;
          releasedCounter = 0;
        } else {
          releasedCounter++;
          pressedCounter = 0;
        }
        if (pressedCounter == DEBOUNCE_COUNT && !pressed) {
          pressed = true;
          fingerEventList.add(createFingerEvent(tip, frameID, 
                                                FingerEventType.PRESSED));
        } else if (releasedCounter == DEBOUNCE_COUNT && pressed) {
          pressed = false;
          fingerEventList.add(createFingerEvent(tip, frameID, 
                                                FingerEventType.RELEASED));
        }
      }
    }
    return fingerEventList;
  }
  
  private FingerEvent createFingerEvent(Point3f posImage, int frameID, 
      FingerEventType type) {
    return new FingerEvent(posImage, 
        calibExample.imageToDisplayCoords(posImage.x, posImage.y),
        frameID, type);
  }
  
}
