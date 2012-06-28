package edu.mit.yingyin.image;

/*
 * Image conversion utilities.
 * 
 * Copyright (c) 2006 Jean-Sebastien Senecal (js@drone.ws)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA.
 */

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.PixelGrabber;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import javax.swing.ImageIcon;

public class ImageConvertUtils {

  /**
   * Converts an AWT Image into a grayscale BufferedImage.
   * 
   * @param image the image to convert
   * @return a BufferedImage
   */
  public static BufferedImage toGrayBufferedImage(Image image) {
    if (image instanceof BufferedImage
        && ((BufferedImage) image).getType() == BufferedImage.TYPE_BYTE_GRAY)
      return (BufferedImage) image;

    // This code ensures that all the pixels in the image are loaded
    image = new ImageIcon(image).getImage();
    // Create a buffered image with a format that's compatible with the screen
    BufferedImage bimage = new BufferedImage(image.getWidth(null),
        image.getHeight(null), BufferedImage.TYPE_BYTE_GRAY);

    // Copy image to buffered image
    Graphics g = bimage.createGraphics();

    // Paint the image onto the buffered image
    g.drawImage(image, 0, 0, null);
    g.dispose();

    return bimage;
  }

  /**
   * Converts an AWT Image into a BufferedImage, with specific type.
   * 
   * @param image the image to convert
   * @param type the BufferedImage type
   * @return a BufferedImage with the contents of the image
   */
  public static BufferedImage toBufferedImage(Image image, int type) {
    if (image instanceof BufferedImage) {
      return convertType((BufferedImage) image, type);
    }

    // This code ensures that all the pixels in the image are loaded
    image = new ImageIcon(image).getImage();

    // Create a buffered image with a format that's compatible with the screen
    BufferedImage bimage = new BufferedImage(image.getWidth(null),
        image.getHeight(null), type);

    // Copy image to buffered image
    Graphics g = bimage.createGraphics();

    // Paint the image onto the buffered image
    g.drawImage(image, 0, 0, null);
    g.dispose();

    return bimage;
  }

  /**
   * Converts an AWT Image into a BufferedImage.
   * 
   * @param image the image to convert
   * @return a BufferedImage with the contents of the image
   */
  public static BufferedImage toBufferedImage(Image image) {
    if (image instanceof BufferedImage) {
      return (BufferedImage) image;
    }

    // This code ensures that all the pixels in the image are loaded
    image = new ImageIcon(image).getImage();

    // Determine if the image has transparent pixels; for this method's
    // implementation, see e661 Determining If an Image Has Transparent Pixels
    boolean hasAlpha = hasAlpha(image);

    // Create a buffered image with a format that's compatible with the screen
    BufferedImage bimage = null;
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    try {
      // Determine the type of transparency of the new buffered image
      int transparency = hasAlpha ? Transparency.BITMASK : Transparency.OPAQUE;

      // Create the buffered image
      GraphicsDevice gs = ge.getDefaultScreenDevice();
      GraphicsConfiguration gc = gs.getDefaultConfiguration();
      bimage = gc.createCompatibleImage(image.getWidth(null),
          image.getHeight(null), transparency);
    } catch (HeadlessException e) {
      // The system does not have a screen
    }

    if (bimage == null) {
      // Create a buffered image using the default color model
      int type = hasAlpha ? BufferedImage.TYPE_INT_ARGB
          : BufferedImage.TYPE_INT_RGB;
      bimage = new BufferedImage(image.getWidth(null), image.getHeight(null),
          type);
    }

    // Copy image to buffered image
    Graphics g = bimage.createGraphics();

    // Paint the image onto the buffered image
    g.drawImage(image, 0, 0, null);
    g.dispose();

    return bimage;
  }

  public static BufferedImage toOpaque(BufferedImage src) {
    int w = src.getWidth();
    int h = src.getHeight();
    BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    int[] alpha = new int[w * h];
    for (int i = 0; i < alpha.length; ++i)
      alpha[i] = Transparency.OPAQUE;
    int buf[] = new int[w];
    for (int y = 0; y < h; y++) {
      src.getRGB(0, y, w, 1, buf, 0, w);
      dest.setRGB(0, y, w, 1, buf, 0, w);
    }
    dest.getAlphaRaster().setPixels(0, 0, w, h, alpha);
    return dest;
  }

