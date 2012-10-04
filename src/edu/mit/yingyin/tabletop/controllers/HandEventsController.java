package edu.mit.yingyin.tabletop.controllers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.vecmath.Point2f;

import edu.mit.yingyin.calib.CalibFrame;
import edu.mit.yingyin.gui.ImageComponent;
import edu.mit.yingyin.tabletop.models.HandTracker.FingerEvent;
import edu.mit.yingyin.tabletop.models.HandTrackingEngine.IHandEventListener;

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
        g2d.drawOval((int) p.x - OVAL_WIDTH, 
            (int) p.y - OVAL_WIDTH, OVAL_WIDTH, OVAL_WIDTH);
        g2d.fillOval((int) p.x - OVAL_WIDTH, 
            (int) p.y - OVAL_WIDTH, OVAL_WIDTH, OVAL_WIDTH);
      }
    }
    
    private Point2f scale(Point2f p) {
      Rectangle bounds = getBounds();
      return new Point2f(p.x * bounds.width / DISPLAY_WIDTH,
          p.y * bounds.height / DISPLAY_HEIGHT);
    }
  }
  private static final int DISPLAY_WIDTH = 2560;
  private static final int DISPLAY_HEIGHT = 2048;
    
    
//    private BufferedImage scaledImage() throws IOException {
//      BufferedImage image = ImageIO.read(new File(IMAGE_FILE_NAME));
//      AffineTransform at = new AffineTransform();
//      Rectangle bounds = getBounds(); 
//      at.scale((float) bounds.width / DISPLAY_WIDTH, 
//               (float) bounds.height / DISPLAY_HEIGHT);
//      AffineTransformOp scaleOp = new AffineTransformOp(at, 
//          AffineTransformOp.TYPE_BILINEAR);
//      BufferedImage after = new BufferedImage(image.getWidth(), 
//          image.getHeight(), BufferedImage.TYPE_INT_ARGB);
//      scaleOp.filter(image, after);
//      return after;
//    }
  
  private List<FingerEvent> feList;
  private CalibFrame frame;
  
  /**
   * Constructs a <code>HandEventsController</code> with a specific displayed 
   * image.
   * 
   * @param image the image to be displayed.
   * @throws IOException
   */
  public HandEventsController(BufferedImage image) throws IOException {
    TestImageComponent ic = new TestImageComponent(image);
    frame = new CalibFrame(ic);
    frame.addKeyListener(this);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        frame.showUI();
      }
    });
  }
  
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
}
