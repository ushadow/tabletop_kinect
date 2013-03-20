package edu.mit.yingyin.tabletop.models;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;

public class FeatureBuilder {
  public static final int CONTINUOUS_FEATURE_SIZE = 11;
  public static final int DEFAULT_IMAGE_WIDTH = 50;
  
  private static int imageWidth = DEFAULT_IMAGE_WIDTH;
  
  public static int imageWidth() { return imageWidth; }
  
  public static void imageWidth(int imageWidth) {
    FeatureBuilder.imageWidth = imageWidth;
  }
  public static float[] create(Forelimb forelimb) {
    float[] features = new float[CONTINUOUS_FEATURE_SIZE + 
                                 imageWidth * imageWidth];
    Hand hand = forelimb.hand();
    features[0] = forelimb.armLength();
    features[1] = hand.distAboveSurface();
    int startIndex = 2;
    addTuple(features, startIndex, hand.velocity());
    startIndex += 3;
    addTuple(features, startIndex, hand.acceleration());
    startIndex += 3;
    addTuple(features, startIndex, hand.rotation());
    addHandPoints(features, hand);
    return features;
  }
  
  private static void addTuple(float[] features, int startIndex, Tuple3f t) {
    features[startIndex] = t.x;
    features[startIndex + 1] = t.y;
    features[startIndex + 2] = t.z;
  }
 
  /**
   * Adds hand cloud points to feature vector. The points are normalized to a 
   * cube with a constant width. 
   * @param features
   * @param hand
   */
  private static void addHandPoints(float[] features, Hand hand) {
    float scale = imageWidth / hand.width();
    float offset = imageWidth / 2;
    for (Point3f p : hand.pointCloud()) {
      int x = (int) (p.x * scale + offset);
      int y = (int) (p.y * scale + offset);
      if (x >= 0 && x < imageWidth && y >= 0 && y < imageWidth) {
        float z = Math.max(0, p.z * scale + offset);
        features[CONTINUOUS_FEATURE_SIZE + y * imageWidth + x] = z;
      }
    }
  }
}
