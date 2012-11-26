package edu.mit.yingyin.tabletop.models;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;

import edu.mit.yingyin.tabletop.models.ProcessPacket.ForelimbFeatures;

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
  
  /**
   * @param packet
   * @return filtered fingertips in the same order as the list of
   *    <code>ForelimbFeatures</code>.
   */
  public List<Point3f> filter(ProcessPacket packet) {
    List<Point3f> res = new ArrayList<Point3f>();
    
    // Hack(yingyin): only considers one forelimb and one fingertip.
    // TODO(yingyin): consider all forelimbs and all fingertips.
    if (!hasFingertip(packet)) {
      reset();
      return null;
    }
    
    ForelimbFeatures ff = packet.forelimbFeatures.get(0);
    Point3f tip = ff.fingertips.get(0).value;
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
    res.add(s2);
    
    return res;
  }
  
  private boolean hasFingertip(ProcessPacket packet) {
    return !packet.forelimbFeatures.isEmpty() && 
        hasFingertip(packet.forelimbFeatures.get(0));
  }
  
  private boolean hasFingertip(ForelimbFeatures ff) {
    return ff.fingertips.size() > 0;
  }
  
  private void reset() {
    s1 = null;
    s2 = null;
    b = null;
  }
}
