package edu.mit.yingyin.tabletop.app;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.ByteBuffer;

import edu.mit.yingyin.tabletop.DebugView;
import edu.mit.yingyin.tabletop.FullOpenNIDevice;
import edu.mit.yingyin.tabletop.HandAnalyzer;
import edu.mit.yingyin.tabletop.OpenNIDevice;
import edu.mit.yingyin.tabletop.ProcessPacket;
import edu.mit.yingyin.tabletop.Table;
import edu.mit.yingyin.tabletop.Tracker;
import edu.mit.yingyin.tabletop.Tracker.FingerEvent;
import edu.mit.yingyin.tabletop.Tracker.TrackerListener;

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
        case KeyEvent.VK_R:
          printDepthRaw();
          break;
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
      ByteBuffer bb = packet.depthImage.getByteBuffer();
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

  private class TrackerController implements TrackerListener {
    @Override
    public void fingerPressed(FingerEvent fe) {
      debugView.drawCircle(fe.x, fe.y);
    }
  }

  public static void main(String[] args) {
    new FingertipTrackingApp();
  }

  private String configFile = "config/config.xml";
  private OpenNIDevice openni;
  private DebugView debugView;
  private int depthWidth, depthHeight;
  private ProcessPacket packet;
  boolean pause = false;

  public FingertipTrackingApp() {
    System.out.println("java.library.path = "
        + System.getProperty("java.library.path"));
    openni = new FullOpenNIDevice(configFile);
    depthWidth = openni.getDepthWidth();
    depthHeight = openni.getDepthHeight();
    HandAnalyzer analyzer = new HandAnalyzer(depthWidth, depthHeight);
    packet = new ProcessPacket(depthWidth, depthHeight);

    debugView = new DebugView(depthWidth, depthHeight);
    debugView.addKeyListener(new KeyController());

    Table table = new Table();
    Tracker tracker = new Tracker(table);
    tracker.addListener(new TrackerController());

    // Only one ProcessPacket present at all time.
    while (debugView.isVisible()) {
      if (pause == true)
        continue;
      try {
        openni.waitDepthUpdateAll();
        openni.getDepthArray(packet.depthRawData);
        packet.depthFrameID = openni.getDepthFrameID();
      } catch (Exception e) {
        System.err.println(e.getMessage());
        System.exit(-1);
      }

      if (!table.isInitialized())
        table.init(packet.depthRawData, depthWidth, depthHeight);
      analyzer.analyzeData(packet);
      debugView.show(packet);
      tracker.update(packet.foreLimbsFeatures);
    }
    openni.release();
    analyzer.release();
    packet.release();
    debugView.cleanUp();
    System.exit(0);
  }
}
