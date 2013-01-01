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

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.vecmath.Point2f;

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
    private static final int OVAL_RADIUS = 50;
    
    public TargetImageComponent(BufferedImage img) {
      super(img);
    }
    
    public void paint(Graphics g) {
      super.paint(g);
      Graphics2D g2d = (Graphics2D) g;
      g2d.setColor(Color.red);
      if (currentPointIndex >= 0 && currentPointIndex < displayPoints.size()) {
        Point p = displayPoints.get(currentPointIndex);
        g2d.fillOval(p.x - OVAL_RADIUS, p.y - OVAL_RADIUS, OVAL_RADIUS * 2, 
                     OVAL_RADIUS * 2);
      }
      if (pintsToDraw != null) {
        g2d.setColor(Color.blue);
        for (Point2f point : pintsToDraw) {
          Point scaled = scale(point);
          SwingUtilities.convertPointFromScreen(scaled, this);
          g2d.fillOval(scaled.x - OVAL_RADIUS, scaled.y - OVAL_RADIUS, 
              OVAL_RADIUS * 2, OVAL_RADIUS * 2);
        }
      }
    }
  }

  private static final long serialVersionUID = -8704714832139857227L;
  private static final int DISPLAY_INTERVAL = 2500; // milliseconds
  private static final int NROW = 2, NCOL = 5;
  
  private final ImageComponent ic;
  private final List<Point> displayPoints;
  private final Dimension frameSize, tableScreenSize;
  private int currentPointIndex = -1;
  private final Timer timer;
  private Point2f[] pintsToDraw;
  
  public DisplayTargetFrame(Dimension tableScreenRes) {
    super("Display Target");
    this.tableScreenSize = tableScreenRes;

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
        currentPointIndex++;
        if (currentPointIndex < displayPoints.size())
          repaint();
      }
    };
    timer = new Timer(DISPLAY_INTERVAL, al);
  }
  
  public void showUI() {
    pack();
    setVisible(true);
    timer.start();
  }
  
  public void update(Point2f[] points) {
    pintsToDraw = points;
    ic.repaint();
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
  
  private Point scale(Point2f p) {
    return new Point((int) (p.getX() * frameSize.width / tableScreenSize.width), 
        (int) (p.getY() * frameSize.height / tableScreenSize.height));
  }
}
