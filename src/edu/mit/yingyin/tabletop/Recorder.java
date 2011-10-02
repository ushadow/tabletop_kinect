package edu.mit.yingyin.tabletop;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Records data to output stream.
 * @author yingyin
 *
 */
public class Recorder {
  private PrintStream ps;
  private boolean started = false;
  
  public Recorder(OutputStream os) {
    ps = new PrintStream(os);
    started = true;
    System.out.println("Started recording.");
  }
  
  /**
   * Prints frameId and the date in the row.
   * @param frameID
   * @param row
   */
  public void print(int frameID, int[] row) {
    ps.print(frameID + " ");
    for (int v : row)
      ps.print(v + " ");
    ps.println();
  }
  
  public void close() {
    ps.close();
    started = false;
    System.out.println("Stopped recording.");
  }
  
  public boolean isRecording() {
    return started;
  }
}
