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
  
  @Test
  public void testBackgroundDiff() {
    Background bg = new Background(WIDTH, HEIGHT, MAX_DEPTH);
    int[] depthRawData = new int[WIDTH * HEIGHT];
    Arrays.fill(depthRawData, 1);
    
    for (int i = 0; i < 11; i++) {
      bg.accumulateBackground(depthRawData);
    }
    
    bg.createModelsFromStats(1, 1);
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
}
