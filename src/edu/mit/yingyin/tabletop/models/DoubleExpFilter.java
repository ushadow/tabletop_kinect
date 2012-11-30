package edu.mit.yingyin.tabletop.models;

import javax.vecmath.Point3f;

public class DoubleExpFilter {

  private float alpha, beta;
  
  /**
   * s1 is the smoothed state in the previous frame.
   * s2 is the smoothed state in the current frame.
   */
  private Point3f s1, s2, b;
  
  public DoubleExpFilter(float alpha, float beta) {
    this.alpha = alpha;
    this.beta = beta;
    reset();
  }
  
  public void filter(ProcessPacket packet) {
    // Hack(yingyin): only considers one forelimb and one fingertip.
    // TODO(yingyin): consider all forelimbs and all fingertips.
    if (packet.forelimbs.isEmpty() || 
        packet.forelimbs.get(0).numFingertips() <= 0) {
      reset();
      return;
    }
    
    Forelimb forelimb = packet.forelimbs.get(0);
    Point3f tip = forelimb.getFingertips().get(0);
    s1 = s2;
    s2 = new Point3f(tip);
    Point3f temp = new Point3f();

    if (s1 != null && b != null) {
      temp.add(s1, b);
      s2.interpolate(temp, 1 - alpha);
      temp.sub(s2, s1);
      b.interpolate(temp, b, 1 - beta);
    } else if (s1 != null) {
      b = new Point3f();
      b.sub(s2, s1);
    }
    forelimb.filteredFingertips.add(s2);
  }
  
  private void reset() {
    s1 = null;
    s2 = null;
    b = null;
  }
}
