package edu.mit.yingyin.tabletop;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;

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
  public static interface IHandEventListener {
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

  private List<IHandEventListener> listeners = 
      new ArrayList<IHandEventListener>();
  
  /**
   * Updates forelimbs information and generates events.
   * @param forelimbs information for all the forlimbs detected.
   * @param frameID frame ID for the current update.
   */
  public void update(List<Forelimb> forelimbs, int frameID) {
    List<FingerEvent> fingerEventList = noFilter(forelimbs, frameID);
    if (!fingerEventList.isEmpty()) {
      for (IHandEventListener l : listeners) 
        l.fingerPressed(fingerEventList);
    }
  }
  
  public void addListener(IHandEventListener l) {
    listeners.add(l);
  }

  private List<FingerEvent> noFilter(List<Forelimb> forelimbs, int frameID) {
    List<FingerEvent> fingerEventList = new ArrayList<FingerEvent>();
    for (Forelimb forelimb : forelimbs) 
      for (Point3f tip : forelimb.filteredFingertips)
        fingerEventList.add(new FingerEvent(tip, frameID));
          return fingerEventList;
  }

  private List<FingerEvent> filterPressed(List<Forelimb> forelimbs, 
                                          int frameID) {
    List<FingerEvent> fingerEventList = new ArrayList<FingerEvent>();
    for (Forelimb forelimb : forelimbs)
      for (Point3f tip : forelimb.filteredFingertips) {
        float tableDepth = Table.instance().depthAt((int)tip.x, (int)tip.y);
        if (tip.z + Hand.FINGER_THICKNESS < )
      }
  }
}
