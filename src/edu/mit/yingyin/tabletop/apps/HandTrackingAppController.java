package edu.mit.yingyin.tabletop.apps;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.OpenNI.GeneralException;

import edu.mit.yingyin.tabletop.controllers.ProcessPacketController;
import edu.mit.yingyin.tabletop.models.HandTracker.FingerEvent;
import edu.mit.yingyin.tabletop.models.HandTracker.IHandEventListener;
import edu.mit.yingyin.tabletop.models.HandTrackingEngine;
import edu.mit.yingyin.tabletop.models.ProcessPacket;
import edu.mit.yingyin.tabletop.models.Recorder;

/**
 * Application that tracks the fingertips in data from an OpenNI device. Saves
 * fingertip events to file in the end.
 * 
 * @author yingyin
 *
 */
public class HandTrackingAppController extends KeyAdapter {
  
  private static Logger logger = Logger.getLogger(
      HandTrackingAppController.class.getName());
  
  private static String MAIN_DIR = "/afs/csail/u/y/yingyin/research/kinect/";
  private static String CONFIG_FILE = MAIN_DIR + 
                                      "config/fingertip_tracking.config";
  
  public static void main(String[] args) {
    new HandTrackingAppController();
  }
  
  private HandTrackingEngine engine;
  private ProcessPacketController packetController;
  private Recorder recorder;
  private String fingertipFile;
  private HandEventListener handEventListener;
  private boolean recording = false;
  private boolean displayOn = true;
  private boolean paused = false;
  private int rowToRecord = 0;

  public HandTrackingAppController()  {
    System.out.println("java.library.path = "
        + System.getProperty("java.library.path"));
    
    Properties config = new Properties();
    FileInputStream in = null;
    try {
      in = new FileInputStream(CONFIG_FILE);
      config.load(in);
      in.close();
    } catch (FileNotFoundException fnfe) {
      logger.severe(fnfe.getMessage());
      System.exit(-1);
    } catch (IOException ioe) {
      logger.severe(ioe.getMessage());
      System.exit(-1);
    }
    
    String openniConfigFile = MAIN_DIR + config.getProperty("openni-config", 
        "config/config.xml");
    String depthFilePrefix = MAIN_DIR + config.getProperty("depth-file-prefix", 
        "data/depth_raw/depth_row");
    
    fingertipFile = MAIN_DIR + config.getProperty("fingertip-file", 
        String.format("data/fingertip/%s.txt", "detected"));
    
    String labelFile = config.getProperty("label-file", null);
    if (labelFile != null)
      labelFile = MAIN_DIR + labelFile;
    
    String displayOnProperty = config.getProperty("display-on", "true");
    String derivativeSaveDir = MAIN_DIR + config.getProperty("derivative-dir", 
        "data/derivative/");
    String calibrationFile = MAIN_DIR + config.getProperty("calibration-file",
        "data/calibration.txt");
    
    if (displayOnProperty.equals("false"))
      displayOn = false;
    
    try {
      engine = new HandTrackingEngine(labelFile, openniConfigFile, 
          calibrationFile);
    } catch (GeneralException ge) {
      logger.severe(ge.getMessage());
      System.exit(-1);
    }
    handEventListener = new HandEventListener();
    engine.addListener(handEventListener);
    
    if (displayOn) {
      packetController = new ProcessPacketController(engine.depthWidth(), 
                                                     engine.depthHeight());
      packetController.addKeyListener(this);
      packetController.derivativeSaveDir = derivativeSaveDir;
    }
    
    while (isRunning()) {
      if (isPaused())
        continue;
      engine.step();
      
      if (packetController != null)
        try {
          packetController.show(engine.packet());
        } catch (GeneralException ge) {
          logger.severe(ge.getMessage());
        }
      if (recording) {
        if (recorder == null) {
          rowToRecord = engine.depthHeight() / 2;
          String recordFileName = depthFilePrefix + rowToRecord;
          try {
            recorder = new Recorder(new FileOutputStream(recordFileName));
          } catch (FileNotFoundException e) {
              System.err.println(e.getMessage());
              System.exit(-1);
            }
        }
        ProcessPacket packet = engine.packet();
        recorder.print(engine.packet().depthFrameID, 
                       packet.getDepthRaw(engine.depthHeight() / 2));
      }
    }

    print();
    engine.release();
    System.exit(0);
  }
  
  public void print() {
    // Prints finger events.
    PrintWriter pw = null;
    try {
      pw = new PrintWriter(fingertipFile);
      handEventListener.toOutput(pw);
      System.out.println("Tracker controller output done.");
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
      case KeyEvent.VK_E:
        printDepthDiff();
        break;
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
      case KeyEvent.VK_W:
        printDepthRaw();
        break;
      case KeyEvent.VK_L:
        recording = !recording;
        if (recording == false && recorder.isRecording())
          recorder.close();
      default:
        break;
    }
  }

    private void printDepthRaw() {
      PrintStream ps = null;
      try {
        ProcessPacket packet = engine.packet();
        ps = new PrintStream(
            new File(String.format("data/depth_raw/depth_raw%03d", 
                                   packet.depthFrameID)));
        int index = 0;
        for (int h = 0; h < engine.depthHeight(); h++) {
          for (int w = 0; w < engine.depthWidth(); w++, index++)
            ps.print(packet.depthRawData[index] + " ");
          ps.println();
        }
      } catch (FileNotFoundException e) {
        System.err.println(e.getMessage());
      } finally {
        if (ps != null)
          ps.close();
      }
      System.out.println("Wrote depth raw value to file.");
    }

    /**
     * Prints the current background subtracted depth frame to a file.
     */
    private void printDepthDiff() {
      ProcessPacket packet = engine.packet();
      ByteBuffer bb = packet.depthImage8U.getByteBuffer();
      PrintStream ps = null;
      try {
        ps = new PrintStream(new File(String.format(
            "data/depth_diff/depth_diff%03d.txt", packet.depthFrameID)));
        for (int h = 0; h < engine.depthHeight(); h++) {
          for (int w = 0; w < engine.depthWidth(); w++)
            ps.print((bb.get(h * engine.depthWidth() + w) & 0xff) + " ");
          ps.println();
        }
      } catch (FileNotFoundException e) {
        System.err.println(e.getMessage());
      } finally {
        if (ps != null)
          ps.close();
      }
    }

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
}
