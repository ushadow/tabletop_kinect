package edu.mit.yingyin.tabletop;

import static org.junit.Assert.*;

import org.OpenNI.GeneralException;
import org.junit.Test;

public class TableTest {

  @Test
  public void testAverageTableDepth() {
    try {
      OpenNIDevice openni = new OpenNIDevice("test_data/config.xml");
      openni.waitAndUpdateAll();
      int[] depthArray = new int[openni.depthWidth() * openni.depthHeight()];
      openni.depthArray(depthArray);
      Table table = new Table(depthArray, openni.depthWidth(), 
                              openni.depthHeight());
      int averageDepth = table.averageDepth();
      assertTrue(averageDepth > 0);
      System.out.println("Average table depth = " + averageDepth);
    } catch (GeneralException e) {
      System.err.println(e.getMessage());
      fail();
    }
  }

}
