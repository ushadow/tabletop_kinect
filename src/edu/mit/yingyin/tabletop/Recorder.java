package edu.mit.yingyin.tabletop;

import java.io.OutputStream;
import java.io.PrintStream;

public class Recorder {
  private PrintStream ps;
  private boolean started = false;
  
  public Recorder(OutputStream os) {
    ps = new PrintStream(os);
    started = true;
    System.out.println("Started recording.");
  }
  
  public void record(int frameID, int[] aRow) {
    ps.println(frameID);
    for (int v : aRow)
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
