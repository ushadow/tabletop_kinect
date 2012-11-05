package edu.mit.yingyin.tabletop.models;

import static com.googlecode.javacv.cpp.opencv_core.CV_32FC3;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_DIST_L2;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFitLine;

import java.nio.FloatBuffer;
import java.util.logging.Logger;

import javax.vecmath.Vector3f;

import org.OpenNI.GeneralException;
import org.OpenNI.Point3D;

import com.googlecode.javacv.cpp.opencv_core.CvMat;

import edu.mit.yingyin.util.VectorUtil;

/**
 * Model of a tabletop.
 * @author yingyin
 *
 */
public class Table {
  private static final Logger logger = Logger.getLogger(Table.class.getName());
  private static final int DIFF_SCALE = 5;
  
  /**
   * Average depth and depth difference in mm.
   */
  private FloatBuffer avg, diff;
  private int avgWidthStep, diffWidthStep, width, height;
  private boolean initialized = false;
  private Vector3f surfaceNormal;

  /**
   * Initializes the table statistics.
   * @param avg
   * @param diff
   * @param avgWidthStep
   * @param diffWidthStep
   * @param scale used to scale the depth value.
   */
  public Table(FloatBuffer avg, FloatBuffer diff, int avgWidthStep,
               int diffWidthStep, int width, int height) {
    this.avg = avg;
    this.diff = diff;
    this.avgWidthStep = avgWidthStep;
    this.diffWidthStep = diffWidthStep;
    this.width = width;
    this.height = height;
    computeSurfaceNormal();
    initialized = true;
  }
  
  public Table(Background background) {
    this(background.avgBuffer(), background.diffBuffer(), 
         background.avgBufferWidthStep(), background.diffBufferWidthStep(),
         background.width(), background.height());
  }
  
  /**
   * Checks if the depth value of z at (x, y) is in contact with the table 
   * surface.
   * @param x
   * @param y
   * @param z physical depth in millimeter.
   * @return
   */
  public boolean isInContact(int x, int y, float z) {
    if (!initialized)
      return false;
    float tableDepth = depthAt(x, y);
    float tableDiff = diffAt(x, y);
    return z < tableDepth + tableDiff * DIFF_SCALE && 
        z > tableDepth - tableDiff * DIFF_SCALE;
  }

  /**
   * Gets the height of the table at point(x, y). The table must be initialized.
   * @param x x coordinate of the point.
   * @param y y coordinate of the point.
   * @return average depth of the table at (x, y).
   */
  private float depthAt(int x, int y) {
    return avg.get(y * avgWidthStep + x);
  }
  
  /**
   * Gets the average height difference of the table at point(x, y). The table
   * must be initialized.
   * @param x
   * @param y
   * @return
   */
  private float diffAt(int x, int y) {
    return diff.get(y * diffWidthStep + x);
  }
  
  private void computeSurfaceNormal() {
    HandTrackingEngine engine = null;
    try {
      engine = HandTrackingEngine.instance();
    } catch (GeneralException ge) {
      logger.severe(ge.getMessage());
      System.exit(-1);
    }
    Point3D[] points = new Point3D[width];
    int h = height / 2;
    int startIndex = h * avgWidthStep;
    for (int i = 0; i < width; i++) {
      float depth = avg.get(startIndex + i);
      points[i] = new Point3D(i, h, depth);
    }
    
    Point3D[] converted = engine.convertProjectiveToRealWorld(points);
    // projective = image coordinate space ((0,0) is top left corner of the 
    // image)
    CvMat pointMat = CvMat.create(1, width, CV_32FC3);
    for (int i = 0; i < converted.length; i++) {
      pointMat.put(i * 3, converted[i].getX());
      pointMat.put(i * 3 + 1, converted[i].getY());
      pointMat.put(i * 3 + 2, converted[i].getZ());
    }
    
    float[] hline = new float[6];
    cvFitLine(pointMat, CV_DIST_L2, 0, 0.001, 0.001, hline);
    pointMat.release();
    
    int w = width / 2;
    points = new Point3D[height];
    for (int i = 0; i < height; i++) {
      points[i] = new Point3D(w, i, avg.get(avgWidthStep * i + w));
    }
    
    converted = engine.convertProjectiveToRealWorld(points);
    pointMat = CvMat.create(1, height, CV_32FC3);
    for (int i = 0; i < converted.length; i++) {
      pointMat.put(i * 3, converted[i].getX());
      pointMat.put(i * 3 + 1, converted[i].getY());
      pointMat.put(i * 3 + 2, converted[i].getZ());
    }
    
    float[] vline = new float[6];
    cvFitLine(pointMat, CV_DIST_L2, 0, 0.001, 0.001, vline);
    pointMat.release();
    
    Vector3f v1 = new Vector3f(hline[0], hline[1], hline[2]);
    Vector3f v2 = new Vector3f(vline[0], vline[1], vline[3]);
    surfaceNormal = VectorUtil.cross(v1, v2);
    logger.info("surface normal = " + surfaceNormal);
  }
}
