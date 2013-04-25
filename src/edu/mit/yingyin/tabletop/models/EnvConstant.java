package edu.mit.yingyin.tabletop.models;

import edu.mit.yingyin.util.FileUtil;

public class EnvConstant {
  public static final int TABLETOP_WIDTH = 1920 * 2;
  public static final int TABLETOP_HEIGHT = 1080 * 2;
  public static final int MIN_DEPTH = 500;
  public static final int MAX_DEPTH = 1300;
  
  public static final String POINT_LABEL_SUFFIX = ".plab";
  public static final String GESTURE_LABEL_SUFFIX = ".glab";
  public static final String GESTURE_FEATURE_SUFFIX = ".gfet";
  public static final String DEFAULT_BASENAME = "captured";
  
  public static final String CONFIG_DIR = "config";
  public static final String DATA_DIR = "data";
  public static final String FINGERTIP_DIR = FileUtil.join(DATA_DIR, 
      "fingertip");
  public static final String DESCRIPTOR_DIR = FileUtil.join(DATA_DIR,
      "descriptor");
  public static final String HAND_POINT_CLOUD_DIR = FileUtil.join(DATA_DIR,
      "hand_point_cloud");
  public static final String ONI_DIR = FileUtil.join(DATA_DIR, 
                                                     "oni");
  public static final String GESUTRE_DIR = FileUtil.join(DATA_DIR, "gesture");
}
