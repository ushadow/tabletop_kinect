package edu.mit.yingyin.util;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class SystemUtil {

	public static boolean isLittleEndianNative() {
		ByteOrder bo = ByteOrder.nativeOrder();
		if(bo.equals(ByteOrder.LITTLE_ENDIAN))
			return true;
		
		return false;
	}
	
	public static boolean isLittleEndianJVM() {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.put((byte)1);
		bb.put((byte)0);
		bb.put((byte)0);
		bb.put((byte)0);
		bb.rewind();
		IntBuffer ib = bb.asIntBuffer();
		ib.rewind();
		int i = ib.get();
		System.out.println(i);
		if(i == 1)
			return true;
		
		return false;
	}
	
	public static Rectangle getVirtualScreenBounds() {
	  GraphicsEnvironment ge;
	  ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	  
	  Rectangle bounds = new Rectangle();
	  
	  GraphicsDevice[] gdArray = ge.getScreenDevices();
	  for (GraphicsDevice gd : gdArray) {
	    GraphicsConfiguration[] gcArray = gd.getConfigurations();
	    for (GraphicsConfiguration gc : gcArray) {
	      bounds = bounds.union(gc.getBounds()); 
	    }
	  }
	  return bounds;
	}
	
	public static void main(String[] args) {
		System.out.println(getVirtualScreenBounds());
	}
}
