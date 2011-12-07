package edu.mit.yingyin.tabletop;

import java.awt.Point;
import java.awt.Rectangle;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Point3f;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvRect;

import edu.mit.yingyin.image.BinaryFast;
import edu.mit.yingyin.image.ThinningTransform;
import edu.mit.yingyin.tabletop.ProcessPacket.ForelimbModel;
import edu.mit.yingyin.tabletop.ProcessPacket.ForelimbModel.ValConfiPair;
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
  
  /**
   * Extracts forelimb features from the information in the <code>packet</code>.
   * @param packet
   */
  public void extractFeaturesConvexHull(ProcessPacket packet) {
    for (int i = 0; i < packet.hulls.size(); i++) {
      Rectangle handRect = packet.handRegions.get(i);
      if (handRect != null) {
        ForelimbModel forelimb = new ForelimbModel();
        List<ValConfiPair<Point3f>> fingerTips = 
            new ArrayList<ValConfiPair<Point3f>>();
        
        CvMat hull = packet.hulls.get(i);
        CvMat approxPoly = packet.approxPolys.get(i);
        CvRect rect = packet.boundingBoxes.get(i);
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
            float z = packet.depthRawData[C.y * packet.depthImage8U.width() + 
                                          C.x];
            fingerTips.add(new ValConfiPair<Point3f>(
                new Point3f(C.x, C.y, z), 1));
          }
        }
        forelimb.fingertips = fingerTips;
  
        forelimb.center = new Point(rect.x() + rect.width() / 2, 
            rect.y() + rect.height() / 2);
        
        packet.foreLimbsFeatures.add(forelimb);
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
    for (Rectangle rect : packet.handRegions) {
      if (rect != null) {
        byte[][] pixels = new byte[rect.height][rect.width];
        for (int dy = 0; dy < rect.height; dy++) 
          for (int dx = 0; dx < rect. width; dx++) {
            int index = (rect.y + dy) * packet.morphedImage.width() + rect.x + 
                        dx;
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
            int index = (rect.y + dy) * packet.morphedImage.width() + 
                        rect.x + dx;
            if (pixels[dy][dx] == BinaryFast.background)
              bb.put(index, (byte)0);
            else bb.put(index, (byte)255);
          }
      }
    }
  }
  
  /**
   * 
   * @param pixels
   */
  private void extractLine(byte[][] pixels) {
    int h = pixels.length;
    int w = pixels[0].length;
    
    int[][] dp = new int[h][w];
    int[][] parents = new int[h][w];
    
    for (int j = 0; j < w; j++) {
      dp[0][j] = pixels[0][j] == BinaryFast.foreground ? 1 : 0; 
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
        dp[i][j] = max + (pixels[i][j] == BinaryFast.foreground ? 1 : 0);
        parents[i][j] = parent; 
      }
    
    int best = 0;
    int bestj = 0;
    for (int j = 0; j < w; j++)
      if (dp[h - 1][j] > best) {
        best = dp[h - 1][j];
        bestj = j;
      }
  
    int parent = parents[h - 1][bestj];
    int j = bestj;
    for (int i = h - 2; i >= 0; i--) {
      parent = parents[i][j + parent];
    }
      
  }
}
