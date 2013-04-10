package edu.mit.yingyin.tabletop.apps;

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
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.OpenNI.GeneralException;
import org.OpenNI.StatusException;

import edu.mit.yingyin.gui.ImageComponent;
import edu.mit.yingyin.gui.ImageFrame;
import edu.mit.yingyin.tabletop.models.EnvConstant;
import edu.mit.yingyin.tabletop.models.ManualGestureLabel;
import edu.mit.yingyin.tabletop.models.ManualPointLabel;
import edu.mit.yingyin.tabletop.models.OpenNIDevice;
import edu.mit.yingyin.util.FileUtil;

/**
 * Application that allows manual labeling of points on an OpenNI recorded file.
 * 
 * Keyboard interactions: s - save labels to file.
 * 
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

      List<Point> points = pointLabel.getPoints(pointLabel.depthFrameID());
      Graphics2D g2d = (Graphics2D) g;
      g2d.setColor(Color.red);
      for (Point p : points)
        g2d.drawOval(p.x - OVAL_WIDTH / 2, p.y - OVAL_WIDTH / 2, OVAL_WIDTH,
            OVAL_WIDTH);
    }
  }

  private static final Logger LOGGER =
      Logger.getLogger(ManualLabelApp.class.getName());
  private static final String DIR = "/afs/csail/u/y/yingyin/research/kinect";
  private static final String CONFIG_FILE = FileUtil.join(DIR,
      "config/manual-label.config");
  private static final int MAX_DEPTH = 1600;

  public static void main(String[] args) {
    new ManualLabelApp();
  }

  private ImageFrame depthViewer, rgbViewer;
  private ManualPointLabel pointLabel;
  private ManualGestureLabel gestureLabel;
  private String openniConfigFile;
  private String pointLabelFile;
  private String gestureLabelFile;

  public ManualLabelApp() {
    Properties config = new Properties();
    FileInputStream in = null;
    try {
      in = new FileInputStream(CONFIG_FILE);
      config.load(in);
      in.close();
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Get configuration parameters.
    openniConfigFile = config.getProperty("openni-config", null);
    if (openniConfigFile != null) {
      openniConfigFile = FileUtil.join(DIR, EnvConstant.ONI_DIR, 
                                       openniConfigFile);
    } else {
      LOGGER.severe("No recording file is specified.");
      System.exit(-1);
    }
    
    String basename = null;
    if (OpenNIDevice.isRecordingFile(openniConfigFile)) {
      basename = FileUtil.basename(openniConfigFile, 
                                   OpenNIDevice.RECORDING_SUFFIX);
    }
    
    String labelPoint = config.getProperty("label-point", "false");
    if (labelPoint.equalsIgnoreCase("true")) {
      pointLabelFile = FileUtil.join(DIR, EnvConstant.FINGERTIP_DIR, 
                                     basename + EnvConstant.POINT_LABEL_SUFFIX);
    }
    
    String labelGesture = config.getProperty("label-gesture", "false");
    if (labelGesture.equalsIgnoreCase("true")) {
      gestureLabelFile = FileUtil.join(DIR, EnvConstant.GESUTRE_DIR, 
          basename + EnvConstant.GESTURE_LABEL_SUFFIX);
    }
    try {
      pointLabel =
          new ManualPointLabel(openniConfigFile, pointLabelFile, MAX_DEPTH);
      gestureLabel = new ManualGestureLabel(gestureLabelFile);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.exit(-1);
    } catch (GeneralException ge) {
      ge.printStackTrace();
      System.exit(-1);
    }
    LabelView depthView =
        new LabelView(new Dimension(pointLabel.depthWidth(),
            pointLabel.depthHeight()));
    ImageComponent rgbView =
        new LabelView(new Dimension(pointLabel.rgbWidth(),
            pointLabel.rgbHeight()));
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
    depthViewer.showUI();
    rgbViewer.showUI();
    seek(true);
  }

  public void keyPressed(KeyEvent ke) {
    int keyCode = ke.getKeyCode();
    switch (keyCode) {
      case KeyEvent.VK_ESCAPE:
      case KeyEvent.VK_Q:
        exit();
        break;
      case KeyEvent.VK_N:
        LOGGER.fine("N pressed.");
        seek(true);
        break;
      case KeyEvent.VK_P:
        LOGGER.fine("P pressed.");
        seek(false);
        break;
      case KeyEvent.VK_UP:
        pointLabel.increaseRate();
        updateTitle();
        break;
      case KeyEvent.VK_DOWN:
        pointLabel.decreaseRate();
        updateTitle();
        break;
      case KeyEvent.VK_G:
        showGestureLabelInputDialog();
        break;
      case KeyEvent.VK_S:
        try {
          if (pointLabelFile != null) {
            pointLabel.save(pointLabelFile);
            LOGGER.info("Saved point labels.");
          }
          if (gestureLabel != null) {
            gestureLabel.save(gestureLabelFile);
            LOGGER.info("Saved gesture labels.");
          }
        } catch (IOException e) {
          LOGGER.severe(e.getMessage());
        }
        break;
      default:
        break;
    }
  }

  @Override
  public void mouseClicked(MouseEvent arg0) {
  }

  @Override
  public void mouseEntered(MouseEvent arg0) {
  }

  @Override
  public void mouseExited(MouseEvent arg0) {
  }

  @Override
  public void mousePressed(MouseEvent me) {
    Point p = me.getPoint();
    depthViewer.setStatus("Clicked at (" + p.x + ", " + p.y + ")");
    // left click
    if ((me.getModifiersEx() | InputEvent.BUTTON1_DOWN_MASK) == 
        InputEvent.BUTTON1_DOWN_MASK) {
      pointLabel.addPoint(p);
      depthViewer.repaint();
    }

    // right click
    if ((me.getModifiersEx() | InputEvent.BUTTON3_DOWN_MASK) == 
        InputEvent.BUTTON3_DOWN_MASK) {
      pointLabel.removeLastPoint();
      depthViewer.repaint();
    }
  }

  @Override
  public void mouseReleased(MouseEvent arg0) {
  }

  private void exit() {
    pointLabel.release();
    System.exit(0);
  }

  /**
   * Seeks forward or backward and updates the image.
   * 
   * @param forward if true seeks forward, otherwise seeks backward.
   */
  private void seek(boolean forward) {
    try {
      pointLabel.update(forward);
      updateTitle();
      depthViewer.updateImage(pointLabel.depthImage());
      rgbViewer.updateImage(pointLabel.rgbImage());
      updateStatus();
    } catch (StatusException e) {
      System.err.println(e.getMessage());
      System.exit(-1);
    } catch (GeneralException ge) {
      System.err.println(ge.getMessage());
      System.exit(-1);
    }
  }

  private void updateTitle() {
    depthViewer.setTitle("Frame = " + pointLabel.depthFrameID() + " skip = " +
        pointLabel.skipRate());
  }

  private void showGestureLabelInputDialog() {
    String label =
        (String) JOptionPane.showInputDialog(depthViewer,
            "Enter gesture label:<G>,<F> Use 0 when there is no gesture", 
            "Gesture Label Input Dialog",
            JOptionPane.PLAIN_MESSAGE);
    if (label != null && !label.isEmpty())
      gestureLabel.add(pointLabel.depthFrameID(), label);
    updateStatus();
  }
  
  private void updateStatus() {
    depthViewer.setStatus(String.format("G,F: %s", 
        gestureLabel.predecessor(pointLabel.depthFrameID())));
  }
}
