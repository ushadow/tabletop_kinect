package edu.mit.yingyin.tabletop.models;

import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;

import java.awt.Point;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.OpenNI.Point3D;
import org.OpenNI.StatusException;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_imgproc.CvConvexityDefect;

import edu.mit.yingyin.image.BinaryFast;
import edu.mit.yingyin.image.ThinningTransform;
import edu.mit.yingyin.tabletop.models.ProcessPacket.ForelimbFeatures;
import edu.mit.yingyin.tabletop.models.ProcessPacket.HandFeatures;
import edu.mit.yingyin.util.CvUtil;
import edu.mit.yingyin.util.Geometry;
import edu.mit.yingyin.util.Matrix;
import edu.mit.yingyin.util.ValConfidencePair;

/**
 * Estimates forelimb model including fingertip positions based on features
 * derived from the image.
 * 
 * @author yingyin
 * 
 */
public class ForelimbModelEstimator {
  // Around 45 deg.
  private static final float FINGERTIP_ANGLE = (float) 0.8;

  private static final float FINGERTIP_WIDTH = 7;

  private static final float FINGERTIP_WIDTH_THRESHOLD = FINGERTIP_WIDTH
      * FINGERTIP_WIDTH / 4;
  
  private static final float SMOOTH_FACTOR = (float) 0.9;
  private static final float TREND_SMOOTH_FACTOR = (float) 0.9;
  
  private final int width, height;
  private final OpenNIDevice openni;
  private final DoubleExpFilter filter;
  
  /**
   * Displacement in world coordinates in the previous frame.
   */
  private Point3f prevS;
  /**
   * Velocity in world coordinates in the previous frame.
   */
  private Vector3f prevV;

  public ForelimbModelEstimator(int width, int height, 
      OpenNIDevice openni) {
    this.width = width;
    this.height = height;
    this.openni = openni;
    filter = new DoubleExpFilter(SMOOTH_FACTOR, TREND_SMOOTH_FACTOR);
  }

  public void updateModel(ProcessPacket packet) throws StatusException {
    for (ForelimbFeatures ff : packet.forelimbFeatures) {
      if (ff.handRegion == null)
        continue;

      findFingertipsConvexityDefects(ff, packet);
    }  
    
    List<Point3f> filteredFingertips = filter.filter(packet);
    
    // HACK: only consider one hand now.
    if (packet.forelimbFeatures.size() > 0) {
      ForelimbFeatures ff = packet.forelimbFeatures.get(0);
      HandFeatures hf = ff.hf;
      if (hf != null) {
        List<Point3f> fingertipsW = null;
        if (filteredFingertips != null) {
          // Convert to world coordinates.
          Point3D[] points = new Point3D[filteredFingertips.size()];
          for (int i = 0; i < filteredFingertips.size(); i++) {
            Point3f point = filteredFingertips.get(i);
            points[i] = new Point3D(point.x, point.y, point.z);
          }
          Point3D[] converted = openni.convertProjectiveToRealWorld(points);
          fingertipsW = new ArrayList<Point3f>(converted.length);
          for (Point3D p : converted)
            fingertipsW.add(new Point3f(p.getX(), p.getY(), p.getZ()));
          
        }
        List<Point3f> armJoints = findCentroid(packet, ff.armJointRegion);
        Vector3f v = null, a = null;
        if (prevS != null) {
          v = new Vector3f();
          v.sub(hf.centroidWorld, prevS);
          if (prevV != null) {
            a = new Vector3f();
            a.sub(v, prevV);
            float dist = InteractionSurface.instance().
                distanceAboveSurface(hf.centroidWorld);
            Hand hand = new Hand(dist, hf.handPoseWidth, hf.centroidWorld, v, a, 
                                 hf.rot, hf.pointCloud);
            Forelimb forelimb = new Forelimb(filteredFingertips, fingertipsW, 
                armJoints, hand);
            packet.forelimbs.add(forelimb);
          }
        }
        prevS = hf.centroidWorld;
        prevV = v;
        return;
      }
    }
    prevS = null;
    prevV = null;
  }