  /**
   * Converts a BufferedImage to the specified type.
   * 
   * @param src the source image
   * @param type the BufferedImage type
   * @return a BufferedImage with the right type
   */
  public static BufferedImage convertType(BufferedImage src, int type) {
    if (src.getType() == type)
      return src;

    int w = src.getWidth();
    int h = src.getHeight();
    BufferedImage image = new BufferedImage(w, h, type);
    Graphics2D g2 = image.createGraphics();
    g2.drawRenderedImage(src, null);
    g2.dispose();
    return image;
  }

  /**
   * Convert pixels from java default ARGB int format to byte array in ABGR
   * format.
   * 
   * @param pixels the pixels to convert
   */
  public static void convertARGBtoABGR(int[] pixels) {
    int p, r, g, b, a;
    for (int i = 0; i < pixels.length; i++) {
      p = pixels[i];
      a = (p >> 24) & 0xFF; // get pixel bytes in ARGB order
      r = (p >> 16) & 0xFF;
      g = (p >> 8) & 0xFF;
      b = (p >> 0) & 0xFF;
      pixels[i] = (a << 24) | (b << 16) | (g << 8) | (r << 0);
    }
  }

  /**
   * Convert pixels from java default ABGR int format to byte array in ARGB
   * format.
   * 
   * @param pixels the pixels to convert
   */
  public static void convertABGRtoARGB(int[] pixels) {
    int p, r, g, b, a;
    for (int i = 0; i < pixels.length; i++) {
      p = pixels[i];
      a = (p >> 24) & 0xFF; // get pixel bytes in ARGB order
      b = (p >> 16) & 0xFF;
      g = (p >> 8) & 0xFF;
      r = (p >> 0) & 0xFF;
      pixels[i] = (a << 24) | (r << 16) | (g << 8) | (b << 0);
    }
  }

  /**
   * Returns true if the specified image has transparent pixels.
   * 
   * @param image the image to test
   * @return true if the specified image has transparent pixels
   */
  public static boolean hasAlpha(Image image) {
    // If buffered image, the color model is readily available
    if (image instanceof BufferedImage) {
      BufferedImage bimage = (BufferedImage) image;
      return bimage.getColorModel().hasAlpha();
    }

    // Use a pixel grabber to retrieve the image's color model;
    // grabbing a single pixel is usually sufficient
    PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
    try {
      pg.grabPixels();
    } catch (InterruptedException e) {
    }

    // Get the image's color model
    ColorModel cm = pg.getColorModel();
    return cm.hasAlpha();
  }

  /**
   * Convert IntBuffer to BufferedImage. The size of IntBuffer should equal to
   * the size of BufferedImage, i.e. ib.capacity() == img.getWidth() *
   * img.getHeight()
   * 
   * @author Ying Yin
   * 
   * @param ib each 4-byte (ARGB) integer in IntBuffer corresponds to a pixel in
   *          the BufferedImage ib should have width*height of integers. ib is
   *          little endian so the byte order is BGRA. ib is a direct buffer
   *          which has no backup array, so ib.array() will fail.
   * @param bi BufferedImage should have type TYPE_INT_ARGB
   */
  public static void intBuffer2BufferedImage(IntBuffer ib, BufferedImage img) {
    int[] rgbArray = new int[ib.capacity()];
    ib.rewind();
    ib.get(rgbArray);
    img.setRGB(0, 0, img.getWidth(), img.getHeight(), rgbArray, 0,
        img.getWidth());
  }
  
