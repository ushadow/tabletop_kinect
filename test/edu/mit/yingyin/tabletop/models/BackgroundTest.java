package edu.mit.yingyin.tabletop.models;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvZero;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.Test;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.mit.yingyin.tabletop.models.Background;

public class BackgroundTest {
  private static final int WIDTH = 10, HEIGHT = 10;
  private static final float EPSILON = 0.00001f;
  
  @Test
  public void testBackgroundDiffSimple() {
    Background bg = Background.initInstance(WIDTH, HEIGHT);
    int[] depthRawData = new int[WIDTH * HEIGHT];
    Arrays.fill(depthRawData, 1);
    
    for (int i = 0; i < 11; i++) {
      bg.accumulateBackground(depthRawData);
    }
    
    bg.createModelsFromStats(1, 1);
    assertEquals(1, bg.avgDepth(), EPSILON);
    assertEquals(0, bg.avgDiff(), EPSILON);
    IplImage mask = IplImage.create(WIDTH, HEIGHT, IPL_DEPTH_8U, 1);
    
    int[] newDepth = new int[WIDTH * HEIGHT]; 
    Arrays.fill(newDepth, 3);
    bg.backgroundDiff(newDepth, mask);
    ByteBuffer bb = mask.getByteBuffer();
    int widthStep = mask.widthStep();
    for (int h = 0; h < HEIGHT; h++)
      for (int w = 0; w < WIDTH; w++)
        assertEquals(255, bb.get(h * widthStep + w) & 0xff);
    
    Arrays.fill(newDepth, 1);
    bg.backgroundDiff(newDepth, mask);
    bb = mask.getByteBuffer();
    for (int h = 0; h < HEIGHT; h++)
      for (int w = 0; w < WIDTH; w++)
        assertEquals(0, bb.get(h * widthStep + w) & 0xff);
  }
  
  @Test
  public void testBackgroundDiffVaried() {
    Background bg = Background.initInstance(WIDTH, HEIGHT);
    int[] depthRawData = new int[WIDTH * HEIGHT];
    
    for (int i = 0; i < 11; i++) {
      Arrays.fill(depthRawData, i);
      bg.accumulateBackground(depthRawData);
    }
    
    bg.createModelsFromStats(1, 1);
    assertEquals(5, bg.avgDepth(), EPSILON);
    assertEquals(2.5, bg.avgDiff(), EPSILON);
    IplImage mask = IplImage.create(WIDTH, HEIGHT, IPL_DEPTH_8U, 1);
    
    int[] newDepth = new int[WIDTH * HEIGHT]; 
    Arrays.fill(newDepth, 1);
    bg.backgroundDiff(newDepth, mask);
    ByteBuffer bb = mask.getByteBuffer();
    int widthStep = mask.widthStep();
    for (int h = 0; h < HEIGHT; h++)
      for (int w = 0; w < WIDTH; w++)
        assertEquals(255, bb.get(h * widthStep + w) & 0xff);
    
    Arrays.fill(newDepth, 4);
    bg.backgroundDiff(newDepth, mask);
    bb = mask.getByteBuffer();
    for (int h = 0; h < HEIGHT; h++)
      for (int w = 0; w < WIDTH; w++)
        assertEquals(0, bb.get(h * widthStep + w) & 0xff);
    
    bg.createModelsFromStats(1, 2);
    System.out.println(bg.toString());
    cvZero(mask);
    Arrays.fill(newDepth, 3);
    bg.backgroundDiff(newDepth, mask);
    for (int h = 0; h < HEIGHT; h++)
      for (int w = 0; w < WIDTH; w++) {
        if (bg.isInCenterColumn(w)) {
          assertEquals(0, bb.get(h * widthStep + w) & 0xff);
        }
      }
  }
  
  @Test
  public void testIsIncenterColumn() {
    Background bg = Background.initInstance(WIDTH, HEIGHT);
    assertTrue(bg.isInCenterColumn(4));
    assertTrue(bg.isInCenterColumn(5));
    assertFalse(bg.isInCenterColumn(WIDTH - 1));
  }
  
  @Test
  public void testWidthSteps() {
    Background bg = Background.initInstance(WIDTH, HEIGHT);
    assertEquals(WIDTH, bg.avgBufferWidthStep());
    assertEquals(WIDTH, bg.diffBufferWidthStep());
  }
}
