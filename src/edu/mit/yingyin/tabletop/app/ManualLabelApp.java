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

import org.OpenNI.GeneralException;
import org.OpenNI.StatusException;

import edu.mit.yingyin.gui.ImageComponent;
import edu.mit.yingyin.gui.ImageFrame;
import edu.mit.yingyin.tabletop.ManualLabelModel;

/**
 * Application that allows manual labelling of points on an OpenNI recorded 
 * file.
 * @author yingyin
 *
 */
public class ManualLabelApp extends KeyAdapter implements MouseListener {
  /** View for displaying point labels on an image component. */
  private class LabelView extends ImageComponent {

    private static final long serialVersionUID = -516675143416854662L;
    private static final int OVAL_WIDTH = 10;

    public LabelView(Dimension d) {
      super(d);
    }

    @Override
    public void paint(Graphics g) {
      super.paint(g);

      List<Point> points = model.getPoints(model.depthFrameID());
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

  private ImageFrame depthViewer, rgbViewer;
  private ManualLabelModel model;
  private String openniConfigFile;
  private String saveFilename;
  private String replayFilename;

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
    
    // Get configuration parameters.
    openniConfigFile = config.getProperty("openni-config", "config/config.xml");
    saveFilename = config.getProperty("save-file", "data/groud_truth.label");
    replayFilename = config.getProperty("replay-file", null);
    
    
    try {
      model = new ManualLabelModel(openniConfigFile, replayFilename);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.exit(-1);
    } catch (GeneralException ge) {
      ge.printStackTrace();
      System.exit(-1);
    }
    LabelView depthView = new LabelView(new Dimension(model.depthWidth(),
        model.depthHeight()));
    ImageComponent rgbView = new LabelView(new Dimension(model.rgbWidth(),
        model.rgbHeight()));
    rgbViewer = new ImageFrame("RGB", rgbView);
    depthViewer = new ImageFrame("Depth", depthView);
    depthViewer.addKeyListener(this);
    rgbViewer.addKeyListener(this);
    depthView.addMouseListener(this);
    WindowAdapter wa = new WindowAdapter() {
      public void windowClosing(WindowEvent arg0) {
        exit();
      }
    };
    depthViewer.addWindowListener(wa);
    rgbViewer.addWindowListener(wa);
    Point location = depthViewer.getLocation();
    rgbViewer.setLocation(location.x + depthViewer.getWidth(), location.y);
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
    depthViewer.setStatus("Clicked at (" + p.x + ", " + p.y + ")");
    // left click
    if ((me.getModifiersEx() | InputEvent.BUTTON1_DOWN_MASK) == 
        InputEvent.BUTTON1_DOWN_MASK) {
      model.addPoint(p);
      depthViewer.repaint();
    }

    // right click
    if ((me.getModifiersEx() | InputEvent.BUTTON3_DOWN_MASK) == 
        InputEvent.BUTTON3_DOWN_MASK) {
      model.removeLastPoint();
      depthViewer.repaint();
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
      model.update(forward);
      updateTitle();
      depthViewer.show(model.depthImage());
      rgbViewer.show(model.rgbImage());
    } catch (StatusException e) {
      System.err.println(e.getMessage());
      System.exit(-1);
    } catch (GeneralException ge) {
      System.err.println(ge.getMessage());
      System.exit(-1);
    }
  }
  
  private void updateTitle() {
    depthViewer.setTitle("Frame = " + model.depthFrameID() + " skip = " + 
        model.skipRate());
  }
}
