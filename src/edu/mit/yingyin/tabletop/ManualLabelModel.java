package edu.mit.yingyin.tabletop;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rywang.util.ObjectIO;

import edu.mit.yingyin.image.ImageConvertUtils;

public class ManualLabelModel {
  private HashMap<Integer, List<Point>> points = 
      new HashMap<Integer, List<Point>>();
  
  private OpenNIWrapper openni;
  private BufferedImage image;
  private int[] depthRawData;
  private int width, height;
  /**
   * Current frame ID of the image from OpenNI.
   */
  private int frameID = 0;
  private int rate = 1;
  private int count = 0;
  
  public ManualLabelModel(String configFile) {
    openni = new OpenNIWrapper(configFile);
    width = openni.getDepthWidth();
    height = openni.getDepthHeight();
    depthRawData = new int[width * height];
    image = null;
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
  
  public int getImageWidth() { return width; }
  
  public int getImageHeight() { return height; }
  
  public int getFrameID() { return frameID; }
  
  /**
   * Returns the next image from OpenNI.
   * @return a gray image with brightness inversely related to depth value.
   */
  public BufferedImage nextImage() {
    openni.waitAnyUpdateAll();
    frameID++;
    while (frameID % rate != 0) {
      openni.waitAnyUpdateAll();
      frameID++;
    }
    openni.getDepthMap(depthRawData);
    image = ImageConvertUtils.depthToGrayBufferedImage(depthRawData, width,
        height);
    return image;
  }

  // Mutators
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
  
  public void increaseRate() { rate++; }
  
  public void decreaseRate() { rate--; }
  
  public void cleanUp() {
    openni.cleanUp();
  }
  
  public void save(String filename) throws IOException {
    ObjectIO.writeObject(points, filename);
  }
  
}
