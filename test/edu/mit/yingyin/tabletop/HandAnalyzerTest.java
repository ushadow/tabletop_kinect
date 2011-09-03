package edu.mit.yingyin.tabletop;

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.Test;

public class HandAnalyzerTest {

  @Test
  public void testSubtractBackground() {
    int width = 100;
    int height = 100;
    HandAnalyzer handAnalyzer = new HandAnalyzer(width, height);
    ProcessPacket packet = new ProcessPacket(width, height);
    BufferedImage bi = packet.depthImage.getBufferedImage();
    assertEquals(width, bi.getWidth());
    assertEquals(height, bi.getHeight());

    Arrays.fill(packet.depthRawData, 500);
    handAnalyzer.subtractBackground(packet);
    ByteBuffer ib = packet.depthImage.getByteBuffer();
    for (int i = 0; i < packet.depthRawData.length; i++) 
      assertEquals(0, ib.get(i));
    
    Arrays.fill(packet.depthRawData, 500);
    handAnalyzer.subtractBackground(packet);
    ib = packet.depthImage.getByteBuffer();
    for (int i = 0; i < packet.depthRawData.length; i++) 
      assertEquals(0, ib.get(i));
    
    Arrays.fill(packet.depthRawData, 499);
    handAnalyzer.subtractBackground(packet);
    ib = packet.depthImage.getByteBuffer();
    for (int i = 0; i < packet.depthRawData.length; i++) 
      assertEquals(0, ib.get(i));
    
    Arrays.fill(packet.depthRawData, 490);
    handAnalyzer.subtractBackground(packet);
    ib = packet.depthImage.getByteBuffer();
    for (int i = 0; i < packet.depthRawData.length; i++) 
      assertEquals(78, ib.get(i));
    
    Arrays.fill(packet.depthRawData, 555);
    handAnalyzer.subtractBackground(packet);
    ib = packet.depthImage.getByteBuffer();
    for (int i = 0; i < packet.depthRawData.length; i++) 
      assertEquals(0, ib.get(i));
  }
}
