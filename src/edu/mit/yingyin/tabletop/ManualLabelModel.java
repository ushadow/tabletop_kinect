package edu.mit.yingyin.tabletop;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.OpenNI.GeneralException;

import rywang.util.ObjectIO;
import edu.mit.yingyin.image.ImageConvertUtils;

public class ManualLabelModel {
  /**
   * Points at each frame. Multiple points at each frame for tracking multiple
   * fingers. 
   */
  private HashMap<Integer, List<Point>> points;

  private OpenNIDevice openni;
  private BufferedImage depthImage, rgbImage;
  private short[] depthRawData;
  private int depthWidth, depthHeight, rgbWidth, rgbHeight;
  /**
   * Current frame ID of the image from OpenNI.
   */
  private int depthFrameID = 0, rgbFrameID = 0;
  private int skip = 1;

  @SuppressWarnings("unchecked")
  public ManualLabelModel(String configFile, String replayFilename) 
      throws IOException {
    try {
      openni = new OpenNIDevice(configFile);
      depthWidth = openni.depthWidth();
      depthHeight = openni.depthHeight();
      rgbWidth = openni.imageWidth();
      rgbHeight = openni.imageHeight();
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
    } catch (GeneralException e) {
      System.err.println(e.getMessage());
      System.exit(-1);
    }
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
    openni.seekFrame(forward ? skip : -skip);
    openni.waitAndUpdateAll();
    depthFrameID = openni.depthFrameID();
    rgbFrameID = openni.imageFrameID();
    while (rgbFrameID != depthFrameID) {
      if (rgbFrameID < depthFrameID)
        openni.waitImageUpdateAll();
      else openni.waitDepthUpdateAll();
      depthFrameID = openni.depthFrameID();
      rgbFrameID = openni.imageFrameID();
    }
    
    openni.depthArray(depthRawData);
    ImageConvertUtils.depthToGrayBufferedImage(depthRawData, depthImage);
    ImageConvertUtils.byteBuffer2BufferedImage(openni.imageBuffer(), rgbImage);
    depthFrameID = openni.depthFrameID();
    rgbFrameID = openni.imageFrameID();
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
