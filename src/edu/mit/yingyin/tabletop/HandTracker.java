package edu.mit.yingyin.tabletop;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import edu.mit.yingyin.tabletop.ProcessPacket.ForelimbModel;
import edu.mit.yingyin.tabletop.ProcessPacket.ForelimbModel.ValConfiPair;

/**
 * <code>HandTracker</code> tracks hand events.
 * @author yingyin
 *
 */
public class HandTracker {
  public static interface HandTrackerListener {
    public void fingerPressed(FingerEvent fe);
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
   * Updates forelimb information.
   * @param forelimbs information for all the forlimbs detected.
   * @param frameID frame ID for the current update.
   */
  public void update(List<ForelimbModel> forelimbs, int frameID) {
    for (ForelimbModel forelimb : forelimbs) 
      for (ValConfiPair<Point3f> tip : forelimb.fingertips)
        if (Math.abs(tip.value.z - 
            table.getHeight((int)tip.value.x, (int)tip.value.y)) <= 5) {
          for (HandTrackerListener l : listeners) 
            l.fingerPressed(new FingerEvent(tip.value, frameID));
        }
  }
  
  public void addListener(HandTrackerListener l) {
    listeners.add(l);
  }
}
