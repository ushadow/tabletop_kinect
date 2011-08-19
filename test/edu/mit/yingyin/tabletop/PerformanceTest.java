package edu.mit.yingyin.tabletop;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.nio.ShortBuffer;
import java.util.Arrays;

import org.OpenNI.GeneralException;
import org.junit.Test;

import rywang.util.Timer;

import edu.mit.yingyin.image.ImageConvertUtils;

public class PerformanceTest {
  @Test
  public void testShortBufferAndArry() {
    try {
      Timer timer = new Timer();
      OpenNI openni = new OpenNI("test_data/config.xml");
      int width = openni.depthWidth();
      int height = openni.depthHeight();
      short[] depthArray = new short[width * height];
      BufferedImage image1 = null;
      BufferedImage image2 = null;
      
      long totalTime1 = 0, totalTime2 = 0;
      int times = 20;
      
      for (int i = 0; i <times; i++) {
        openni.waitDepthAndUpdateAll();
        ShortBuffer buffer = openni.depthBuffer();
        
        timer.tic();
        image1 = ImageConvertUtils.depthToGrayBufferedImage(buffer, 
            width, height);
        totalTime1 += timer.getToc();
  
        buffer.rewind();
        timer.tic();
        buffer.get(depthArray);
        image2 = ImageConvertUtils.depthToGrayBufferedImage(
            depthArray, width, height);
        totalTime2 += timer.getToc();
      }
      
      System.out.println(totalTime1 / times + 
                         "ms : Image conversion using ShortBuffer.");
      System.out.println(totalTime2 / times +
                         "ms : Image conversion using short[].");
      short[] imageArray1 = 
          ((DataBufferUShort) image1.getRaster().getDataBuffer()).getData();
      
      short[] imageArray2 = 
          ((DataBufferUShort) image2.getRaster().getDataBuffer()).getData();
      
      long sum = 0;
      for (int i = 0; i < width * height; i++) 
        sum += imageArray2[i] & 0xffffffff; 
      assertTrue(sum > 0);
      assertTrue(Arrays.equals(imageArray1, imageArray2));
      openni.release();
    } catch (GeneralException e) {
      e.printStackTrace();
      fail();
    }
  }
  
  @Test
  public void testJni() {
    String configFile = "test_data/config.xml";
    Timer timer = new Timer();
    int times = 20;
    long totalTime = 0;
    try {
      OpenNI openni = new OpenNI(configFile);
      for (int i = 0; i < times; i++) {
        timer.tic();
        openni.waitDepthAndUpdateAll();
        totalTime += timer.getToc();
      }
      System.out.println(totalTime / times + 
                         "ms : OpenNI's jni implementation.");
      openni.release();
    } catch (GeneralException e) {
      e.printStackTrace();
      fail();
    }
    
    OpenNIWrapper openni = new OpenNIWrapper(configFile);
    totalTime = 0;
    for (int i = 0; i < times; i++) {
      timer.tic();
      openni.waitDepthUpdateAll();
      totalTime += timer.getToc();
    }
    System.out.println(totalTime / times + 
                       "ms : Ying's jni implementation.");
    openni.release();
  }
}
