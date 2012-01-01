package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.CV_32FC1;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateMat;
import static com.googlecode.javacv.cpp.opencv_video.cvCreateKalman;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_video.CvKalman;

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
  
  private static final float[] F = {1, 0, 1, 0, 
                                    0, 1, 0, 1,
                                    0, 0, 1, 0,
                                    0, 0, 0, 1};

  private Table table;
  private List<HandTrackerListener> listeners = 
      new ArrayList<HandTrackerListener>();
  
  private CvKalman kalman = cvCreateKalman(4, 2, 0);
  private CvMat xk = cvCreateMat(4, 1, CV_32FC1);
  private CvMat wk = cvCreateMat(4, 1, CV_32FC1);
  private CvMat zk = cvCreateMat(2, 1, CV_32FC1);
  
  public HandTracker(Table table) { 
    this.table = table; 
    
    FloatBuffer fb = kalman.transition_matrix().getFloatBuffer();
    fb.put(F);
    
    
  }
  
  /**
   * Updates forelimbs information and generates events.
   * @param forelimbs information for all the forlimbs detected.
   * @param frameID frame ID for the current update.
   */
  public void update(List<Forelimb> forelimbs, int frameID) {
    List<FingerEvent> fingerEventList = new ArrayList<FingerEvent>();
    for (Forelimb forelimb : forelimbs) 
      for (ValConfiPair<Point3f> tip : forelimb.fingertips)
          fingerEventList.add(new FingerEvent(tip.value, frameID));
    if (!fingerEventList.isEmpty()) {
      for (HandTrackerListener l : listeners) 
        l.fingerPressed(fingerEventList);
    }
  }
  
  private void filter() {
    
  }
  
  public void addListener(HandTrackerListener l) {
    listeners.add(l);
  }
}
