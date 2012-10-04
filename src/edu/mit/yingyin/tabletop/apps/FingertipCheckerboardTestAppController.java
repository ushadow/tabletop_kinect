package edu.mit.yingyin.tabletop.apps;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.OpenNI.GeneralException;

import edu.mit.yingyin.tabletop.controllers.HandEventsController;
import edu.mit.yingyin.tabletop.controllers.ProcessPacketController;
import edu.mit.yingyin.tabletop.models.HandTrackingEngine;

/**
 * Tests the fingertip tracking with the checkerboard calibration image.
 * @author yingyin
 *
 */
public class FingertipCheckerboardTestAppController {
  
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
      FingertipCheckerboardTestAppController.class.getName());
  private static final String MAIN_DIR = "./";
  private static final String OPENNI_CONFIG_FILE = 
      MAIN_DIR + "config/config.xml";
  private static final String CALIB_FILE = MAIN_DIR + 
      "data/calibration/calibration.txt";
  private static final String IMAGE_FILE_NAME = MAIN_DIR + 
    "data/calibration/checkerboard.png";
  
  private static final int MAX_DEPTH = 1600;
  
  public static void main(String[] args) {
    new FingertipCheckerboardTestAppController();
  }
  
  private HandTrackingEngine engine;
  
  public FingertipCheckerboardTestAppController() {
    try {
      BufferedImage image = ImageIO.read(new File(IMAGE_FILE_NAME));
      HandEventsController heController = new HandEventsController(image);
      engine = new HandTrackingEngine(OPENNI_CONFIG_FILE, CALIB_FILE, 
          MAX_DEPTH);
      ProcessPacketController packetController = new ProcessPacketController(
          engine.depthWidth(), engine.depthHeight(), null);
      packetController.showDepthImage(false);
      packetController.showDiagnosticImage(false);
      
      engine.addListener(heController);
      packetController.addKeyListener(new KeyController());
      
      while (!engine.isDone() && heController.isViewVisible()) {
        engine.step();
        try {
          packetController.show(engine.packet());
        } catch (GeneralException e) {
          logger.severe(e.getMessage());
        }
      }
      exit();
    } catch (GeneralException ge) {
      logger.severe(ge.getMessage());
      System.exit(-1);
    } catch (IOException ioe) {
      logger.severe(ioe.getMessage());
      System.exit(-1);
    }
  }
  
  private void exit() {
    if (engine != null)
      engine.release();
    System.exit(0);
  }
}
