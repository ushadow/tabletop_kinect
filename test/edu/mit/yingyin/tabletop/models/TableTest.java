package edu.mit.yingyin.tabletop.models;

import static org.junit.Assert.assertTrue;

import java.nio.FloatBuffer;

import org.junit.Test;

import edu.mit.yingyin.tabletop.models.Table;
import edu.mit.yingyin.util.DirectBufferUtil;

public class TableTest {
  private static final int WIDTH  = 10;
  private static final int HEIGHT = 10;
  
  @Test
  public void testTableContact() {
    FloatBuffer avg = DirectBufferUtil.allocateFloatBuffer(WIDTH * HEIGHT);
    FloatBuffer diff = DirectBufferUtil.allocateFloatBuffer(WIDTH * HEIGHT);
    avg.rewind();
    diff.rewind();
    while(avg.remaining() > 0)
      avg.put(1);
    while(diff.remaining() > 0)
      diff.put(1);
    
    Table table = new Table(avg, diff, WIDTH, WIDTH, WIDTH, HEIGHT);
    assertTrue(table.isInContact(0, 0, 1));
    assertTrue(table.isInContact(0, 1, (float)1.9));
    assertTrue(table.isInContact(1, 1, (float)0.1));
    assertTrue(!table.isInContact(2, 2, (float)6.3));
    assertTrue(!table.isInContact(3, 3, (float)-6.1));
  }
}
