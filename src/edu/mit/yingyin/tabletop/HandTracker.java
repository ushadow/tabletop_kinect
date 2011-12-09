package edu.mit.yingyin.tabletop;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;

import edu.mit.yingyin.tabletop.Forelimb.ValConfiPair;

/**
 * <code>HandTracker</code> tracks hand events.
 * @author yingyin
 *
 */
public class HandTracker {
  /**
   * The listener interface for recieving finger events.
   * @author yingyin
   *
   */
  public static interface HandTrackerListener {
    public void fingerPressed(List<FingerEvent> feList);
  }
  
  public static class FingerEvent {
    public int frameID;
    public Point3f fingertip;
    public FingerEvent(Point3f fingertip, int frameID) { 
      this.fingertip = fingertip;
      this.frameID = frameID;
    }
  }
  
  private Table table;
  private List<HandTrackerListener> listeners = 
      new ArrayList<HandTrackerListener>();
  
  public HandTracker(Table table) { this.table = table; }
  
  /**
   * Updates forelimbs information and generates events.
   * @param forelimbs information for all the forlimbs detected.
   * @param frameID frame ID for the current update.
   */
  public void update(List<Forelimb> forelimbs, int frameID) {
    List<FingerEvent> fingerEventList = new ArrayList<FingerEvent>();
    for (Forelimb forelimb : forelimbs) 
      for (ValConfiPair<Point3f> tip : forelimb.fingertips)
        if (Math.abs(tip.value.z - 
            table.getHeight((int)tip.value.x, (int)tip.value.y)) <= 5) {
          fingerEventList.add(new FingerEvent(tip.value, frameID));
        }
    if (!fingerEventList.isEmpty()) {
      for (HandTrackerListener l : listeners) 
        l.fingerPressed(fingerEventList);
    }
  }
  
  public void addListener(HandTrackerListener l) {
    listeners.add(l);
  }
}
