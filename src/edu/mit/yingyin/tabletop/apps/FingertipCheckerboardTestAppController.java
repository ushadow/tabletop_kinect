package edu.mit.yingyin.tabletop.apps;

import java.io.IOException;
import java.util.logging.Logger;

import org.OpenNI.GeneralException;

import edu.mit.yingyin.tabletop.controllers.HandEventsController;
import edu.mit.yingyin.tabletop.controllers.ProcessPacketController;
import edu.mit.yingyin.tabletop.models.HandTrackingEngine;

/**
 * Tests the fingertip tracking with the checkerboard calibraiton image.
 * @author yingyin
 *
 */
public class FingertipCheckerboardTestAppController {
  
  private static Logger logger = Logger.getLogger(
      FingertipCheckerboardTestAppController.class.getName());
  
  private static final String MAIN_DIR = 
      "/afs/csail/u/y/yingyin/research/kinect/";
  private static final String OPENNI_CONFIG_FILE = 
      MAIN_DIR + "config/config.xml";
  private static final String CALIB_FILE = MAIN_DIR + "data/calibration.txt";
  
  public static void main(String[] args) {
    new FingertipCheckerboardTestAppController();
  }
  
  private HandTrackingEngine engine;
  
  public FingertipCheckerboardTestAppController() {
    try {
      HandEventsController heController = new HandEventsController();
      engine = new HandTrackingEngine(OPENNI_CONFIG_FILE, CALIB_FILE);
      ProcessPacketController packetController = new ProcessPacketController(
          engine.depthWidth(), engine.depthHeight(), null);
      packetController.showDepthImage(false);
      packetController.showDiagnosticImage(false);
      
      engine.addListener(heController);
      while (!engine.isDone() && heController.isViewVisible()) {
        engine.step();
        try {
          packetController.show(engine.packet());
        } catch (GeneralException e) {
          logger.severe(e.getMessage());
        }
      }
      engine.release();
      System.exit(0);
    } catch (GeneralException ge) {
      logger.severe(ge.getMessage());
      System.exit(-1);
    } catch (IOException ioe) {
      logger.severe(ioe.getMessage());
      System.exit(-1);
    }
    
  }
}
