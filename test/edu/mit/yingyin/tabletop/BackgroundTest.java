package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;

import org.junit.Test;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class BackgroundTest {
  private static final float EPSILON = 0.0001f;
  private static final int MAX_DEPTH = 10;
  private static final int WIDTH = 10, HEIGHT = 10;
  
  @Test
  public void testBackgroundDiff() {
    Background bg = new Background(WIDTH, HEIGHT, MAX_DEPTH);
    int[] depthRawData = new int[WIDTH * HEIGHT];
    Arrays.fill(depthRawData, 1);
    
    for (int i = 0; i < 10; i++) {
      bg.accumulateBackground(depthRawData);
    }
    
    IplImage avg = bg.getAvg();
    FloatBuffer avgBuffer = avg.getFloatBuffer();
    for (int i = 0; i < WIDTH * HEIGHT; i++) {
      System.out.print(avgBuffer.get(i) + " ");
    }
    System.out.println();
    
    bg.createModelsFromStats(1, 1);
    IplImage mask = IplImage.create(WIDTH, HEIGHT, IPL_DEPTH_8U, 1);
    
    int[] newDepth = new int[WIDTH * HEIGHT]; 
    Arrays.fill(newDepth, 2);
    bg.backgroundDiff(newDepth, mask);
    ByteBuffer bb = mask.getByteBuffer();
    for (int i = 0; i < newDepth.length; i++) {
      System.out.println(bb.get(i) * 0xff);
      assertEquals(255, bb.get(i) & 0xff);
    }
  }
}
