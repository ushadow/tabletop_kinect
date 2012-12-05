package edu.mit.yingyin.tabletop;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.nio.ShortBuffer;
import java.util.Arrays;

import org.OpenNI.GeneralException;
import org.junit.Test;

import edu.mit.yingyin.image.ImageConvertUtils;
import edu.mit.yingyin.tabletop.models.OpenNIDevice;
import edu.mit.yingyin.tabletop.models.PartialOpenNIDevice;
import edu.mit.yingyin.util.Timer;

public class PerformanceTest {
  @Test
  public void testShortBufferAndArry() {
    try {
      Timer timer = new Timer();
      OpenNIDevice openni = new OpenNIDevice("test_data/config.xml");
      int width = openni.getDepthWidth();
      int height = openni.getDepthHeight();
      short[] depthArray = new short[width * height];
      BufferedImage image1 = null;
      BufferedImage image2 = null;
      
      long totalTime1 = 0, totalTime2 = 0;
      int times = 20;
      
      for (int i = 0; i <times; i++) {
        openni.waitDepthUpdateAll();
        ShortBuffer buffer = openni.getDepthBuffer();
        
        timer.tic();
        image1 = new BufferedImage(width, height, 
            BufferedImage.TYPE_USHORT_GRAY);
        ImageConvertUtils.shortBufferToGrayBufferedImage(buffer, image1);
        totalTime1 += timer.getToc();
  
        buffer.rewind();
        timer.tic();
        buffer.get(depthArray);
        image2 = new BufferedImage(width, height, 
            BufferedImage.TYPE_USHORT_GRAY);
        ImageConvertUtils.depthToGrayBufferedImage(depthArray, image2);
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
      OpenNIDevice openni = new OpenNIDevice(configFile);
      for (int i = 0; i < times; i++) {
        timer.tic();
        openni.waitDepthUpdateAll();
        totalTime += timer.getToc();
      }
      System.out.println(totalTime / times + 
                         "ms : OpenNI's jni implementation.");
      openni.release();
    } catch (GeneralException e) {
      e.printStackTrace();
      fail();
    }
    
    PartialOpenNIDevice openni = new PartialOpenNIDevice(configFile);
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
