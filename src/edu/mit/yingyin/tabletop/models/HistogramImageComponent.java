package edu.mit.yingyin.tabletop.models;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentLinkedQueue;

import edu.mit.yingyin.gui.ImageComponent;
import edu.mit.yingyin.image.ImageConvertUtils;

public class HistogramImageComponent extends ImageComponent {
  private static class Label {
    public Color color;
    public Point point;
    
    public Label(Point p, Color c) {
      point = p;
      color = c;
    }
  }
  
  private static final long serialVersionUID = -5149221395117722234L;
  private static final int OVAL_WIDTH = 10;
  private final float[] histogram;
  private final int width, height;
  private ConcurrentLinkedQueue<Label> labels = 
      new ConcurrentLinkedQueue<Label>();

  /**
   * 
   * @param d caller should not change {@code: d} after calling the constructor.
   * @param max
   */
  public HistogramImageComponent(int width, int height, int max) {
    super(new Dimension(width, height));
    histogram = new float[max + 1];
    this.width = width;
    this.height = height;
    setImage(new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY));
  }
  
  public void setImage(int[] image) {
    if (image.length != height * width) {
      throw new IllegalArgumentException("The size of input image is not " +
      		"equal to the size of the image to be displayed.");
    }
    ImageConvertUtils.arrayToHistogram(image, histogram);
    ImageConvertUtils.histogramToBufferedImageUShort(image, histogram, img);
    labels.clear();
    repaint();
  }
  
  public void addLabel(Point point, Color color) { 
    labels.add(new Label(point, color));
    repaint();
  }
  
  @Override
  public void paint(Graphics g) {
    super.paint(g);
    
    Graphics2D g2d = (Graphics2D) g;
    for (Label l : labels) {
      g2d.setColor(l.color);
      Point point = l.point;
      g2d.drawOval(point.x - OVAL_WIDTH / 2, point.y - OVAL_WIDTH / 2, 
                   OVAL_WIDTH, OVAL_WIDTH);
    }
  }
}
