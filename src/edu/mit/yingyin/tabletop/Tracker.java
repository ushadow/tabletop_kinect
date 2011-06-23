package edu.mit.yingyin.tabletop;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;

import edu.mit.yingyin.tabletop.ProcessPacket.ForelimbModel;
import edu.mit.yingyin.tabletop.ProcessPacket.ForelimbModel.ValConfiPair;

public class Tracker {
  public static interface TrackerListener {
    public void fingerPressed(FingerEvent fe);
  }
  
  public static class FingerEvent {
    public int x, y;
    public FingerEvent(int x, int y) { this.x = x; this.y = y; }
  }
  
  private Table table;
  private List<TrackerListener> listeners = 
    new ArrayList<TrackerListener>();
  
  public Tracker(Table table) { this.table = table; }
  
  public void update(List<ForelimbModel> forelimbs) {
    for (ForelimbModel forelimb : forelimbs) 
      for (ValConfiPair<Point3f> tip : forelimb.fingertips)
        if (Math.abs(tip.value.z - 
            table.getHeight((int)tip.value.x, (int)tip.value.y)) <= 3) {
          for (TrackerListener l : listeners)
            l.fingerPressed(new FingerEvent((int)tip.value.x, 
                                            (int)tip.value.y));
        }
  }
  
  public void addListener(TrackerListener l) {
    listeners.add(l);
  }
}
