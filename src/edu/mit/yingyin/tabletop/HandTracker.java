package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.CV_32FC1;
import static com.googlecode.javacv.cpp.opencv_core.CV_RAND_NORMAL;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateMat;
import static com.googlecode.javacv.cpp.opencv_core.cvRNG;
import static com.googlecode.javacv.cpp.opencv_core.cvRandArr;
import static com.googlecode.javacv.cpp.opencv_core.cvRealScalar;
import static com.googlecode.javacv.cpp.opencv_core.cvSetIdentity;
import static com.googlecode.javacv.cpp.opencv_core.cvZero;
import static com.googlecode.javacv.cpp.opencv_video.cvCreateKalman;
import static com.googlecode.javacv.cpp.opencv_video.cvKalmanCorrect;
import static com.googlecode.javacv.cpp.opencv_video.cvKalmanPredict;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvRNG;
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
  
  /**
   * Transition matrix describing relationship between model parameters at step
   * k and at step k + 1 (the dynamics of the model).
   */
  private static final float[] F = {1, 0, 1, 0, 
                                    0, 1, 0, 1,
                                    0, 0, 1, 0,
                                    0, 0, 0, 1};

  private Table table;
  private List<HandTrackerListener> listeners = 
      new ArrayList<HandTrackerListener>();
  
  private CvRNG rng = cvRNG(0);
  private CvKalman kalman;
  /**
   * Measurements at step k: (x, y).
   */
  private CvMat zk = cvCreateMat(2, 1, CV_32FC1);
  private boolean initialized = false;
  
  public HandTracker(Table table) { 
    this.table = table; 
    kalman = cvCreateKalman(4, 2, 0);
    
    // Initializes initial state with random guess.
    cvRandArr(rng, kalman.state_post(), CV_RAND_NORMAL, cvRealScalar(0), 
              cvRealScalar(0.1));
    // Initializes Kalman filter parameters.
    FloatBuffer fb = kalman.transition_matrix().getFloatBuffer();
    fb.rewind();
    fb.put(F);

    cvSetIdentity(kalman.measurement_matrix(), cvRealScalar(1));
    cvSetIdentity(kalman.process_noise_cov(), cvRealScalar(1));
    cvSetIdentity(kalman.measurement_noise_cov(), cvRealScalar(1e-5));
  }
  
  /**
   * Updates forelimbs information and generates events.
   * @param forelimbs information for all the forlimbs detected.
   * @param frameID frame ID for the current update.
   */
  public void update(List<Forelimb> forelimbs, int frameID) {
    List<FingerEvent> fingerEventList = kalmanFilter(forelimbs, frameID);
    if (!fingerEventList.isEmpty()) {
      for (HandTrackerListener l : listeners) 
        l.fingerPressed(fingerEventList);
    }
  }
  
  private List<FingerEvent> noFilter(List<Forelimb> forelimbs, int frameID) {
    List<FingerEvent> fingerEventList = new ArrayList<FingerEvent>();
    for (Forelimb forelimb : forelimbs) 
      for (ValConfiPair<Point3f> tip : forelimb.fingertips)
          fingerEventList.add(new FingerEvent(tip.value, frameID));
    return fingerEventList;
  }
  
  private List<FingerEvent> kalmanFilter(List<Forelimb> forelimbs, int frameID) 
  {
    List<FingerEvent> fingerEventList = new ArrayList<FingerEvent>();
    for (Forelimb forelimb : forelimbs) {
      if (forelimb.fingertips.isEmpty()) {
        resetKalman();
      } else if (initialized) {
        Point3f tip = filter(forelimb);
        if (tip != null)
          fingerEventList.add(new FingerEvent(tip, frameID));
        } else {
          Point3f tip = findBestPoint(forelimb.fingertips); 
          initKalman(tip.x, tip.y);
          fingerEventList.add(new FingerEvent(tip, frameID));
      }
    }
    return fingerEventList;
  }
  
  public void addListener(HandTrackerListener l) {
    listeners.add(l);
  }
  
  private Point3f findBestPoint(List<ValConfiPair<Point3f>> fingertips) {
    float bestY = table.getHeight();
    Point3f bestPoint = null;
    for (ValConfiPair<Point3f> tip : fingertips) {
      if (tip.value.y < bestY) {
        bestY = tip.value.y;
        bestPoint = tip.value;
      }
    }
    return bestPoint;
  }
  
  private void resetKalman() {
    initialized = false;
  }
  
  private void initKalman(float x, float y) {
    kalman.state_post().put(x, y, 0, 0);
    cvSetIdentity(kalman.error_cov_post(), cvRealScalar(1));
    initialized = true;
  }

  private Point3f filter(Forelimb forelimb) {
    Point3f closest = null;
    float minDistance2 = Float.MAX_VALUE;
    
    // Predicted point position.
    CvMat yk = cvKalmanPredict(kalman, null);
    Point2f p1 = new Point2f((float)yk.get(0), (float)yk.get(1));
    
    for (ValConfiPair<Point3f> tip : forelimb.fingertips) {
      Point2f p2 = new Point2f(tip.value.x, tip.value.y);
      float distance2 = p1.distanceSquared(p2);
      if (distance2 < minDistance2) {
        minDistance2 = distance2;
        closest = tip.value;
      }
    }
    
    if (closest != null) {
      zk.put(closest.x, closest.y);
      CvMat statePost = cvKalmanCorrect(kalman, zk);
      return new Point3f((float)statePost.get(0), (float)statePost.get(1), 
          closest.z);
    }
    
    return null;
  }
}
