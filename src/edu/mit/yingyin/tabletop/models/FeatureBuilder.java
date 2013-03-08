package edu.mit.yingyin.tabletop.models;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;

public class FeatureBuilder {
  public static final int CONTINUOUS_FEATURE_SIZE = 11;
  public static final int IMAGE_SIZE = 50;
  
  public static float[] create(Forelimb forelimb) {
    float[] features = new float[CONTINUOUS_FEATURE_SIZE + 
                                 IMAGE_SIZE * IMAGE_SIZE];
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
  
  private static void addHandPoints(float[] features, Hand hand) {
    float scale = IMAGE_SIZE / hand.width();
    float offset = IMAGE_SIZE / 2;
    for (Point3f p : hand.pointCloud()) {
      int x = (int) (p.x * scale + offset);
      int y = (int) (p.y * scale + offset);
      if (x >= 0 && x < IMAGE_SIZE && y >= 0 && y < IMAGE_SIZE) {
        float z = p.z * scale + offset;
        features[CONTINUOUS_FEATURE_SIZE + y * IMAGE_SIZE + x] = Math.max(0, z);
      }
    }
  }
}