  /**
   * Finds fingertip positions in the image coordinates using convexity defects 
   * method.
   * 
   * @param packet
   */
  private  void findFingertipsConvexityDefects(ForelimbFeatures ff, 
      ProcessPacket packet) {
    CvSeq defects = ff.convexityDefects;

    for (int i = 0; i < defects.total(); i++) {
      CvConvexityDefect defect1 = new CvConvexityDefect(cvGetSeqElem(defects,
          i));
      if (CvUtil.pointInRect(defect1.end(), ff.handRegion)) {
        CvConvexityDefect defect2 = new CvConvexityDefect(cvGetSeqElem(
            defects, (i + 1) % defects.total()));
        if (CvUtil.pointInRect(defect2.start(), ff.handRegion)) {
          ValConfidencePair<Point3f> fingertip = findFingertip(defect1, defect2,
              packet);
          if (fingertip != null)
            ff.fingertips.add(fingertip);
        }
      }
    }
  }

  /**
   * Finds a fingertip from two convexity defects.
   * 
   * @param defect1
   * @param defect2
   * @param packet
   * @return
   */
  private ValConfidencePair<Point3f> findFingertip(CvConvexityDefect defect1,
      CvConvexityDefect defect2, ProcessPacket packet) {
    Vector2f v1 = new Vector2f(defect1.depth_point().x() - defect1.end().x(),
        defect1.depth_point().y() - defect1.end().y());
    Vector2f v2 = new Vector2f(defect2.depth_point().x() - defect2.start().x(),
        defect2.depth_point().y() - defect2.start().y());
    float distance2 = CvUtil.distance2(defect1.depth_point(),
        defect2.depth_point());
    if (Geometry.angle(v1, v2) <= FINGERTIP_ANGLE
        && distance2 >= FINGERTIP_WIDTH_THRESHOLD) {
      int mx = (defect1.end().x() + defect2.start().x()) / 2;
      int my = (defect1.end().y() + defect2.start().y()) / 2;

      Vector2f unitDir = searchDir(defect1, defect2);
      Point2f fp = searchFingertip(new Point2f(mx, my), unitDir, packet);
      float z = packet.getDepthRaw(Math.round(fp.x), Math.round(fp.y));
      return new ValConfidencePair<Point3f>(new Point3f(Math.round(fp.x),
          Math.round(fp.y), z), 1);
    }
    return null;
  }

  private Vector2f searchDir(CvConvexityDefect d1, CvConvexityDefect d2) {
    Vector2f v1 = new Vector2f();
    Vector2f v2 = new Vector2f();
    Point2f end1 = CvUtil.toPoint2f(d1.end());
    Point2f mid1 = CvUtil.toPoint2f(d1.depth_point());
    Point2f start2 = CvUtil.toPoint2f(d2.start());
    Point2f mid2 = CvUtil.toPoint2f(d2.depth_point());

    v1.sub(end1, mid1);
    v1.scale(1 / v1.length());
    v2.sub(start2, mid2);
    v2.scale(1 / v2.length());
    v1.add(v2);
    v1.scale(1 / v1.length());
    return v1;
  }

  private Point2f searchFingertip(Point2f start, Vector2f unitDir,
      ProcessPacket packet) {
    Point2f p = new Point2f(start);

    FloatBuffer fb = packet.derivative.getFloatBuffer();
    int widthStep = packet.derivative.widthStep() / 4;

    int count = 0;
    while (count <= FINGERTIP_WIDTH && p.y >= 0 && p.y < height && p.x >= 0
        && p.x < width) {
      int index = (int) p.y * widthStep + (int) p.x;
      float gradient = fb.get(index);
      if (gradient > 0.05 || gradient < -0.05) {
        break;
      }
      p.add(unitDir);
      count++;
    }

    Point2f result = new Point2f();
    result.scaleAdd(-FINGERTIP_WIDTH / 2, unitDir, p);
    return result;
  }

