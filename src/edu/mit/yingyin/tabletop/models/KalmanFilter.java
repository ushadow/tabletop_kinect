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
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvRNG;
import com.googlecode.javacv.cpp.opencv_video.CvKalman;

import edu.mit.yingyin.tabletop.models.Forelimb.ValConfiPair;
import edu.mit.yingyin.tabletop.models.ProcessPacket.ForelimbFeatures;

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
  public List<Point3f> filter(ProcessPacket packet) {
    List<Point3f> res = new ArrayList<Point3f>();
    
    if (packet.forelimbFeatures.isEmpty()) {
      resetKalman();
      return null;
    } else {
      // Hack(ushadow): assumes one forelimb only.
      // TODO(ushadow): use multiple kalman filters for multiple forelimbs.
      ForelimbFeatures ff = packet.forelimbFeatures.get(0);
      if (initialized) {
        Point3f tip = filter(ff);
        if (tip != null)
          res.add(tip);
      } else if (ff.fingertips.size() > 0) {
        // Finds the best point to initialize Kalman filter.
        Point3f tip = findBestPoint(ff.fingertips); 
        initKalman(tip.x, tip.y);
        res.add(tip);
      }
    }
    return res;
  }
  
  /**
   * Returns the string the contains the values in the Kalman filter.
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
    // Initializes the posterior error covariance to the identity (this is 
    // required to guarantee the meaningfulness of the first iteration; it will
    // subsequently be overwritten.
    cvSetIdentity(kalman.error_cov_post(), cvRealScalar(1));
    initialized = true;
  }

  /**
   * Returns a new point filtered from all the fingetips.
   * @param forelimb the <code>Forelimb</code> that contains the fingertips.
   * @return a point that is most likely to be the fingertip based on the 
   *    updated model.
   */
  private Point3f filter(ForelimbFeatures ff) {
    Point3f closest = null;
    Point3f result = null;
    float minDistance2 = Float.MAX_VALUE;
    
    // Predicted point position.
    CvMat yk = cvKalmanPredict(kalman, null);
    float x = (float)yk.get(0);
    float y = (float)yk.get(1);
    Point2f p1 = new Point2f(x, y);
    
    for (ValConfiPair<Point3f> vcp : ff.fingertips) {
      Point3f tip = vcp.value;
      Point2f p2 = new Point2f(tip.x, tip.y);
      float distance2 = p1.distanceSquared(p2);
      if (distance2 < minDistance2) {
        minDistance2 = distance2;
        closest = tip;
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
   * Returns a new point which is the best candidate from all the fingertips in 
   * the forelimb.
   * @param forelimb the <code>Forelimb</code> that contains the fingertips.
   * @return a point that is most likely as the fingertip.
   */
  private Point3f findBestPoint(List<ValConfiPair<Point3f>> fingertips) {
    // Hack(yingyin): assumes the point that is nearest to the center of the 
    // of the image as the fintertip. Assumes hand is extended from the bottom 
    // of the image.
    // TODO(yingyin): need to consider different hand orientation.
    float bestY = height;
    Point3f bestPoint = null;
    for (ValConfiPair<Point3f> vcp : fingertips) {
      Point3f tip = vcp.value;
      if (tip.y < bestY) {
        bestY = tip.y;
        bestPoint = tip;
      }
    }
    return new Point3f(bestPoint);
  }
}
