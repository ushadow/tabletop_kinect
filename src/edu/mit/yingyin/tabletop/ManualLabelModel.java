package edu.mit.yingyin.tabletop;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.OpenNI.GeneralException;
import org.OpenNI.StatusException;

import rywang.util.ObjectIO;
import edu.mit.yingyin.image.ImageConvertUtils;

public class ManualLabelModel {
  private HashMap<Integer, List<Point>> points = new HashMap<Integer, List<Point>>();

  private OpenNIDevice openni;
  private BufferedImage image;
  private short[] depthRawData;
  private int width, height;
  /**
   * Current frame ID of the image from OpenNI.
   */
  private int frameID = 0;
  private int skip = 1;

  public ManualLabelModel(String configFile) {
    try {
      openni = new OpenNIDevice(configFile);
      width = openni.depthWidth();
      height = openni.depthHeight();
      depthRawData = new short[width * height];
      image = null;
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

  public int imageWidth() {
    return width;
  }

  public int imageHeight() {
    return height;
  }

  /**
   * @return The frame ID of the current image.
   */
  public int frameID() { return frameID; }

  /**
   * Returns the next image from OpenNI and updates the related information.
   * 
   * @return a gray image with brightness inversely related to depth value.
   * @throws StatusException 
   */
  public BufferedImage nextImage(boolean forward) throws StatusException {
    openni.seekFrame(forward ? skip : -skip);
    openni.waitDepthAndUpdateAll();
    ShortBuffer depthBuffer = openni.depthBuffer();
    depthBuffer.get(depthRawData);
    image = ImageConvertUtils.depthToGrayBufferedImage(depthRawData, width,
        height);
    frameID = openni.depthFrameID();
    return image;
  }

  // Mutators
  /**
   * Adds a point corresponding to the current image.
   * @param p point to be added corresponding to current image's frame ID.
   */
  public void addPoint(Point p) {
    List<Point> list = points.get(frameID);
    if (list == null) {
      list = new ArrayList<Point>();
      points.put(frameID, list);
    }
    list.add(p);
  }

  public void removeLastPoint() {
    List<Point> list = points.get(frameID);
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