  /**
   * Converts a <code>ByteBuffer</code> to a <code>BufferedImage</code>.
   * @param bb a ByteBuffer with each byte representing R, G, B color bytes.
   * @param bi a BufferedImage of TYPE_3BYTE_BGR. The size of the ByteBuffer 
   *    must equal to the number of pixels of the BufferedImage * 3.
   */
  public static void byteBuffer2BufferedImage(ByteBuffer bb, BufferedImage bi) {
    final int bytesPerPixel = 3;
    byte[] imageArray = ((DataBufferByte)bi.getRaster().getDataBuffer()).
        getData();
    bb.rewind();
    bb.get(imageArray);
    int numPixels = bb.capacity() / bytesPerPixel;
    for (int i = 0; i < numPixels; i++) {
      byte tmp = imageArray[i * bytesPerPixel];
      imageArray[i * bytesPerPixel] = imageArray[i * bytesPerPixel + 2];
      imageArray[i * bytesPerPixel + 2] = tmp;
    }
  } 

  /**
   * Converts float values to an unsigned short <code>BufferedImage</code>.
   * 
   * @param floatBuffer contains float values. The size of the buffer must be at
   *    least as big as the size of the image.
   * @param bi <code>BufferedImage</code> with type TYPE_USHORT_GRAY. 
   * @param widthStep number of float values per row in 
   *    <code>floatBuffer</code>.
   * @return
   */
  public static BufferedImage floatBuffer2UShortGrayBufferedImage(
      FloatBuffer floatBuffer, BufferedImage bi, int widthStep) {
    final int MAX = 65535;
    float min = Float.MAX_VALUE;
    float max = Float.MIN_VALUE;
    int width = bi.getWidth();
    int height = bi.getHeight();
    for (int h = 0; h < height; h++)
      for (int w = 0; w < width; w++) {
        float value = floatBuffer.get(h * widthStep + w);
        min = Math.min(min, value);
        max = Math.max(max, value);
      }
    short[] array = ((DataBufferUShort)bi.getRaster().getDataBuffer()).
        getData();
    
    float range = max - min;
    if (range == 0)
      range = 1;
    for (int h = 0; h < height; h++)
      for (int w = 0; w < width; w++) {
        float value = floatBuffer.get(h * widthStep + w);
        int converted = Math.round(((value - min) * MAX / range));
        array[h * width + w] = (short)converted;
      }
    return bi;
  }
  
  
  /**
   * Converts an array of depth values to a gray BufferedImage.
   * 
   * The size of the array must equal to the product of width and height.
   * 
   * @param depth an integer array of depth values.
   * @param width width of the image returned.
   * @param height height of the image returned.
   * @return a gray BufferedImage such that the brightness of each pixel is
   *         <i>inversely</i> proportional to the depth from the camera.
   */
  public static BufferedImage depthToGrayBufferedImage(int[] depth, int width,
      int height) {
    final int MAX_DEPTH = 65535;
    BufferedImage image = new BufferedImage(width, height,
        BufferedImage.TYPE_USHORT_GRAY);
    short[] imageArray = ((DataBufferUShort) image.getRaster().getDataBuffer()).
        getData();
    int totalPixels = width * height;
    int max = 0;
    int min = MAX_DEPTH; // Two bytes.
    for (int i = 0; i < totalPixels; i++) {
      int value = depth[i];
      if (value != 0) {
        max = Math.max(max, value);
        min = Math.min(min, value);
      }
    }

    if (min == max) {
      Arrays.fill(imageArray, (short) 0);
    } else {
      for (int i = 0; i < totalPixels; i++) {
        int value = depth[i];
        imageArray[i] = value == 0 ? 0
            : (short) ((max - value) * MAX_DEPTH / (max - min));
      }
    }
    return image;
  }

  public static void depthToGrayBufferedImage(short[] depth, BufferedImage bi) {
    final int MAX_DEPTH = 65535;
    short[] imageArray = ((DataBufferUShort) bi.getRaster().getDataBuffer()).
        getData();
    int totalPixels = bi.getWidth() * bi.getHeight();
    int max = 0;
    int min = MAX_DEPTH; // Two bytes.
    for (int i = 0; i < totalPixels; i++) {
      int value = depth[i] & 0x0000ffff;
      if (value != 0) {
        max = Math.max(max, value);
        min = Math.min(min, value);
      }
    }

    if (min == max) {
      Arrays.fill(imageArray, (short) 0);
    } else {
      for (int i = 0; i < totalPixels; i++) {
        int value = depth[i] & 0x0000ffff;
        imageArray[i] = value == 0 ? 0
            : (short) ((max - value) * MAX_DEPTH / (max - min));
      }
    }
  }

