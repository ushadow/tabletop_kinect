package edu.mit.yingyin.tabletop.apps;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.OpenNI.GeneralException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import edu.mit.yingyin.tabletop.controllers.ProcessPacketController;
import edu.mit.yingyin.tabletop.models.EnvConstants;
import edu.mit.yingyin.tabletop.models.HandTracker.FingerEvent;
import edu.mit.yingyin.tabletop.models.HandTrackingEngine;
import edu.mit.yingyin.tabletop.models.HandTrackingEngine.IHandEventListener;
import edu.mit.yingyin.util.CommandLineOptions;
import edu.mit.yingyin.util.ObjectIO;

/**
 * Application that tracks the fingertips in data from an OpenNI device. Saves
 * fingertip events to file in the end.
 *
 * @author yingyin
 *
 */
public class HandTrackingApp extends KeyAdapter {

  /**
   * Listens to hand events.
   * @author yingyin
   *
   */
  private class HandEventListener implements IHandEventListener {
    /**
     * List of finger events detected in a frame.
     */
    private List<List<FingerEvent>> fingerEventList = 
        new ArrayList<List<FingerEvent>>();
    
    @Override
    public void fingerPressed(List<FingerEvent> feList) {
      if (packetController != null) {
        for (FingerEvent fe : feList)
          packetController.drawCircle((int)fe.posImage.x, (int)fe.posImage.y);
      }
      fingerEventList.add(feList);
    }
    
    public void toOutput(PrintWriter pw) {
      pw.println("# frame-id x y z x y z ...");
      for (List<FingerEvent> list : fingerEventList) {
        for (int i = 0; i < list.size(); i++) {
          if (i == 0) {
            pw.print(String.format("%d %d %d %d ", list.get(i).frameID, 
                (int)list.get(i).posImage.x, (int)list.get(i).posImage.y, 
                (int)list.get(i).posImage.z));
          } else {
            pw.print(String.format("%d %d %d ", (int)list.get(i).posImage.x, 
                (int)list.get(i).posImage.y, (int)list.get(i).posImage.z));
          }
        }
        pw.println();
      }
    }
  }
  
  private static Logger logger = Logger.getLogger(
      HandTrackingApp.class.getName());
  
  private static final String CONFIG_FILE = 
      "/config/fingertip_tracking.properties";
  private static final String DATA_DIR = "/data/";
  private static final String FINGERTIP_DIR = DATA_DIR + "fingertip/";
  private static final String TIME_FORMAT = "yyyy-MM-dd_HH-mm-SS";
  
  @SuppressWarnings("static-access")
  public static void main(String[] args) {
    Option mainDirOpt = OptionBuilder.withArgName("main directory").
        withLongOpt("dir").
        hasArg().withDescription("The main directory for input and output. " +
        "The configuration file should be in <dir>/config/folder." + 
        "The default dir is the current directory.").create("d");
    CommandLineOptions.addOption(mainDirOpt);
    CommandLineOptions.parse(args);
    String mainDir = CommandLineOptions.getOptionValue("d", "./");
    new HandTrackingApp(mainDir);
  }
  
  private HandTrackingEngine engine;
  private ProcessPacketController packetController;
  private HandEventListener handEventListener;
  private String mainDir;
  private boolean displayOn = true, saveFingertip = false;
  private boolean paused = false;
  private SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
  private Table3DFrame tableFrame;
  
