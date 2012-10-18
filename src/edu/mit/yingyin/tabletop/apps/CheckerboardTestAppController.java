package edu.mit.yingyin.tabletop.apps;

import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import org.OpenNI.GeneralException;

import edu.mit.yingyin.tabletop.controllers.HandEventsController;
import edu.mit.yingyin.tabletop.controllers.ProcessPacketController;
import edu.mit.yingyin.tabletop.models.EnvConstants;
import edu.mit.yingyin.tabletop.models.HandTrackingEngine;

/**
 * Tests the fingertip tracking with the checkerboard calibration image.
 * @author yingyin
 *
 */
public class CheckerboardTestAppController {
  
  private class KeyController extends KeyAdapter {
    @Override
    public void keyPressed(KeyEvent ke) {
      switch(ke.getKeyCode()) {
        case KeyEvent.VK_Q:
        case KeyEvent.VK_ESCAPE:
          exit();
          break;
        default: break;
      }
    }
  }
  
  private static Logger logger = Logger.getLogger(
      CheckerboardTestAppController.class.getName());
  private static final String MAIN_DIR = "./";
  private static final String OPENNI_CONFIG_FILE = 
      MAIN_DIR + "config/config.xml";
  private static final String CALIB_FILE = MAIN_DIR + 
      "data/calibration/calibration.txt";
  private static final String IMAGE_FILE_NAME = MAIN_DIR + 
    "data/calibration/checkerboard.png";
  
  private static final int TABLETOP_WIDTH = 1920 * 2;
  private static final int TABLETOP_HEIGHT = 1080 * 2;
  
  public static void main(String[] args) {
    final CheckerboardTestAppController controller = 
        new CheckerboardTestAppController();
    
    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        @Override
        public void run() {
          controller.showUI();
        }
      });
    } catch (InterruptedException e) {
      System.err.println(e.getMessage());
      System.exit(-1);
    } catch (InvocationTargetException e) {
      System.err.println(e.getMessage());
      System.exit(-1);
    }
    controller.start();
  }
  
  private HandTrackingEngine engine;
  private HandEventsController heController;
  private ProcessPacketController packetController;
  
  public CheckerboardTestAppController() {
    try {
      BufferedImage image = ImageIO.read(new File(IMAGE_FILE_NAME));
      heController = new HandEventsController(image, 
          new Dimension(TABLETOP_WIDTH, TABLETOP_HEIGHT));
      engine = new HandTrackingEngine(OPENNI_CONFIG_FILE, CALIB_FILE, 
          EnvConstants.DEFAULT_MAX_DEPTH);
      packetController = new ProcessPacketController(engine.depthWidth(), 
          engine.depthHeight(), null);
      engine.addListener(heController);
      packetController.addKeyListener(new KeyController());
    } catch (GeneralException ge) {
      logger.severe(ge.getMessage());
      System.exit(-1);
    } catch (IOException ioe) {
      logger.severe(ioe.getMessage());
      System.exit(-1);
    }
  }
  
  public void showUI() {
    heController.showUI();
    packetController.showDepthImage(false);
    packetController.showDiagnosticImage(false);
  }
  
  public void start() {
    while (!engine.isDone() && heController.isViewVisible()) {
      engine.step();
      try {
        packetController.show(engine.packet());
      } catch (GeneralException e) {
        logger.severe(e.getMessage());
      }
    }
    exit();
  }
  
  private void exit() {
    if (engine != null)
      engine.release();
    System.exit(0);
  }
}
