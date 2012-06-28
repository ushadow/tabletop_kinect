package edu.mit.yingyin.calib;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import edu.mit.yingyin.gui.ImageComponent;

/**
 * View for camera calibration.
 * 
 * Keys:
 * S: saves the points.
 * @author yingyin
 *
 */

public class CalibController implements KeyListener {
  
  /**
   * The image component.
   * @author yingyin
   *
   */
  private class CalibImageComponent extends ImageComponent 
      implements MouseListener {
    private static final long serialVersionUID = 1L;

    private static final int OVAL_WIDTH = 10;

    public CalibImageComponent(Dimension d) {
      super(d);
      addMouseListener(this);
    }
    
    public CalibImageComponent(BufferedImage bi) {
      super(bi);
      addMouseListener(this);
    }

    public void paint(Graphics g) {
      super.paint(g);
      
      Graphics2D g2d = (Graphics2D) g;

      List<Point> pts = model.getImagePoints();

      g2d.setColor(Color.RED);
      for (Point p : pts) {
        if (model.isScrnCoord())
          SwingUtilities.convertPointFromScreen(p, this);

        g2d.drawOval(p.x - OVAL_WIDTH / 2, p.y - OVAL_WIDTH / 2, OVAL_WIDTH, 
            OVAL_WIDTH);
      }
    }

    @Override
    public void mousePressed(MouseEvent e) {
      Point p = e.getPoint();

      // left click
      if ((e.getModifiersEx() | InputEvent.BUTTON1_DOWN_MASK) == 
          InputEvent.BUTTON1_DOWN_MASK) {
        // Convert a point from a component's coordinate system to screen
        // coordinates.
        if (model.isScrnCoord())
          SwingUtilities.convertPointToScreen(p, this);

        model.addImagePoint(p);
        repaint();
      }

      // right click
      if ((e.getModifiersEx() | InputEvent.BUTTON3_DOWN_MASK) == 
          InputEvent.BUTTON3_DOWN_MASK) {
        model.removeLastPoint();
        repaint();
      }
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {}
    
    @Override
    public void mouseClicked(MouseEvent e) {}
    
    @Override
    public void mouseEntered(MouseEvent arg0) {}
    
    @Override
    public void mouseExited(MouseEvent arg0) {}
  }
    
  private GeoCalibModel model;
  private CalibFrame frame;
  
  public CalibController(GeoCalibModel model) {
    this.model = model;
    frame = new CalibFrame(new CalibImageComponent(model.getImage()));
    frame.addKeyListener(this);
  }
  
  public void showUI() {
    frame.showUI();
  }
  
  public void addKeyListener(KeyListener kl) {
    frame.addKeyListener(kl);
  }
  
  @Override
  public void keyPressed(KeyEvent ke) {
    switch (ke.getKeyCode()) {
      case KeyEvent.VK_S:
        String fileName = (String)JOptionPane.showInputDialog(frame, 
            "File name:", "Save as", JOptionPane.PLAIN_MESSAGE, null, null, 
            model.getPointsFileName());
        model.saveImagePoints(fileName);
        break;
        
      case KeyEvent.VK_P:
        model.createPoints();
        frame.repaint();
        break;
        
      case KeyEvent.VK_C:
        model.clearPoints();
        frame.repaint();
        break;
        
      default:
        break;
    }
  }
  
  @Override
  public void keyReleased(KeyEvent arg0) {}
  
  @Override
  public void keyTyped(KeyEvent arg0) {}
}