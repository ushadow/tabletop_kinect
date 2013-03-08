package edu.mit.yingyin.tabletop.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

import edu.mit.yingyin.util.ObjectIO;

public class ManualGestureLabel {
  private static final Logger LOGGER = Logger.getLogger(
      ManualGestureLabel.class.getName());
  private TreeMap<Integer, String> labels;

  public ManualGestureLabel() {
    initEmptyLabel();
  };
  
  /**
   * Initializes from file.
   * @param filename
   * @throws IOException 
   */
  @SuppressWarnings("unchecked")
  public ManualGestureLabel(String filename) throws IOException {
    if (filename == null) {
      initEmptyLabel();
    } else {
      File f = new File(filename);
      if (f.exists()) {
        labels = (TreeMap<Integer, String>) ObjectIO.readObject(filename);
      } else {
        initEmptyLabel();
      }
    }
  }
  
  public void add(int frameID, String label) {
    labels.put(frameID, label);
  }
  
  public void save(String fileName) {
    try {
      ObjectIO.writeObject(labels, fileName);
    } catch (IOException e) {
      LOGGER.severe(e.getMessage());
    }
    String csvFileName = fileName + ".csv";
    PrintWriter pw = null;
    try {
      pw = new PrintWriter(csvFileName);
      Integer lastFrame = labels.lastKey();
      // Print header.
      pw.println(String.format("frameID,G,F", lastFrame));
      Iterator<Entry<Integer, String>> iterator = labels.entrySet().iterator();
      
      Entry<Integer, String> nextEntry = iterator.next();
      int curFrame = nextEntry.getKey();
      String label = nextEntry.getValue();
      println(pw, curFrame, label);
      while (iterator.hasNext()) {
        nextEntry = iterator.next();
        curFrame++;
        while (curFrame < nextEntry.getKey()) {
          println(pw, curFrame, label);
          curFrame++;
        }
        label = nextEntry.getValue();
        println(pw, curFrame, label);
      }
    } catch (FileNotFoundException e) {
      LOGGER.severe(e.getMessage());
    } finally {
      if (pw != null)
        pw.close();
    }
  }
 
  public String predecessor(int frameID) {
    Entry<Integer, String> predecessor = labels.floorEntry(frameID);
    if (predecessor != null) {
      return predecessor.getValue();
    }
    return "null";
  }
  
  private void println(PrintWriter pw, int frameID, String label) {
    if (!label.startsWith("0")) {
      pw.println(frameID + "," + label);
    }
  }
    
  private void initEmptyLabel() {
    labels = new TreeMap<Integer, String>();
  }
}
