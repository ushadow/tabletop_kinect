package edu.mit.yingyin.tabletop;

public class HandProcessor {
  private int[] bgDepthMap;

  public void processData(int[] depth_map) {
    if (bgDepthMap == null) {
      bgDepthMap = depth_map.clone();
    }
    
    for (int i = 0; i < depth_map.length; i++) {
      if (bgDepthMap[i] - depth_map[i] < 10)
        depth_map[i] = 0;
    }
  }
}
