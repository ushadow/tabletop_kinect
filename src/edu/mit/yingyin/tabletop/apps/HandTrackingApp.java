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
import edu.mit.yingyin.tabletop.models.HandTracker.DiecticEvent;
import edu.mit.yingyin.tabletop.models.HandTracker.ManipulativeEvent;
import edu.mit.yingyin.tabletop.models.HandTrackingEngine;
import edu.mit.yingyin.tabletop.models.HandTrackingEngine.IHandEventListener;
import edu.mit.yingyin.tabletop.models.ProcessPacket;
import edu.mit.yingyin.tabletop.views.Table3DFrame;
import edu.mit.yingyin.util.CommandLineOptions;
import edu.mit.yingyin.util.FileUtil;
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
   * 
   * @author yingyin
   * 
   */
  private class HandEventListener implements IHandEventListener {
    /**
     * List of finger events detected in a frame.
     */
    private List<List<ManipulativeEvent>> fingerEventList = 
        new ArrayList<List<ManipulativeEvent>>();

    @Override
    public void fingerPressed(List<ManipulativeEvent> feList) {
      if (packetController != null) {
        for (ManipulativeEvent fe : feList)
          packetController.drawCircle((int) fe.posImage.x, (int) fe.posImage.y);
      }
      fingerEventList.add(feList);
    }

    /**
     * Outputs finger events.
     * 
     * @param pw <code>PrinterWriter</code> for output.
     */
    public void toOutput(PrintWriter pw) {
      pw.println("# frame-id x y z x y z ...");
      for (List<ManipulativeEvent> list : fingerEventList) {
        if (list.isEmpty())
          continue;
        pw.print(list.get(0).frameID + " ");
        for (ManipulativeEvent fe : list) {
          pw.print(String.format("%d %d %d ", (int) fe.posImage.x,
              (int) fe.posImage.y, (int) fe.posImage.z));
        }
        pw.println();
      }
    }

    @Override
    public void fingerPointed(DiecticEvent de) {}
  }

  private static Logger LOGGER = Logger.getLogger(
      HandTrackingApp.class.getName());

  private static final String CONFIG_DIR = "config";
  private static final String DATA_DIR = "data";
  private static final String FINGERTIP_DIR = FileUtil.join(DATA_DIR, 
      "fingertip");
  /**
   * Application properties.
   */
  private static final String APP_PROPS = FileUtil.join(CONFIG_DIR, 
      "fingertip_tracking.properties");
  private static final String DEFAULT_OPENNI_CONFIG_FILE = FileUtil.join(
      CONFIG_DIR, "config.xml");
  private static final String DEFAULT_CALIB_FILE = FileUtil.join(DATA_DIR, 
      "calibration", "calibration.txt");
  private static final String TIME_FORMAT = "yyyy-MM-dd_HH-mm-SS";
  private static final String OUTPUT_EXTENSION = ".log";

  @SuppressWarnings("static-access")
  public static void main(String[] args) {
    Option mainDirOpt = OptionBuilder.withArgName("main directory").withLongOpt(
        "dir").hasArg().withDescription(
        "The main directory for input and output. "
            + "The configuration file should be in <dir>/config/folder."
            + "The default dir is the current directory.").create("d");
    CommandLineOptions.addOption(mainDirOpt);
    CommandLineOptions.parse(args);
    String mainDir = CommandLineOptions.getOptionValue("d", ".");
    new HandTrackingApp(mainDir);
  }

  private HandTrackingEngine engine;
  private ProcessPacketController packetController;
  private HandEventListener handEventListener;
  private String mainDir;
  private boolean displayOn = true, saveFingertip = false;
  private boolean paused = false;
  private SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);

  @SuppressWarnings("unchecked")
  public HandTrackingApp(String mainDir) {
    LOGGER.info("java.library.path = "
        + System.getProperty("java.library.path"));

    this.mainDir = mainDir;
    Properties config = new Properties();
    FileInputStream in = null;
    try {
      in = new FileInputStream(FileUtil.join(mainDir, APP_PROPS));
      config.load(in);
      in.close();
    } catch (FileNotFoundException fnfe) {
      LOGGER.info("congfig file not found: " + mainDir + APP_PROPS);
      LOGGER.severe(fnfe.getMessage());
      System.exit(-1);
    } catch (IOException ioe) {
      LOGGER.severe(ioe.getMessage());
      System.exit(-1);
    }

    String openniConfigFile = FileUtil.join(mainDir,
        config.getProperty("openni-config", DEFAULT_OPENNI_CONFIG_FILE));

    String saveFingertipProperty = config.getProperty("save-fingertip-data",
        "false");
    if (saveFingertipProperty.equals("true"))
      saveFingertip = true;

    String labelFile = config.getProperty("fingertip-label-file", null);
    if (labelFile != null)
      labelFile = FileUtil.join(mainDir, FINGERTIP_DIR, labelFile);

    String displayOnProperty = config.getProperty("display-on", "true");

    String calibrationFile = FileUtil.join(mainDir,
        config.getProperty("calibration-file", DEFAULT_CALIB_FILE));

    if (displayOnProperty.equals("false"))
      displayOn = false;

    try {
      engine = new HandTrackingEngine(openniConfigFile, calibrationFile);
    } catch (GeneralException ge) {
      LOGGER.info("OpenNI config file = " + openniConfigFile);
      LOGGER.severe(ge.getMessage());
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

      } catch (IOException e) {
        System.err.println(e.getMessage());
        System.exit(-1);
      }
    }

    while (isRunning()) {
      try {
        if (isPaused())
          continue;
        ProcessPacket packet = engine.step();

        if (packetController != null)
          packetController.show(packet);

        packet.release();
      } catch (GeneralException ge) {
        LOGGER.severe(ge.getMessage());
        engine.release();
        System.exit(-1);
      }
    }

    print();
    engine.release();
    packetController.release();
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
      Date date = new Date();
      String fingertipFile = FileUtil.join(mainDir, FINGERTIP_DIR, 
          dateFormat.format(date) + OUTPUT_EXTENSION);
      pw = new PrintWriter(fingertipFile);
      handEventListener.toOutput(pw);
      LOGGER.info("Tracker controller output done.");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } finally {
      if (pw != null)
        pw.close();
    }
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

  private boolean isRunning() {
    return ((packetController != null && 
        packetController.isVisible() || 
        displayOn == false) && 
        !engine.isDone());
  }
  
  private boolean isPaused() {
    return packetController != null && paused;
  }
}
