package edu.mit.yingyin.tabletop;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.OpenNI.GeneralException;
import org.junit.Test;

public class TableTest {

  @Test
  public void testAverageTableDepth() {
    try {
      FullOpenNIDevice openni = new FullOpenNIDevice("test_data/config.xml");
      openni.waitAndUpdateAll();
      int[] depthArray = new int[openni.getDepthWidth() * openni.getDepthHeight()];
      openni.getDepthArray(depthArray);
      Table table = Table.instance();
      int averageDepth = table.getAverageDepth();
      assertTrue(averageDepth > 0);
      System.out.println("Average table depth = " + averageDepth);
    } catch (GeneralException e) {
      System.err.println(e.getMessage());
      fail();
    }
  }

}
