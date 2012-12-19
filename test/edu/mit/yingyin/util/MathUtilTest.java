package edu.mit.yingyin.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class MathUtilTest {

  @Test
  public void testClip() {
    assertEquals(1, MathUtil.clip(0, 1, 2));
    assertEquals(0, MathUtil.clip(0, 0, 2));
    assertEquals(0, MathUtil.clip(0, -1, 2));
    assertEquals(1, MathUtil.clip(0, 1, 1));
    assertEquals(0, MathUtil.clip(0, 1, 0));
  }
}