  @SuppressWarnings("unchecked")
  public HandTrackingApp(String mainDir)  {
    logger.info("java.library.path = " + 
        System.getProperty("java.library.path"));
    
    this.mainDir = mainDir;
    Properties config = new Properties();
    FileInputStream in = null;
    try {
      in = new FileInputStream(mainDir + CONFIG_FILE);
      config.load(in);
      in.close();
    } catch (FileNotFoundException fnfe) {
      logger.info("congfig file not found: " + mainDir + CONFIG_FILE);
      logger.severe(fnfe.getMessage());
      System.exit(-1);
    } catch (IOException ioe) {
      logger.severe(ioe.getMessage());
      System.exit(-1);
    }
    
    String openniConfigFile = mainDir + config.getProperty("openni-config", 
        "config/config.xml");
    
    String saveFingertipProperty = config.getProperty("save-fingertip-data", 
        "false");
    if (saveFingertipProperty.equals("true"))
      saveFingertip = true;
    
    String labelFile = config.getProperty("fingertip-label-file", null);
    if (labelFile != null)
      labelFile = mainDir + FINGERTIP_DIR + labelFile;
    
    String displayOnProperty = config.getProperty("display-on", "true");
    
    int maxDepth = EnvConstants.DEFAULT_MAX_DEPTH;
    try {
      maxDepth = Integer.parseInt(config.getProperty("max-depth", "1600"));
    } catch(NumberFormatException efe) {
      System.err.println(efe.getMessage());
      System.err.println(String.format("maxDepth = %d", maxDepth));
    }
    
    String derivativeSaveDir = mainDir + config.getProperty("derivative-dir", 
        "data/derivative/");
    String calibrationFile = mainDir + config.getProperty("calibration-file",
        "data/calibration/calibration.txt");
    
    if (displayOnProperty.equals("false"))
      displayOn = false;
    
    try {
      engine = HandTrackingEngine.initInstance(openniConfigFile, calibrationFile, 
          maxDepth);
    } catch (GeneralException ge) {
      logger.info("OpenNI config file = " + openniConfigFile);
      logger.severe(ge.getMessage());
      System.exit(-1);
    }
    handEventListener = new HandEventListener();
    engine.addListener(handEventListener);
    
    if (displayOn) {
      try {
        HashMap<Integer, List<Point>> labels = null;
        if (labelFile != null)
          labels = (HashMap<Integer, List<Point>>) ObjectIO.readObject(
              labelFile);

        packetController = new ProcessPacketController(engine.depthWidth(), 
            engine.depthHeight(), labels);
        
        packetController.addKeyListener(this);
        packetController.derivativeSaveDir = derivativeSaveDir;
        packetController.showUI();
        
      } catch (IOException e) {
        System.err.println(e.getMessage());
        System.exit(-1);
      }
    }
    
    while (isRunning()) {
      if (isPaused())
        continue;
      engine.step();
      
      if (packetController != null) {
        try {
          packetController.show(engine.packet());
        } catch (GeneralException ge) {
          logger.severe(ge.getMessage());
        }
      }
      
      if (engine.isTableInitialized() && tableFrame == null) {
        tableFrame = new Table3DFrame(engine.table());
        Rectangle rect = packetController.getViewBounds();
        tableFrame.setLocation(rect.width, 0);
        tableFrame.showUI();
      }
    }

    print();
    engine.release();
    System.exit(0);
  }
  
  /**
   * Prints finger events for evaluation.
   */
  public void print() {
    if (!saveFingertip)
      return;
    
    PrintWriter pw = null;
    try {
      Date date =  new Date();
      String fingertipFile = mainDir + FINGERTIP_DIR + dateFormat.format(date) + 
          ".txt";
      pw = new PrintWriter(fingertipFile);
      handEventListener.toOutput(pw);
      logger.info("Tracker controller output done.");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } finally {
      if (pw != null)
        pw.close();
    }
  }
  
  public boolean isRunning() {
    return ((packetController != null && packetController.isVisible() || 
        displayOn == false) && !engine.isDone());
  }
  
  public boolean isPaused() {
    return packetController != null && paused;
  }
    
  public void keyPressed(KeyEvent ke) {
    switch (ke.getKeyCode()) {
      case KeyEvent.VK_N:
        paused = true;
        engine.step();
        break;
      case KeyEvent.VK_P:
        paused = !paused;
        break;
      case KeyEvent.VK_ESCAPE:
      case KeyEvent.VK_Q:
        packetController.hide();
        break;
      default:
        break;
    }
  }
}