package edu.mit.yingyin.util;

public class MathUtil {
  public static int clip(int v, int min, int max) {
    int res = Math.max(v, min);
    return Math.min(res, max);
  }

}
