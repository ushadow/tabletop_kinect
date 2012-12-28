package edu.mit.yingyin.tabletop.models;

import static com.googlecode.javacv.cpp.opencv_core.CV_32FC1;
import static com.googlecode.javacv.cpp.opencv_core.CV_PCA_DATA_AS_ROW;
import static com.googlecode.javacv.cpp.opencv_core.cvCalcPCA;
import static com.googlecode.javacv.cpp.opencv_core.cvGEMM;
import static com.googlecode.javacv.cpp.opencv_core.cvT;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.OpenNI.Point3D;
import org.OpenNI.StatusException;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.mit.yingyin.tabletop.models.ProcessPacket.ForelimbFeatures;

/**
 * Detects hand pose features.
 * @author yingyin
 *
 */
public class HandPoseFeatureDetector {
  private static final Logger LOGGER = Logger.getLogger(
      HandPoseFeatureDetector.class.getName());
  
  /**
   * Depth image width and height.
   */
  private final int width, height;
  private final int dim = 3;
  private final OpenNIDevice openni;
  private final CvMat mean = CvMat.create(1, dim, CV_32FC1);
  /**
   * Eigen values with decreasing magnitude.
   */
  private final CvMat eigenvals = CvMat.create(1, dim, CV_32FC1);
  /**
   * Each row is an eigenvector.
   */
  private final CvMat eigenvecs = CvMat.create(dim, dim, CV_32FC1);
  private final CvMat rotMat = CvMat.create(dim, dim, CV_32FC1);
      
  public HandPoseFeatureDetector(int width, int height, OpenNIDevice openni) {
    this.width = width;
    this.height = height;
    this.openni = openni;
  }
  
  public void detect(ProcessPacket packet) throws StatusException {
    for (ForelimbFeatures ff : packet.forelimbFeatures) {
      if (ff.handRegion != null) {
        Point3D[] worldPoints = preprocess(packet.depthRawData, ff.handRegion, 
                                           packet.morphedImage); 
        if (worldPoints.length < dim)
          continue;
        ff.handPose = alignPCA(worldPoints);
      }
    }
  }
  
  public void release() {
    mean.release();
    eigenvals.release();
    eigenvecs.release();
    rotMat.release();
  }
  
  private Point3D[] preprocess(int[] rawDepthData, CvRect handRegion, 
                               IplImage mask) 
      throws StatusException {
    ByteBuffer foregroundMask = mask.getByteBuffer();
    int maskWidthStep = mask.widthStep();
    int handWidth = handRegion.width();
    int handHeight = handRegion.height();
    ArrayList<Point3D> points = new ArrayList<Point3D>(handWidth * handHeight); 
    for (int y = handRegion.y(); y < handRegion.y() + handHeight; y++)
      for (int x = handRegion.x(); x < handRegion.x() + handWidth; x++) {
        if (foregroundMask.get(y * maskWidthStep + x) != 0)
          points.add(new Point3D(x, y, rawDepthData[y * width + x]));
      }
    Point3D[] projective = new Point3D[points.size()];
    points.toArray(projective);
    return openni.convertProjectiveToRealWorld(projective);
  }
  
  private CvMat alignPCA(Point3D[] worldPoints) {
    int n = worldPoints.length;
    // Row matrix.
    CvMat worldPointsMat = CvMat.create(n, dim, CV_32FC1);
    FloatBuffer fb = worldPointsMat.getFloatBuffer();
    fb.rewind();
    for (int i = 0; i < n; i++) {
      Point3D p = worldPoints[i];
      fb.put(p.getX());
      fb.put(p.getY());
      fb.put(p.getZ());
    }
    cvCalcPCA(worldPointsMat, mean, eigenvals, eigenvecs, CV_PCA_DATA_AS_ROW);
    fb.rewind();
    for (int i = 0; i < n; i++) {
      fb.put((float) (fb.get(i * dim) - mean.get(0)));
      fb.put((float) (fb.get(i * dim + 1) - mean.get(1)));
      fb.put((float) (fb.get(i * dim + 2) - mean.get(2)));
    }
    LOGGER.fine("mean: " + mean.toString());
    LOGGER.fine("eigen values:" + eigenvals.toString());
    LOGGER.fine(eigenvecs.toString());
    checkPolarity(eigenvecs);
    // Rotation matrix is inverse of the PCA space coordinate axes.
    cvT(eigenvecs, rotMat);
    
    CvMat aligned = CvMat.create(n, dim, CV_32FC1);
    /**
     * aligned = 1 * op(worldPointsMat)* op(eigenvecs) + 0 * op(null)
     * op() = 0 means no transposition.
     */
    cvGEMM(worldPointsMat, rotMat, 1, null, 0, aligned, 0);
    
    worldPointsMat.release();
    return aligned;
  }
  
  private void checkPolarity(CvMat mat) {
    int zIndex = 0;
    double max = Float.NEGATIVE_INFINITY;
    for (int i = 0; i < 3; i++) {
      double z = mat.get(i, 2);
      if ( z > max) {
        max = z;
        zIndex = i;
      }
    }
    double[] p = new double[3];
    for (int i = zIndex; i < 2; i++) {
      for (int j = 0; j < 3; j++) {
        p[j] = mat.get(i, j);
        mat.put(i, j, mat.get(i + 1, j));
        mat.put(i + 1, j, p[j]);
      }
    }
  }
}
