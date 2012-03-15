package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.Test;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class BackgroundTest {
  private static final int MAX_DEPTH = 10;
  private static final int WIDTH = 10, HEIGHT = 10;
  private static final float EPSILON = 0.00001f;
  
  @Test
  public void testBackgroundDiffSimple() {
    Background bg = new Background(WIDTH, HEIGHT, MAX_DEPTH);
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
    Arrays.fill(newDepth, 2);
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
    Background bg = new Background(WIDTH, HEIGHT, MAX_DEPTH);
    int[] depthRawData = new int[WIDTH * HEIGHT];
    
    for (int i = 0; i < 11; i++) {
      Arrays.fill(depthRawData, i);
      bg.accumulateBackground(depthRawData);
    }
    
    bg.createModelsFromStats(2, 1);
    assertEquals(5.5, bg.avgDepth(), EPSILON);
    assertEquals(1, bg.avgDiff(), EPSILON);
    IplImage mask = IplImage.create(WIDTH, HEIGHT, IPL_DEPTH_8U, 1);
    
    int[] newDepth = new int[WIDTH * HEIGHT]; 
    Arrays.fill(newDepth, 2);
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
  }
  
  @Test
  public void testWidthSteps() {
    Background bg = new Background(WIDTH, HEIGHT, MAX_DEPTH);
    assertEquals(WIDTH, bg.avgBufferWidthStep());
    assertEquals(WIDTH, bg.diffBufferWidthStep());
  }
}
