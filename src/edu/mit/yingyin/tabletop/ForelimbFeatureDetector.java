package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_imgproc.CvConvexityDefect;

import java.awt.Point;
import java.awt.Rectangle;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector2f;

import edu.mit.yingyin.image.BinaryFast;
import edu.mit.yingyin.image.ThinningTransform;
import edu.mit.yingyin.tabletop.Forelimb.ValConfiPair;
import edu.mit.yingyin.tabletop.ProcessPacket.ForelimbFeatures;
import edu.mit.yingyin.util.Geometry;
import edu.mit.yingyin.util.Matrix;

public class ForelimbFeatureDetector {
  
  private static final float FINGERTIP_ANGLE_THRESH = (float)1.6;
  
  /**
   * Structuring elements for skeletonization by morphological thinning.
   */
  private static int[] THINNING_KERNEL_ORTH = {0, 0, 0, 2, 1, 2, 1, 1, 1};
  private static int[] THINNING_KERNEL_DIAG = {2, 0, 0, 1, 1, 0, 2, 1, 2};
  private static int[] PRUNING_KERNEL1 = {0, 0, 0, 0, 1, 0, 0, 2, 2};
  private static int[] PRUNING_KERNEL2 = {0, 0, 0, 0, 1, 0, 2, 2, 0};
  
  public void extractFingertipsConvexityDefects(ProcessPacket packet) {
    for (ForelimbFeatures ff : packet.forelimbFeatures) {
      CvSeq defects = ff.convexityDefects;
      for (int i = 0; i < defects.total(); i++) {
        CvConvexityDefect defect1 = new CvConvexityDefect(
            cvGetSeqElem(defects, i));
        if (CvUtil.pointInRect(defect1.end(), ff.handRegion)) {
          CvConvexityDefect defect2 = new CvConvexityDefect(
              cvGetSeqElem(defects, (i + 1) % defects.total()));
          if (CvUtil.pointInRect(defect2.start(), ff.handRegion)) {
            Vector2f v1 = new Vector2f(
                defect1.depth_point().x() - defect1.end().x(),
                defect1.depth_point().y() - defect1.end().y());
            Vector2f v2 = new Vector2f(
                defect2.depth_point().x() - defect2.start().x(),
                defect2.depth_point().y() - defect2.start().y());
          }
        }
      }
    }
    
  }
  
  /**
   * Extracts fingertips based on convex hull.
   * @param packet contains all the processed information.
   */
  public void extractFingertipsConvexHull(ProcessPacket packet) {
    for (ForelimbFeatures ff : packet.forelimbFeatures) {
      Rectangle handRect = ff.handRegion;
      if (handRect != null) {
        Forelimb forelimb = new Forelimb();
        
        CvMat hull = ff.hull;
        CvMat approxPoly = ff.approxPoly;
        CvRect rect = ff.boundingBox;
        int numPolyPts = approxPoly.length();
  
        for (int j = 0; j < hull.length(); j++) {
          int idx = (int)hull.get(j);
          int pdx = (idx - 1 + numPolyPts) % numPolyPts;
          int sdx = (idx + 1) % numPolyPts;
          Point C = new Point((int)approxPoly.get(idx * 2), 
                              (int)approxPoly.get(idx * 2 + 1));
          Point A = new Point((int)approxPoly.get(pdx * 2), 
                              (int)approxPoly.get(pdx * 2 + 1));
          Point B = new Point((int)approxPoly.get(sdx * 2),
                              (int)approxPoly.get(sdx * 2 + 1));
          
          float angle = (float)Geometry.getAngleC(A, B, C);
          if (angle < FINGERTIP_ANGLE_THRESH && C.y >= handRect.y && 
              C.y <= handRect.y + handRect.height) {
            float z = packet.depthRawData[C.y * packet.width + C.x];
            forelimb.fingertips.add(new ValConfiPair<Point3f>(
                new Point3f(C.x, C.y, z), 1));
          }
        }
        forelimb.center = new Point(rect.x() + rect.width() / 2, 
            rect.y() + rect.height() / 2);
        
        packet.foreLimbs.add(forelimb);
      }
    }
  }
  
  public void extractFeaturesThinning(ProcessPacket packet) {
    thinningHands(packet);
  }
  
  /**
   * Applies thinning mophological operation to hand regions.
   * 
   * @param packet
   */
  private void thinningHands(ProcessPacket packet) {
    ByteBuffer bb = packet.morphedImage.getByteBuffer();
    int widthStep = packet.morphedImage.widthStep();
    for (ForelimbFeatures ff : packet.forelimbFeatures) {
      Rectangle rect = ff.handRegion;
      Forelimb forelimb = new Forelimb();
      
      if (rect != null) {
        byte[][] pixels = new byte[rect.height][rect.width];
        for (int dy = 0; dy < rect.height; dy++) 
          for (int dx = 0; dx < rect. width; dx++) {
            int index = (rect.y + dy) * widthStep + rect.x + dx;
            if (bb.get(index) == 0)
              pixels[dy][dx] = BinaryFast.background;
            else pixels[dy][dx] = BinaryFast.foreground;
          }
        BinaryFast bf = new BinaryFast(pixels, rect.width, rect.height);
        
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
        for (int dy = 0; dy < rect.height; dy++) 
          for (int dx = 0; dx < rect. width; dx++) {
            int index = (rect.y + dy) * widthStep + rect.x + dx;
            if (pixels[dy][dx] == BinaryFast.background)
              bb.put(index, (byte)0);
            else bb.put(index, (byte)255);
          }
        
        List<Point3f> finger = new ArrayList<Point3f>();
        for (Point p : extractFinger(pixels)) {
          int x = rect.x + p.x; 
          int y = rect.y + p.y;
          float z = packet.depthRawData[y * packet.width + x];
          finger.add(new Point3f(x, y, z));
        }
        if (!finger.isEmpty()) {
          forelimb.fingers.add(finger);
          forelimb.fingertips.add(new ValConfiPair<Point3f>(
                                  new Point3f(finger.get(finger.size() - 1)), 1));
        }
        forelimb.center = new Point(rect.x + rect.width / 2, 
                                    rect.y + rect.height / 2);
        
        packet.foreLimbs.add(forelimb);
      }
    }
  }
  
  /**
   * 
   * @param pixels two dimensional array of the hand region with at least one 
   *    row.
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
      
    for (int i = 1; i < h; i++ )
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
}
