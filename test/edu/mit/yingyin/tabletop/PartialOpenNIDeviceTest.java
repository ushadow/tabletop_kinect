package edu.mit.yingyin.tabletop;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;

import org.junit.Test;

public class PartialOpenNIDeviceTest {

  @Test
  public void testInitFromFileAndUpdate() {
    PartialOpenNIDevice openniWrapper = 
        new PartialOpenNIDevice("test_data/config.xml");
    assertTrue(openniWrapper.initialized());
    assertEquals(640, openniWrapper.getDepthWidth());
    assertEquals(480, openniWrapper.getDepthHeight());
    assertTrue(openniWrapper.waitAnyUpdateAll());
    openniWrapper.waitAnyUpdateAll();
    int[] depthMap = 
        new int[openniWrapper.getDepthWidth() * openniWrapper.getDepthHeight()];
    openniWrapper.getDepthArray(depthMap);
    int index = 0;
    for (int h = 0; h < openniWrapper.getDepthHeight(); h++) {
      for (int w = 0; w < openniWrapper.getDepthWidth(); w++, index++) {
        int depth = depthMap[index];
        assertTrue(depth >= 0 && depth <= PartialOpenNIDevice.MAX_DEPTH);
      }
    }
    openniWrapper.release();
  }
  
  @Test
  public void testRawDepthToBufferedImage() {
    BufferedImage image = PartialOpenNIDevice.rawDepthToBufferedImage(
        "test_data/Depth_0.raw");
    assertEquals(PartialOpenNIDevice.DEFAULT_DEPTH_WIDTH, image.getWidth());
    assertEquals(PartialOpenNIDevice.DEFAULT_DEPTH_HEIGHT, image.getHeight());
    assertEquals(BufferedImage.TYPE_USHORT_GRAY, image.getType());
    short[] depthArray = ((DataBufferUShort)image.getRaster().getDataBuffer()).
        getData();
    int total = 0;
    for (int i = 0; i < image.getWidth() * image.getHeight(); i++)
      total += depthArray[i] & 0xffff;
    assertTrue(total > 0);
  }
}
