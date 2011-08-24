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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.OpenNI.StatusException;

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

      List<Point> points = model.getPoints(model.frameID());
      Graphics2D g2d = (Graphics2D) g;
      g2d.setColor(Color.red);
      for (Point p : points)
        g2d.drawOval(p.x - OVAL_WIDTH / 2, p.y - OVAL_WIDTH / 2, OVAL_WIDTH,
            OVAL_WIDTH);
    }
  }

  private static final String CONFIG_FILE = "./config/manual_label.config"; 
  public static void main(String[] args) {
    new ManualLabelApp();
  }

  private ImageView viewer;
  private ManualLabelModel model;
  private String openniConfigFile;
  private String saveFilename;

  public ManualLabelApp() {
    Properties config = new Properties();
    FileInputStream in = null;
    try {
      in = new FileInputStream(CONFIG_FILE);
      config.load(in);
      in.close();
    } catch (FileNotFoundException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } 
    
    openniConfigFile = config.getProperty("openni-config", "config/config.xml");
    saveFilename = config.getProperty("save-file", "data/groud_truth.label");
    model = new ManualLabelModel(openniConfigFile);
    LabelView labelView = new LabelView(new Dimension(model.imageWidth(),
        model.imageHeight()));
    viewer = new ImageView("Label", labelView);
    viewer.addKeyListener(this);
    labelView.addMouseListener(this);
    viewer.addWindowListener(new WindowAdapter() {

      public void windowClosing(WindowEvent arg0) {
        exit();
      }
    });
    showNextImage(true);
  }

  public void keyPressed(KeyEvent ke) {
    switch (ke.getKeyCode()) {
      case KeyEvent.VK_ESCAPE:
      case KeyEvent.VK_Q:
        exit();
        break;
      case KeyEvent.VK_N:
        showNextImage(true);
        break;
      case KeyEvent.VK_P:
        showNextImage(false);
        break;
      case KeyEvent.VK_UP:
        model.increaseRate();
        updateTitle();
        break;
      case KeyEvent.VK_DOWN:
        model.decreaseRate();
        updateTitle();
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
  public void mouseClicked(MouseEvent arg0) {}

  @Override
  public void mouseEntered(MouseEvent arg0) {}

  @Override
  public void mouseExited(MouseEvent arg0) {}

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
  public void mouseReleased(MouseEvent arg0) {}

  private void exit() {
    model.release();
    System.exit(0);
  }

  private void showNextImage(boolean forward) {
    try {
      viewer.show(model.nextImage(forward));
      updateTitle();
    } catch (StatusException e) {
      System.err.println(e.getMessage());
      System.exit(-1);
    }
  }
  
  private void updateTitle() {
    viewer.setTitle("Frame = " + model.frameID() + " skip = " + 
        model.skipRate());
  }
}