  /**
   * Converts depth values in <code>ShortBuffer</code> to a gray scale 
   * <code>BufferedImage</code>.
   * @param buffer
   * @param bi <BufferedImage> of with type <code>TYPE_USHORT_GRAY</code>
   */
  public static void depthToGrayBufferedImage(ShortBuffer buffer,
      BufferedImage bi) {
    final int MAX_DEPTH = 65535;
    short[] imageArray = ((DataBufferUShort) bi.getRaster().getDataBuffer()).
        getData();
    buffer.rewind();
    int max = 0;
    int min = MAX_DEPTH; // Two bytes.
    while (buffer.remaining() > 0) {
      int value = buffer.get() & 0x0000ffff;
      if (value != 0) {
        max = Math.max(max, value);
        min = Math.min(min, value);
      }
    }

    if (min == max) {
      Arrays.fill(imageArray, (short) 0);
    } else {
      buffer.rewind();
      while (buffer.remaining() > 0) {
        int pos = buffer.position();
        int value = buffer.get() & 0x0000ffff;
        imageArray[pos] = value == 0 ? 0
            : (short) ((max - value) * MAX_DEPTH / (max - min));
      }
    }
  }
  
  /**
   * Converts an array of integer values into a cumulative historgram.
   * @param array an integer array.
   * @param histogram keeps the cumulative frequencies of values in the 
   *    <code>array</code> in the range (0, length of <code>histogram</code>).
   */
  public static void arrayToHistogram(int[] array, float[] histogram) {
    Arrays.fill(histogram, 0);
    
    int totalPoints = 0;
    for (int v : array) {
      if (v > 0 && v < histogram.length) {
        histogram[v]++;
        totalPoints++;
      }
    }
    
    for (int i = 1; i < histogram.length; i++) 
      histogram[i] += histogram[i - 1];
  
    if (totalPoints > 0) {
      for (int i = 1; i < histogram.length; i++)
        histogram[i] = (totalPoints - histogram[i]) / (float)totalPoints;
    }
  }
  
  /**
   * Converts an array of short values into a cumulative historgram.
   * @param array a short array.
   * @param histogram keeps the cumulative frequencies of values in the 
   *    <code>array</code> in the range (0, length of <code>histogram</code>).
   */
  public static void arrayToHistogram(short[] array, float[] histogram) {
    Arrays.fill(histogram, 0);

    int totalPoints = 0;
    for (short a : array) {
      int v = a & 0x0000ffff;
      if (v > 0 && v < histogram.length) {
        histogram[v]++;
        totalPoints++;
      }
    }
    
    for (int i = 1; i < histogram.length; i++) 
      histogram[i] += histogram[i - 1];
  
    if (totalPoints > 0) {
      for (int i = 1; i < histogram.length; i++)
        histogram[i] = (totalPoints - histogram[i]) / (float)totalPoints;
    }
  }
  
  /**
   * Converts an array of short values to <code>BufferedImage</code> with type
   * TYPE_USHORT_GRAY based on the <code>histogram</code>.
   * 
   * @param array the array of short values.
   * @param histogram cumulative frequencies of the values in the <code>array
   *    </code>.
   * @param bi converted <code>BufferedImage</code> with type TYPE_USHORT_GRAY. 
   *    The higher the frequency, the greater the pixel value.
   */
  public static void histogramToBufferedImageUShort(short[] array, 
      float[] histogram, BufferedImage bi) {
    short[] imageArray = ((DataBufferUShort) bi.getRaster().getDataBuffer()).
        getData();
    int totalPixels = bi.getWidth() * bi.getHeight();
    for (int i = 0; i < totalPixels; i++) {
      short a = array[i];
      int v = a & 0x0000ffff;
      if (v < 0 || v > histogram.length)
        v = 0;
      imageArray[i] = (short)(histogram[v] * 65535);
    }
  }
}
