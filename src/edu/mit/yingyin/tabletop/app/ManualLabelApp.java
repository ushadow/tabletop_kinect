package edu.mit.yingyin.tabletop.app;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;

import edu.mit.yingyin.gui.ImageComponent;
import edu.mit.yingyin.gui.ImageView;
import edu.mit.yingyin.tabletop.ManualLabelModel;

public class ManualLabelApp extends KeyAdapter implements MouseListener {
  private class LabelView extends ImageComponent {

    private static final long serialVersionUID = -516675143416854662L;
    private static final int OVAL_WIDTH = 10;

    public LabelView(Dimension d) {
      super(d);
    }

    @Override
    public void paint(Graphics g) {
      super.paint(g);

      List<Point> points = model.getPoints(model.getFrameID());
      Graphics2D g2d = (Graphics2D) g;
      g2d.setColor(Color.red);
      for (Point p : points)
        g2d.drawOval(p.x - OVAL_WIDTH / 2, p.y - OVAL_WIDTH / 2, OVAL_WIDTH,
            OVAL_WIDTH);
    }
  }

  public static void main(String[] args) {
    new ManualLabelApp();
  }

  private ImageView viewer;
  private ManualLabelModel model;
  private String openniConfigFile = "config/config.xml";
  private String saveFilename = "data/groud_truth.label";

  public ManualLabelApp() {
    model = new ManualLabelModel(openniConfigFile);
    LabelView labelView = new LabelView(new Dimension(model.getImageWidth(),
        model.getImageHeight()));
    viewer = new ImageView("Label", labelView);
    viewer.addKeyListener(this);
    labelView.addMouseListener(this);
    viewer.addWindowListener(new WindowAdapter() {

      public void windowClosing(WindowEvent arg0) {
        exit();
      }
    });
    showNextImage();
  }

  private void exit() {
    model.cleanUp();
    System.exit(0);
  }

  private void showNextImage() {
    viewer.show(model.nextImage());
    viewer.setTitle("Frame = " + model.getFrameID());
  }

  public void keyPressed(KeyEvent ke) {
    switch (ke.getKeyCode()) {
      case KeyEvent.VK_ESCAPE:
      case KeyEvent.VK_Q:
        exit();
        break;
      case KeyEvent.VK_N:
        showNextImage();
        break;
      case KeyEvent.VK_UP:
        model.increaseRate();
        break;
      case KeyEvent.VK_DOWN:
        model.decreaseRate();
        break;
      case KeyEvent.VK_S:
        try {
          model.save(saveFilename);
          System.out.println("Saved labels.");
        } catch (IOException e) {
          System.err.println(e.getMessage());
        }
        break;
      default:
        break;
    }
  }

  @Override
  public void mouseClicked(MouseEvent arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void mouseEntered(MouseEvent arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void mouseExited(MouseEvent arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void mousePressed(MouseEvent me) {
    Point p = me.getPoint();

    // left click
    if ((me.getModifiersEx() | InputEvent.BUTTON1_DOWN_MASK) == 
        InputEvent.BUTTON1_DOWN_MASK) {
      model.addPoint(p);
      viewer.repaint();
    }

    // right click
    if ((me.getModifiersEx() | InputEvent.BUTTON3_DOWN_MASK) == 
        InputEvent.BUTTON3_DOWN_MASK) {
      model.removeLastPoint();
      viewer.repaint();
    }
  }

  @Override
  public void mouseReleased(MouseEvent arg0) {
    // TODO Auto-generated method stub

  }

}
