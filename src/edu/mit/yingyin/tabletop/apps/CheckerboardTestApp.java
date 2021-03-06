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

import edu.mit.yingyin.tabletop.controllers.DisplayImageFrame;
import edu.mit.yingyin.tabletop.controllers.ProcessPacketController;
import edu.mit.yingyin.tabletop.models.EnvConstant;
import edu.mit.yingyin.tabletop.models.HandTrackingEngine;
import edu.mit.yingyin.tabletop.models.ProcessPacket;
import edu.mit.yingyin.util.FileUtil;

/**
 * Tests the fingertip tracking with the checkerboard calibration image.
 * @author yingyin
 *
 */
public class CheckerboardTestApp {
  
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
      CheckerboardTestApp.class.getName());
  private static final String MAIN_DIR = ".";
  private static final String CALIB_DIR = FileUtil.join("data", "calibration");
  private static final String OPENNI_CONFIG_FILE = FileUtil.join(
      MAIN_DIR, "config", "config.xml");
  private static final String CALIB_FILE = FileUtil.join(MAIN_DIR, 
      CALIB_DIR, "calibration.txt");
  private static final String IMAGE_FILE_NAME = FileUtil.join(MAIN_DIR,
      CALIB_DIR, "checkerboard.png");
  
  public static void main(String[] args) {
    final CheckerboardTestApp controller = 
        new CheckerboardTestApp();
    
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
  private DisplayImageFrame heController;
  private ProcessPacketController packetController;
  
  public CheckerboardTestApp() {
    try {
      BufferedImage image = ImageIO.read(new File(IMAGE_FILE_NAME));
      heController = new DisplayImageFrame(image, 
          new Dimension(EnvConstant.TABLETOP_WIDTH, 
                        EnvConstant.TABLETOP_HEIGHT));
      engine = new HandTrackingEngine(OPENNI_CONFIG_FILE, CALIB_FILE);
      packetController = new ProcessPacketController(engine.depthWidth(), 
          engine.depthHeight(), null);
      engine.addHandEventListener(heController);
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
  }
  
  public void start() {
    while (!engine.isDone() && heController.isViewVisible()) {
      try {
        ProcessPacket packet = engine.step();
        packetController.show(packet);
      } catch (GeneralException e) {
        logger.severe(e.getMessage());
        engine.release();
        System.exit(-1);
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
