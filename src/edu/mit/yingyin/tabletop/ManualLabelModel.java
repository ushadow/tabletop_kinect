package edu.mit.yingyin.tabletop;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.OpenNI.GeneralException;

import com.googlecode.javacv.FrameGrabber.Array;

import rywang.util.ObjectIO;
import edu.mit.yingyin.image.ImageConvertUtils;

/**
 * Model for manual labeling of points per frame in a OpenNI recorded file.
 * @author yingyin
 *
 */
public class ManualLabelModel {
  /**
   * Points at each frame. Multiple points at each frame for tracking multiple
   * fingers. 
   */
  private HashMap<Integer, List<Point>> points;

  private FullOpenNIDevice openni;
  private BufferedImage depthImage, rgbImage;
  private short[] depthRawData;
  private int depthWidth, depthHeight, rgbWidth, rgbHeight;
  /**
   * Current frame ID of the image from OpenNI.
   */
  private int depthFrameID = 0, rgbFrameID = 0;
  private int skip = 1;
  private float[] histogram;

  /**
   * Creates the ManualLabelModel object.
   * 
   * @param configFile OpenNI configuration file.
   * @param replayFilename Recorded file with tracked points at each frame.  
   * @throws IOException If reading from <code>replayFilename</code> fails.
   * @throws GeneralException If initializationg from OpenNI device fails.
   */
  @SuppressWarnings("unchecked")
  public ManualLabelModel(String configFile, String replayFilename) 
      throws IOException, GeneralException {
    openni = new FullOpenNIDevice(configFile);
    depthWidth = openni.getDepthWidth();
    depthHeight = openni.getDepthHeight();
    rgbWidth = openni.getImageWidth();
    rgbHeight = openni.getImageHeight();
    rgbImage = new BufferedImage(rgbWidth, rgbHeight, 
                                 BufferedImage.TYPE_3BYTE_BGR);
    depthImage = new BufferedImage(depthWidth, depthHeight, 
                                   BufferedImage.TYPE_USHORT_GRAY);
    depthRawData = new short[depthWidth * depthHeight];
    if (replayFilename != null) 
      points = ((HashMap<Integer, List<Point>>)
          ObjectIO.readObject(replayFilename));
    else
      points = new HashMap<Integer, List<Point>>();
  
    histogram = new float[HandAnalyzer.MAX_DEPTH];
  }

  // Accessors
  public List<Point> getPoints(int frameID) {
    List<Point> newList = new ArrayList<Point>();
    List<Point> list = points.get(frameID);
    if (list != null && !list.isEmpty()) {
      for (Point p : list)
        newList.add(new Point(p));
    }
    return newList;
  }

  public int depthWidth() { return depthWidth; }

  public int depthHeight() { return depthHeight; }

  public int rgbWidth() { return rgbWidth; }
  
  public int rgbHeight() { return rgbHeight; }
  
  /**
   * @return The frame ID of the current image.
   */
  public int depthFrameID() { return depthFrameID; }

  public int rgbFrameID() { return rgbFrameID; }
  
  public BufferedImage depthImage() { return depthImage; }
  
  public BufferedImage rgbImage() { return rgbImage; }
  
  // Mutators
  /**
   * Returns the next image from OpenNI and updates the related information.
   * 
   * @return a gray image with brightness inversely related to depth value.
   * @throws GeneralException 
   */
  public void update(boolean forward) throws GeneralException {
    openni.seekFrameBy(forward ? skip : -skip);
    openni.waitAndUpdateAll();
    depthFrameID = openni.getDepthFrameID();
    rgbFrameID = openni.getImageFrameID();
    while (rgbFrameID != depthFrameID) {
      if (rgbFrameID < depthFrameID)
        openni.waitImageUpdateAll();
      else openni.waitDepthUpdateAll();
      depthFrameID = openni.getDepthFrameID();
      rgbFrameID = openni.getImageFrameID();
    }
    
    openni.getDepthArray(depthRawData);
    ImageConvertUtils.arrayToHistogram(depthRawData, histogram);
    System.out.println(Arrays.toString(histogram));
    ImageConvertUtils.histogramToGrayBufferedImage(depthRawData, histogram, 
        depthImage);
    ImageConvertUtils.byteBuffer2BufferedImage(openni.getImageBuffer(), 
        rgbImage);
    depthFrameID = openni.getDepthFrameID();
    rgbFrameID = openni.getImageFrameID();
  }

  /**
   * Adds a point corresponding to the current image.
   * @param p point to be added corresponding to current image's frame ID.
   */
  public void addPoint(Point p) {
    List<Point> list = points.get(depthFrameID);
    if (list == null) {
      list = new ArrayList<Point>();
      points.put(depthFrameID, list);
    }
    list.add(p);
  }

  public void removeLastPoint() {
    List<Point> list = points.get(depthFrameID);
    if (list != null && !list.isEmpty())
      list.remove(list.size() - 1);
  }

  public void increaseRate() {
    skip++;
  }

  public void decreaseRate() {
    if (skip > 1)
      skip--;
  }

  public void release() {
    openni.release();
  }

  public void save(String filename) throws IOException {
    ObjectIO.writeObject(points, filename);
  }
  
  public int skipRate() { return skip; }

}
