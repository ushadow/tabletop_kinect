package edu.mit.yingyin.tabletop.app;

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

import org.OpenNI.GeneralException;

import edu.mit.yingyin.tabletop.ProcessPacketView;
import edu.mit.yingyin.tabletop.FullOpenNIDevice;
import edu.mit.yingyin.tabletop.HandAnalyzer;
import edu.mit.yingyin.tabletop.OpenNIDevice;
import edu.mit.yingyin.tabletop.ProcessPacket;
import edu.mit.yingyin.tabletop.Recorder;
import edu.mit.yingyin.tabletop.Table;
import edu.mit.yingyin.tabletop.HandTracker;
import edu.mit.yingyin.tabletop.HandTracker.FingerEvent;
import edu.mit.yingyin.tabletop.HandTracker.HandTrackerListener;

/**
 * Application that tracks the fingertips in data from an OpenNI device. Saves
 * fingertip events to file in the end.
 * 
 * @author yingyin
 *
 */
public class FingertipTrackingApp {
  private class KeyController extends KeyAdapter {
    public void keyPressed(KeyEvent ke) {
      switch (ke.getKeyCode()) {
        case KeyEvent.VK_D:
          printDepthDiff();
          break;
        case KeyEvent.VK_P:
          pause = !pause;
          break;
        case KeyEvent.VK_ESCAPE:
        case KeyEvent.VK_Q:
          debugView.hide();
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
        ps = new PrintStream(
            new File(String.format("data/depth_raw/depth_raw%03d", 
                                   packet.depthFrameID)));
        int index = 0;
        for (int h = 0; h < depthHeight; h++) {
          for (int w = 0; w < depthWidth; w++, index++)
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
      ByteBuffer bb = packet.depthImage8U.getByteBuffer();
      PrintStream ps = null;
      try {
        ps = new PrintStream(new File(String.format("tmp/depth_diff%03d.txt",
            packet.depthFrameID)));
        for (int h = 0; h < depthHeight; h++) {
          for (int w = 0; w < depthWidth; w++)
            ps.print((bb.get(h * depthWidth + w) & 0xff) + " ");
          ps.println();
        }
      } catch (FileNotFoundException e) {
        System.err.println(e.getMessage());
      } finally {
        if (ps != null)
          ps.close();
      }
    }
  }

  private class TrackerController implements HandTrackerListener {
    /**
     * List of finger events detected in a frame.
     */
    private List<List<FingerEvent>> fingerEventList = 
        new ArrayList<List<FingerEvent>>();
    
    @Override
    public void fingerPressed(List<FingerEvent> feList) {
      if (debugView != null) {
        for (FingerEvent fe : feList)
          debugView.drawCircle((int)fe.fingertip.x, (int)fe.fingertip.y);
      }
      fingerEventList.add(feList);
    }
    
    public void toOutput(PrintWriter pw) {
      pw.println("# frame-id x y z x y z ...");
      for (List<FingerEvent> list : fingerEventList) {
        for (int i = 0; i < list.size(); i++) {
          if (i == 0) {
            pw.print(String.format("%d %d %d %d ", list.get(i).frameID, 
                (int)list.get(i).fingertip.x, (int)list.get(i).fingertip.y, 
                (int)list.get(i).fingertip.z));
          } else {
            pw.print(String.format("%d %d %d ", (int)list.get(i).fingertip.x, 
                (int)list.get(i).fingertip.y, (int)list.get(i).fingertip.z));
          }
        }
        pw.println();
      }
    }
  }

  private static String DIR = "/afs/csail/u/y/yingyin/research/kinect/";
  private static String CONFIG_FILE = DIR + "config/fingertip_tracking.config";
          
  public static void main(String[] args) {
    new FingertipTrackingApp();
  }

  private OpenNIDevice openni;
  private ProcessPacketView debugView;
  private int depthWidth, depthHeight;
  private ProcessPacket packet;
  boolean pause = false;
  boolean recording = false;
  Recorder recorder;
  private int rowToRecord = 0;
  private int prevDepthFrameID = -1;

  public FingertipTrackingApp() {
    System.out.println("java.library.path = "
        + System.getProperty("java.library.path"));
    
    Properties config = new Properties();
    FileInputStream in = null;
    try {
      in = new FileInputStream(CONFIG_FILE);
      config.load(in);
      in.close();
    } catch (FileNotFoundException fnfe) {
      fnfe.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    String openniConfigFile = DIR + config.getProperty("openni-config", 
                                                       "config/config.xml");
    String depthFilePrefix = DIR + config.getProperty("depth-file-prefix", 
        "data/depth_raw/depth_row");
    String fingertipFile = DIR + config.getProperty("fingertip-file", 
        "data/fingertip/fingertip.txt");
    String displayOnProperty = config.getProperty("display-on", "true");
    boolean displayOn = true;
    if (displayOnProperty.equals("false"))
      displayOn = false;
    
    try {
      openni = new FullOpenNIDevice(openniConfigFile);
    } catch (GeneralException ge) {
      ge.printStackTrace();
      System.exit(-1);
    }
    depthWidth = openni.getDepthWidth();
    depthHeight = openni.getDepthHeight();
    HandAnalyzer analyzer = new HandAnalyzer(depthWidth, depthHeight);
    packet = new ProcessPacket(depthWidth, depthHeight);

    if (displayOn) {
      debugView = new ProcessPacketView(depthWidth, depthHeight);
      debugView.addKeyListener(new KeyController());
    }
    
    Table table = new Table();
    HandTracker tracker = new HandTracker(table);
    TrackerController trackerController = new TrackerController();
    tracker.addListener(trackerController);

    // A one thread process. Only one ProcessPacket present at all time.
    while (debugView != null && debugView.isVisible() || displayOn == false) {
      if (debugView != null && pause)
        continue;
      try {
        openni.waitDepthUpdateAll();
        openni.getDepthArray(packet.depthRawData);
        prevDepthFrameID = packet.depthFrameID;
        packet.depthFrameID = openni.getDepthFrameID();
        
        // Breaks at the last frame.
        if (packet.depthFrameID < prevDepthFrameID)
          break;
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(-1);
      }

      if (!table.isInitialized())
        table.init(packet.depthRawData, depthWidth, depthHeight);
      analyzer.analyzeData(packet);
      
      if (debugView != null)
        debugView.show(packet);
      
      tracker.update(packet.foreLimbsFeatures, packet.depthFrameID);

      if (recording) {
        if (recorder == null) {
          rowToRecord = depthHeight / 2;
          String recordFileName = depthFilePrefix + rowToRecord;
          try {
            recorder = new Recorder(new FileOutputStream(recordFileName));
          } catch (FileNotFoundException e) {
              System.err.println(e.getMessage());
              System.exit(-1);
            }
        }
        recorder.print(packet.depthFrameID, 
                       packet.getDepthRaw(depthHeight / 2));
      }
    }
    
    // Prints finger events.
    PrintWriter pw = null;
    try {
      pw = new PrintWriter(fingertipFile);
      trackerController.toOutput(pw);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } finally {
      if (pw != null)
        pw.close();
    }
    
    openni.release();
    analyzer.release();
    packet.release();
    if (debugView != null)
      debugView.release();  
    System.exit(0);
  }
}
