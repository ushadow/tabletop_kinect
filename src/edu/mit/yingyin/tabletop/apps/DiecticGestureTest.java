package edu.mit.yingyin.tabletop.apps;

import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.OpenNI.GeneralException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import edu.mit.yingyin.tabletop.controllers.ProcessPacketController;
import edu.mit.yingyin.tabletop.models.HandTrackingEngine;
import edu.mit.yingyin.tabletop.models.ProcessPacket;
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
    
    packetController = new ProcessPacketController(engine.depthWidth(),
        engine.depthHeight(), null);

    packetController.addKeyListener(this);
    
    DisplayTargetFrame frame = new DisplayTargetFrame();
    frame.addKeyListener(this);
    frame.showUI();
    
    final BlockingQueue<ProcessPacket> queue = 
        new LinkedBlockingQueue<ProcessPacket>();
    
    Thread producer = new Thread() {
      @Override
      public void run() {
        while (isRunning()) {
          try {
            ProcessPacket packet = engine.step();
            queue.offer(packet);
          } catch (Exception ge) {
            LOGGER.severe(ge.getMessage());
          }
        }
        
        engine.release();
        System.exit(0);
      }
    };
    producer.start();
    
    Thread consumer = new Thread() {
      @Override
      public void run() {
        while (isRunning()) {
          ProcessPacket packet;
          try {
            packet = queue.take();
            packetController.show(packet);
  
            packet.release();
          } catch (InterruptedException e) {
            e.printStackTrace();
          } catch (GeneralException ge) {
            ge.printStackTrace();
          }
        }
      }
    };
    consumer.start();
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
