package edu.mit.yingyin.tabletop;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;

import org.junit.Test;

public class OpenNIWrapperTest {

  @Test
  public void testInitFromFileAndUpdate() {
    OpenNIWrapper openniWrapper = new OpenNIWrapper("test_data/config.xml");
    assertTrue(openniWrapper.initialized());
    assertEquals(640, openniWrapper.depthWidth());
    assertEquals(480, openniWrapper.depthHeight());
    assertTrue(openniWrapper.waitAnyUpdateAll());
    assertTrue(openniWrapper.waitDepthUpdateAll());
    int[] depthMap = 
        new int[openniWrapper.depthWidth() * openniWrapper.depthHeight()];
    openniWrapper.getDepthMap(depthMap);
    int index = 0;
    for (int h = 0; h < openniWrapper.depthHeight(); h++) {
      for (int w = 0; w < openniWrapper.depthWidth(); w++, index++) {
        int depth = depthMap[index];
        assertTrue(depth >= 0 && depth <= OpenNIWrapper.MAX_DEPTH);
      }
    }
    openniWrapper.release();
  }
  
  @Test
  public void testRawDepthToBufferedImage() {
    BufferedImage image = OpenNIWrapper.rawDepthToBufferedImage(
        "test_data/Depth_0.raw");
    assertEquals(OpenNIWrapper.DEFAULT_DEPTH_WIDTH, image.getWidth());
    assertEquals(OpenNIWrapper.DEFAULT_DEPTH_HEIGHT, image.getHeight());
    assertEquals(BufferedImage.TYPE_USHORT_GRAY, image.getType());
    short[] depthArray = ((DataBufferUShort)image.getRaster().getDataBuffer()).
        getData();
    int total = 0;
    for (int i = 0; i < image.getWidth() * image.getHeight(); i++)
      total += depthArray[i] & 0xffff;
    assertTrue(total > 0);
  }
}
