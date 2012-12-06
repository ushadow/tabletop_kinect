package edu.mit.yingyin.tabletop.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.Timer;

import edu.mit.yingyin.gui.ImageComponent;
import edu.mit.yingyin.util.SystemUtil;

/**
 * Displays targets for testing.
 * @author yingyin
 *
 */
public class DisplayTargetFrame extends JFrame {
  private class TargetImageComponent extends ImageComponent {
    private static final long serialVersionUID = 7001378911756844295L;
    
    private Dimension d;
    public TargetImageComponent(BufferedImage img) {
      super(img);
      d = new Dimension(img.getWidth(), img.getHeight());
    }
    
    public void paint(Graphics g) {
      super.paint(g);
      Graphics2D g2d = (Graphics2D) g;
      g2d.setColor(Color.red);
      if (currentPointIndex < displayPoints.size()) {
        Point p = displayPoints.get(currentPointIndex);
        g2d.fillOval(p.x, p.y, 100, 100);
        currentPointIndex++;
      }
    }
  }

  private static final Logger LOGGER = Logger.getLogger(
      DisplayTargetFrame.class.getName());
  
  private static final long serialVersionUID = -8704714832139857227L;
  private static final int DISPLAY_INTERVAL = 2500; // milliseconds
  private static final int NROW = 2, NCOL = 5;
  
  private ImageComponent ic;
  private List<Point> displayPoints;
  private Dimension frameSize;
  private int currentPointIndex = 0;
  
  public DisplayTargetFrame() {
    super("Display Target");
    setUndecorated(true);
    setResizable(false);
    frameSize = SystemUtil.getVirtualScreenBounds().getSize();
    BufferedImage bi = new BufferedImage(frameSize.width, frameSize.height,
        BufferedImage.TYPE_3BYTE_BGR);
    ic = new TargetImageComponent(bi);
    getContentPane().add(ic);
    
    displayPoints = createDisplayPoints(frameSize);
    
    ActionListener al = new ActionListener() {
      
      @Override
      public void actionPerformed(ActionEvent e) {
        if (currentPointIndex < displayPoints.size())
          repaint();
      }
    };
    new Timer(DISPLAY_INTERVAL, al).start();
  }
  
  public void showUI() {
    pack();
    setVisible(true);
  }
  
  private List<Point> createDisplayPoints(Dimension bound) {
    List<Point> points = new ArrayList<Point>();
    int dx = bound.width / (NCOL + 1);
    int dy = bound.height / (NROW + 1);
    for (int i = 1; i <= NROW; i++)
      for (int j = 1; j <= NCOL; j++) {
        points.add(new Point(dx * j, dy * i));
      }
    return points;
  }
  
}
