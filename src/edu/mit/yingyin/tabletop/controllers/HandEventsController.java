package edu.mit.yingyin.tabletop.controllers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.vecmath.Point2f;

import edu.mit.yingyin.gui.ImageComponent;
import edu.mit.yingyin.tabletop.models.HandTracker.FingerEvent;
import edu.mit.yingyin.tabletop.models.HandTracker.IHandEventListener;
import edu.mit.yingyin.util.SystemUtil;

/**
 * Controls interaction with hand events.
 * @author yingyin
 *
 */
public class HandEventsController extends KeyAdapter 
    implements IHandEventListener {
  
  private class HandEventsImageComponent extends ImageComponent {
    private static final long serialVersionUID = 5617635233634459251L;
    private static final int OVAL_WIDTH = 20;
    private static final int DISPLAY_WIDTH = 2560;
    private static final int DISPLAY_HEIGHT = 2048;
    
    public HandEventsImageComponent(Dimension d) {
      super(d);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      
      if (feList == null)
        return;
      
      Graphics2D g2d = (Graphics2D) g;
      g2d.setColor(Color.red);
      for (FingerEvent fe : feList)
      {
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
  
  /**
   * A frame to show visualization of hand events.
   * @author yingyin
   *
   */
  private class HandEventsFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String IMAGE_FILE_NAME = 
        "/afs/csail/u/y/yingyin/research/kinect/data/checkerboard.png";
    
    private ImageComponent ic;
    
    /**
     *  Creates a full screen frame.
     */
    public HandEventsFrame() {
      super("Hand events view");
      setUndecorated(true);
      setResizable(false);
      
      Dimension screenSize = SystemUtil.getVirtualScreenBounds().getSize();
      this.setBounds(new Rectangle(screenSize));
      this.setLocation(0, 0);
    
      ic = new HandEventsImageComponent(screenSize);
      try {
        ic.setImage(ImageIO.read(new File(IMAGE_FILE_NAME)));
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(-1);
      }
      getContentPane().add(ic);
    }
    
    public void showUI() {
      pack();
      setVisible(true);
    }
  }
  
  private HandEventsFrame handEventView = new HandEventsFrame();
  private List<FingerEvent> feList;
  
  public HandEventsController() {
    handEventView.addKeyListener(this);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        handEventView.showUI();
      }
    });
  }
  
  @Override
  public void fingerPressed(List<FingerEvent> feList) {
    this.feList = feList;
    handEventView.repaint();
  }
  
  @Override
  public void keyPressed(KeyEvent ke) {
    switch (ke.getKeyCode()) {
      case KeyEvent.VK_ESCAPE:
      case KeyEvent.VK_Q:
        handEventView.setVisible(false);
        break;
      default:
        break;
    }
  }
  
  public boolean isViewVisible() {
    return handEventView.isVisible();
  }
}
