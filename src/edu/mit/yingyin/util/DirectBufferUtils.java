/*
 * Created on Jan 11, 2006
 */
package edu.mit.yingyin.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class DirectBufferUtils {
  public static FloatBuffer allocateFloatBuffer(int numFloats) {

    ByteBuffer bb;
    int allocationSize = 4 * numFloats;
    bb = ByteBuffer.allocateDirect(allocationSize);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    return bb.asFloatBuffer();
  }
  
  public static DoubleBuffer allocateDoubleBuffer(int numDoubles) {

    ByteBuffer bb;
    int allocationSize = 8 * numDoubles;
    bb = ByteBuffer.allocateDirect(allocationSize);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    return bb.asDoubleBuffer();
  }

  public static IntBuffer allocateIntBuffer(int numInts) {

    ByteBuffer bb;
    int allocationSize = 4 * numInts;
    bb = ByteBuffer.allocateDirect(allocationSize);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    return bb.asIntBuffer();
  }

  public static ByteBuffer allocateByteBuffer(int numBytes) {

    ByteBuffer bb;
    int allocationSize = numBytes;
    bb = ByteBuffer.allocateDirect(allocationSize);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    return bb;
  }
}
