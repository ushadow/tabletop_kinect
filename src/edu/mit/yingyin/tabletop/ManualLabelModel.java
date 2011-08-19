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
  private HashMap<Integer, List<Point>> points = 
      new HashMap<Integer, List<Point>>();
  
  private OpenNI openni;
  private BufferedImage image;
  private short[] depthRawData;
  private int width, height;
  /**
   * Current frame ID of the image from OpenNI.
   */
  private int frameID = 0;
  private int rate = 1;
  private int count = 0;
  
  public ManualLabelModel(String configFile) {
    try {
      openni = new OpenNI(configFile);
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
  
  public int imageWidth() { return width; }
  
  public int imageHeight() { return height; }
  
  public int frameID() { return frameID; }
  
  /**
   * Returns the next image from OpenNI.
   * @return a gray image with brightness inversely related to depth value.
   */
  public BufferedImage nextImage() {
//    frameID++;
//    while (frameID % rate != 0) {
//      openni.waitAnyUpdateAll();
//      frameID++;
//    }
//    openni.epthMap(depthRawData);
//    image = ImageConvertUtils.depthToGrayBufferedImage(depthRawData, width,
//        height);
    try {
      openni.waitDepthAndUpdateAll();
      ShortBuffer depthBuffer = openni.depthBuffer();
      depthBuffer.get(depthRawData);
      image = ImageConvertUtils.depthToGrayBufferedImage(depthRawData, width, height);
//      image = ImageConvertUtils.depthToGrayBufferedImage(depthBuffer, width, 
//                                                         height);
      frameID = openni.depthFrameID();
    } catch (StatusException e) {
      System.err.println(e.getMessage());
      System.exit(-1);
    }
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
  
  public void release() {
    openni.release();
  }
  
  public void save(String filename) throws IOException {
    ObjectIO.writeObject(points, filename);
  }
  
}
