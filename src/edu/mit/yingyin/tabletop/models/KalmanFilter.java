package edu.mit.yingyin.tabletop.models;

import static com.googlecode.javacv.cpp.opencv_core.CV_32FC1;
import static com.googlecode.javacv.cpp.opencv_core.CV_RAND_NORMAL;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateMat;
import static com.googlecode.javacv.cpp.opencv_core.cvRNG;
import static com.googlecode.javacv.cpp.opencv_core.cvRandArr;
import static com.googlecode.javacv.cpp.opencv_core.cvRealScalar;
import static com.googlecode.javacv.cpp.opencv_core.cvSetIdentity;
import static com.googlecode.javacv.cpp.opencv_video.cvCreateKalman;
import static com.googlecode.javacv.cpp.opencv_video.cvKalmanCorrect;
import static com.googlecode.javacv.cpp.opencv_video.cvKalmanPredict;

import java.nio.FloatBuffer;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvRNG;
import com.googlecode.javacv.cpp.opencv_video.CvKalman;

import edu.mit.yingyin.tabletop.models.Forelimb.ValConfiPair;

public class KalmanFilter {
  /**
   * Transition matrix describing relationship between model parameters at step
   * k and at step k + 1 (the dynamics of the model).
   */
  private static final float[] F = {1, 0, 1, 0, 
                                    0, 1, 0, 1,
                                    0, 0, 1, 0,
                                    0, 0, 0, 1};
  
  private CvRNG rng = cvRNG(0);
  private CvKalman kalman;
  /**
   * Measurements at step k: (x, y).
   */
  private CvMat zk = cvCreateMat(2, 1, CV_32FC1);
  private boolean initialized = false;
  /**
   * Depth image width and height.
   */
  private int height;
  
  public KalmanFilter(int width, int height) {
    this.height = height;
    kalman = cvCreateKalman(4, 2, 0);
    
    // Initializes initial state with random guess.
    cvRandArr(rng, kalman.state_post(), CV_RAND_NORMAL, cvRealScalar(0), 
              cvRealScalar(0.1));
    // Initializes Kalman filter parameters.
    FloatBuffer fb = kalman.transition_matrix().getFloatBuffer();
    fb.rewind();
    fb.put(F);

    cvSetIdentity(kalman.measurement_matrix(), cvRealScalar(1));
    // Process noise is associated with random events or forces that directly
    // affect the actual state of the system.
    CvMat Q = kalman.process_noise_cov();
    Q.put(0, 0, 1);
    Q.put(1, 1, 1);
    Q.put(2, 2, 10);
    Q.put(3, 3, 10);
    cvSetIdentity(kalman.measurement_noise_cov(), cvRealScalar(1));
  }
  
  /*
   * Filters the detected fingertips.
   */
  public void filter(ProcessPacket packet) {
    if (packet.forelimbs.isEmpty()) {
      resetKalman();
    } else {
      // Hack(ushadow): assumes one forelimb only.
      // TODO(ushadow): use multiple kalman filters for multiple forelimbs.
      for (Forelimb forelimb : packet.forelimbs) {
        if (initialized) {
          Point3f tip = filter(forelimb, packet);
          if (tip != null)
            forelimb.filteredFingertips.add(tip);
        } else if (!forelimb.fingertips.isEmpty()) {
          // Finds the best point to initialize Kalman filter.
          Point3f tip = findBestPoint(forelimb); 
          initKalman(tip.x, tip.y);
          forelimb.filteredFingertips.add(tip);
        }
      }
    }
  }
  
  /**
   * Returns the string the contains the values in the kalman filter.
   */
  public String toString() {
    StringBuffer sb = new StringBuffer("Kalman fiter:\n");
    sb.append("Post state:\n");
    sb.append(kalman.state_post());
    sb.append("\nProcess noise cov:\n");
    sb.append(kalman.process_noise_cov());
    sb.append("\nPost error cov:\n");
    sb.append(kalman.error_cov_post());
    return sb.toString();
  }
  
  private void resetKalman() {
    initialized = false;
  }
  
  private void initKalman(float x, float y) {
    // Initializes dx and dy to 0s.
    kalman.state_post().put(x, y, 0, 0);
    // Initializes the posterior error covariance to the identitiy (this is 
    // required to gurantee the meaningfulness of the first iteration; it will
    // subsequently be overwritten.
    cvSetIdentity(kalman.error_cov_post(), cvRealScalar(1));
    initialized = true;
  }

  /**
   * Returns a new point filtered from all the fingetips.
   * @param forelimb the <code>Forelimb</code> that contains the fingertips.
   * @param packet the <code>ProcessPacket</code> that contains all the 
   *    processed information.
   * @return a point that is most likely to be the fingertip based on the 
   *    updated model.
   */
  private Point3f filter(Forelimb forelimb, ProcessPacket packet) {
    Point3f closest = null;
    Point3f result = null;
    float minDistance2 = Float.MAX_VALUE;
    
    // Predicted point position.
    CvMat yk = cvKalmanPredict(kalman, null);
    float x = (float)yk.get(0);
    float y = (float)yk.get(1);
    Point2f p1 = new Point2f(x, y);
    
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
      result = new Point3f((float)statePost.get(0), (float)statePost.get(1), 
                           closest.z);
    } 
    return result;
  }
  
  /**
   * Returns a new point which is the best canditate from all the fingertips in 
   * the forelimb.
   * @param forelimb the <code>Forelimb</code> that contains the fingertips.
   * @return a point that is most likely as the fingertip.
   */
  private Point3f findBestPoint(Forelimb forelimb) {
    // Hack(ushadow): assumes the point that is nearest to the center of the 
    // of the image as the fintertip. Assumes hand is extended from the bottom 
    // of the image.
    // TODO(ushadow): need to consider different hand orientation.
    float bestY = height;
    Point3f bestPoint = null;
    for (ValConfiPair<Point3f> tip : forelimb.fingertips) {
      if (tip.value.y < bestY) {
        bestY = tip.value.y;
        bestPoint = tip.value;
      }
    }
    return new Point3f(bestPoint);
  }
}
