package edu.mit.yingyin.tabletop.controllers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import edu.mit.yingyin.calib.CalibFrame;
import edu.mit.yingyin.gui.ImageComponent;
import edu.mit.yingyin.tabletop.models.HandTracker.FingerEvent;
import edu.mit.yingyin.tabletop.models.HandTrackingEngine.IHandEventListener;
import edu.mit.yingyin.util.SystemUtil;

/**
 * Controls interaction with hand events.
 * @author yingyin
 *
 */
public class HandEventsController extends KeyAdapter 
    implements IHandEventListener {
  
  private class TestImageComponent extends ImageComponent {
    private static final long serialVersionUID = 5617635233634459251L;
    private static final int OVAL_WIDTH = 20;
    
    public TestImageComponent(Dimension d) {
      super(d);
    }
    
    public TestImageComponent(BufferedImage bi) {
      super(bi);
    }
    
    @Override
    public void paint(Graphics g) {
      super.paint(g);
      
      if (feList == null)
        return;
      
      Graphics2D g2d = (Graphics2D) g;
      g2d.setColor(Color.red);
      for (FingerEvent fe : feList) {
        Point2f p = scale(fe.posDisplay);
        Point pointInImageCoord = new Point((int) p.x, (int) p.y);
        SwingUtilities.convertPointFromScreen(pointInImageCoord, this);
        g2d.drawOval(pointInImageCoord.x - OVAL_WIDTH, 
            pointInImageCoord.y - OVAL_WIDTH, OVAL_WIDTH, OVAL_WIDTH);
        g2d.fillOval(pointInImageCoord.x - OVAL_WIDTH, 
            pointInImageCoord.y - OVAL_WIDTH, OVAL_WIDTH, OVAL_WIDTH);
      }
    }
    
    private Point2f scale(Point2f p) {
      Dimension d = SystemUtil.getVirtualScreenBounds().getSize();
      return new Point2f(p.x * d.width / tabletopRes.width,
          p.y * d.height / tabletopRes.height);
    }
  }
  
  private List<FingerEvent> feList;
  private CalibFrame frame;
  private final Dimension tabletopRes;
  
  /**
   * Constructs a <code>HandEventsController</code> with a specific displayed 
   * image.
   * 
   * @param image the image to be displayed.
   * @throws IOException
   */
  public HandEventsController(BufferedImage image, Dimension screenResolution) 
      throws IOException {
    tabletopRes = screenResolution;
    TestImageComponent ic = new TestImageComponent(scaleImage(image));
    frame = new CalibFrame(ic);
    frame.addKeyListener(this);
  }
  
  public void showUI() { frame.showUI(); }
  
  @Override
  public void fingerPressed(List<FingerEvent> feList) {
    this.feList = feList;
    frame.repaint();
  }
  
  @Override
  public void keyPressed(KeyEvent ke) {
    switch (ke.getKeyCode()) {
      case KeyEvent.VK_ESCAPE:
      case KeyEvent.VK_Q:
        frame.setVisible(false);
        break;
      default:
        break;
    }
  }
  
  public boolean isViewVisible() {
    return frame.isVisible();
  }

  private BufferedImage scaleImage(BufferedImage image) throws IOException {
    AffineTransform at = new AffineTransform();
    Dimension d = SystemUtil.getVirtualScreenBounds().getSize();
    double widthRatio = (double) d.width / tabletopRes.width;
    double heightRatio = (double) d.height / tabletopRes.height;
    at.scale(widthRatio, heightRatio);
    AffineTransformOp scaleOp = new AffineTransformOp(at, 
        AffineTransformOp.TYPE_BILINEAR);
    int scaledWidth = (int) (image.getWidth() * widthRatio);
    int scaledHeight = (int) (image.getHeight() * heightRatio);
    BufferedImage after = new BufferedImage(scaledWidth, scaledHeight, 
        BufferedImage.TYPE_INT_ARGB);
    scaleOp.filter(image, after);
    return after;
  }

  @Override
  public void fingerPointed(List<Point3f> points) {
    // TODO Auto-generated method stub
    
  }
}