  /**
   * Extracts fingertips based on convex hull.
   * 
   * @param packet contains all the processed information.
   */
  private List<ValConfidencePair<Point3f>> findFingertipsConvexHull(
      ForelimbFeatures ff, ProcessPacket packet) {
    CvRect handRect = ff.handRegion;

    CvMat hull = ff.hull;
    CvMat approxPoly = ff.approxPoly;
    int numPolyPts = approxPoly.length();

    List<ValConfidencePair<Point3f>> fingertips = 
        new ArrayList<ValConfidencePair<Point3f>>();
    for (int j = 0; j < hull.length(); j++) {
      int idx = (int) hull.get(j);
      int pdx = (idx - 1 + numPolyPts) % numPolyPts;
      int sdx = (idx + 1) % numPolyPts;
      Point C = new Point((int) approxPoly.get(idx * 2),
          (int) approxPoly.get(idx * 2 + 1));
      Point A = new Point((int) approxPoly.get(pdx * 2),
          (int) approxPoly.get(pdx * 2 + 1));
      Point B = new Point((int) approxPoly.get(sdx * 2),
          (int) approxPoly.get(sdx * 2 + 1));

      float angle = (float) Geometry.getAngleC(A, B, C);
      if (angle < FINGERTIP_ANGLE_THRESH && C.y >= handRect.y()
          && C.y <= handRect.y() + handRect.height()) {
        float z = packet.depthRawData[C.y * packet.width + C.x];
        fingertips.add(new ValConfidencePair<Point3f>(new Point3f(C.x, C.y, z),
            1));
      }
    }
    return fingertips;
  }

  private static final float FINGERTIP_ANGLE_THRESH = (float) 1.6;

  /**
   * Structuring elements for skeletonization by morphological thinning.
   */
  private static int[] THINNING_KERNEL_ORTH = {0, 0, 0, 2, 1, 2, 1, 1, 1};
  private static int[] THINNING_KERNEL_DIAG = {2, 0, 0, 1, 1, 0, 2, 1, 2};
  private static int[] PRUNING_KERNEL1 = {0, 0, 0, 0, 1, 0, 0, 2, 2};
  private static int[] PRUNING_KERNEL2 = {0, 0, 0, 0, 1, 0, 2, 2, 0};

  /**
   * Finds fingertips based on thinning of fingers by applying thinning 
   * mophological operation to hand regions.
   * 
   * @param packet
   */
  private List<ValConfidencePair<Point3f>> findFingertipsThinning(
      ForelimbFeatures ff, ProcessPacket packet) {
    ByteBuffer bb = packet.morphedImage.getByteBuffer();
    int widthStep = packet.morphedImage.widthStep();
    CvRect rect = ff.handRegion;

    byte[][] pixels = new byte[rect.height()][rect.width()];
    for (int dy = 0; dy < rect.height(); dy++)
      for (int dx = 0; dx < rect.width(); dx++) {
        int index = (rect.y() + dy) * widthStep + rect.x() + dx;
        if (bb.get(index) == 0)
          pixels[dy][dx] = BinaryFast.background;
        else
          pixels[dy][dx] = BinaryFast.foreground;
      }
    BinaryFast bf = new BinaryFast(pixels, rect.width(), rect.height());

    for (int i = 0; i < 20; i++) {
      ThinningTransform.thinBinaryOnce(bf, THINNING_KERNEL_ORTH);
      ThinningTransform.thinBinaryOnce(bf, THINNING_KERNEL_DIAG);
      Matrix.rot90(THINNING_KERNEL_ORTH, 3);
      Matrix.rot90(THINNING_KERNEL_DIAG, 3);
    }
    for (int i = 0; i < 8; i++) {
      ThinningTransform.thinBinaryOnce(bf, PRUNING_KERNEL1);
      ThinningTransform.thinBinaryOnce(bf, PRUNING_KERNEL2);
      Matrix.rot90(PRUNING_KERNEL1, 3);
      Matrix.rot90(PRUNING_KERNEL2, 3);
    }
    for (int dy = 0; dy < rect.height(); dy++)
      for (int dx = 0; dx < rect.width(); dx++) {
        int index = (rect.y() + dy) * widthStep + rect.x() + dx;
        if (pixels[dy][dx] == BinaryFast.background)
          bb.put(index, (byte) 0);
        else
          bb.put(index, (byte) 255);
      }

    List<Point3f> finger = new ArrayList<Point3f>();
    List<ValConfidencePair<Point3f>> fingertips = 
        new ArrayList<ValConfidencePair<Point3f>>();
    for (Point p : extractFinger(pixels)) {
      int x = rect.x() + p.x;
      int y = rect.y() + p.y;
      float z = packet.depthRawData[y * packet.width + x];
      finger.add(new Point3f(x, y, z));
    }
    if (!finger.isEmpty()) {
      fingertips.add(new ValConfidencePair<Point3f>(new Point3f(
          finger.get(finger.size() - 1)), 1));
    }
    return fingertips;
  }

