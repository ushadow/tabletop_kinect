package edu.mit.yingyin.tabletop.apps;

import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.OpenNI.GeneralException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import edu.mit.yingyin.tabletop.controllers.ProcessPacketController;
import edu.mit.yingyin.tabletop.models.HandTrackingEngine;
import edu.mit.yingyin.tabletop.views.DisplayTargetFrame;
import edu.mit.yingyin.tabletop.views.Table3DFrame;
import edu.mit.yingyin.util.CommandLineOptions;
import edu.mit.yingyin.util.FileUtil;

public class DiecticGestureTest extends KeyAdapter {
  private static final Logger LOGGER = Logger.getLogger(
      DiecticGestureTest.class.getName());
  private static final String CONFIG_DIR = "config";
  private static final String DATA_DIR = "data";
  private static final String DEFAULT_OPENNI_CONFIG_FILE = FileUtil.join(
      CONFIG_DIR, "config.xml");
  private static final String DEFAULT_CALIB_FILE = FileUtil.join(DATA_DIR, 
      "calibration", "calibration.txt");
  
  public static void main(String[] args) {
    @SuppressWarnings("static-access")
    Option mainDirOpt = OptionBuilder.withArgName("main directory").withLongOpt(
        "dir").hasArg().create("d");
    CommandLineOptions.addOption(mainDirOpt);
    CommandLineOptions.parse(args);
    final String mainDir = CommandLineOptions.getOptionValue("d", ".");
    SwingUtilities.invokeLater(new Runnable() {
      
      @Override
      public void run() {
        new DiecticGestureTest(mainDir);
      }
    });
  }

  private HandTrackingEngine engine;
  private ProcessPacketController packetController;
  private Table3DFrame tableFrame;
  
  public DiecticGestureTest(String mainDir) {
    String openniConfigFile = FileUtil.join(mainDir, 
        DEFAULT_OPENNI_CONFIG_FILE);

    String calibrationFile = FileUtil.join(mainDir, DEFAULT_CALIB_FILE); 
    
    try {
      engine = new HandTrackingEngine(openniConfigFile, calibrationFile);
    } catch (GeneralException ge) {
      LOGGER.severe(ge.getMessage());
      System.exit(-1);
    }
    
    
    DisplayTargetFrame frame = new DisplayTargetFrame();
    frame.addKeyListener(this);
    frame.showUI();
    
    Thread thread = new Thread() {
      @Override
      public void run() {
        // TODO Auto-generated method stub
        while (isRunning()) {
          try {
            engine.step();
            
//            if (packetController != null)
//              packetController.show(engine.packet());
            
//        if (engine.interactionSurfaceInitialize() && tableFrame == null) {
//          tableFrame = new Table3DFrame(engine.interactionSurface());
//          Rectangle rect = packetController.getViewBounds();
//          tableFrame.setLocation(rect.width / 4, rect.height / 2);
//          tableFrame.addKeyListener(this);
//          engine.addListener(tableFrame);
//          tableFrame.showUI();
//        }
//
//        if (tableFrame != null)
//          tableFrame.redraw(engine.packet());
          } catch (Exception ge) {
            LOGGER.severe(ge.getMessage());
          }
        }
        
        engine.release();
        System.exit(0);
      }
    };
    thread.start();
  }
  
  public void keyPressed(KeyEvent ke) {
    switch (ke.getKeyCode()) {
      case KeyEvent.VK_ESCAPE:
        engine.release();
        System.exit(0);
        break;
      default:
        break;
    }
  }
  
  private boolean isRunning() {
    return !engine.isDone();
  }
}