  /**
   * 
   * @param pixels two dimensional array of the hand region with at least one
   *          row.
   */
  private List<Point> extractFinger(byte[][] pixels) {
    List<Point> finger = new ArrayList<Point>();

    int h = pixels.length;
    int w = pixels[0].length;

    int[][] dp = new int[h][w];
    int[][] parents = new int[h][w];

    for (int j = 0; j < w; j++) {
      dp[0][j] = isSinglePixel(pixels, 0, j) ? 1 : 0;
      parents[0][j] = 2;
    }

    for (int i = 1; i < h; i++)
      for (int j = 0; j < w; j++) {
        int max = dp[i - 1][j];
        int parent = 0;
        if (j - 1 >= 0 && max < dp[i - 1][j - 1]) {
          max = dp[i - 1][j - 1];
          parent = -1;
        }
        if (j + 1 < w && max < dp[i - 1][j + 1]) {
          max = dp[i - 1][j + 1];
          parent = 1;
        }
        dp[i][j] = max + (isSinglePixel(pixels, i, j) ? 1 : 0);
        parents[i][j] = parent;
      }

    int best = 0;
    int bestj = 0;
    for (int j = 0; j < w; j++)
      if (dp[h - 1][j] > best) {
        best = dp[h - 1][j];
        bestj = j;
      }

    int current = bestj;
    for (int i = h - 1; i > 0; i--) {
      if (isSinglePixel(pixels, i, current))
        finger.add(new Point(current, i));
      current = current + parents[i][current];
    }
    return finger;
  }

  private boolean isSinglePixel(byte[][] pixels, int i, int j) {
    int w = pixels[0].length;
    if (pixels[i][j] == BinaryFast.background)
      return false;
    if (j - 1 >= 0 && pixels[i][j - 1] == BinaryFast.foreground)
      return false;
    if (j + 1 < w && pixels[i][j + 1] == BinaryFast.foreground)
      return false;
    return true;
  }

  /**
   * Finds the centroid of points in a rectangular region in both image and 
   * world coordinates.
   * 
   * @param packet
   * @param rect the rectangular region of interest.
   * @return a list of 3D points. The first point is the 3D location of the arm
   *         joint in the image, and the 2nd one is the location in the world
   *         coordinate. The list is empty if the arm joint cannot be found.
   * @throws StatusException 
   */
  private List<Point3f> findCentroid(ProcessPacket packet, CvRect rect) 
      throws StatusException {
    List<Point3f> res = new ArrayList<Point3f>(2);

    if (rect == null)
      return res;

    ByteBuffer maskBuffer = packet.foregroundMask.getByteBuffer();
    int maskStepWidth = packet.foregroundMask.widthStep();
    List<Point3D> list = new ArrayList<Point3D>();
    for (int y = rect.y(); y < rect.y() + rect.height(); y++)
      for (int x = rect.x(); x < rect.x() + rect.width(); x++) {
        if ((maskBuffer.get(y * maskStepWidth + x) & 0xff) == 255) {
          list.add(new Point3D(x, y,
              packet.depthRawData[y * packet.width + x]));
        }
      }
    if (list.size() == 0)
      return null;

    float imagex = 0, imagey = 0, imagez = 0;
    for (Point3D point : list) {
      imagex += point.getX();
      imagey += point.getY();
      imagez += point.getZ();
    }

    res.add(new Point3f(imagex / list.size(), imagey / list.size(), imagez
        / list.size()));

    Point3D[] points = new Point3D[list.size()];
    list.toArray(points);
    Point3D[] converted = openni.convertProjectiveToRealWorld(points);
    float centerx = 0, centery = 0, centerz = 0;
    for (Point3D point : converted) {
      centerx += point.getX();
      centery += point.getY();
      centerz += point.getZ();
    }
    res.add(new Point3f(centerx / list.size(), centery / list.size(), centerz
        / list.size()));

    return res;
  }
}
