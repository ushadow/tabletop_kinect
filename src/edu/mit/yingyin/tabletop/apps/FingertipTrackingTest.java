package edu.mit.yingyin.tabletop.apps;

import org.OpenNI.GeneralException;

import edu.mit.yingyin.tabletop.controllers.HandEventsController;
import edu.mit.yingyin.tabletop.models.HandTrackingEngine;

public class FingertipTrackingTest {
  private static final String MAIN_DIR = 
      "/afs/csail/u/y/yingyin/research/kinect/";
  private static final String OPENNI_CONFIG_FILE = 
      MAIN_DIR + "config/config.xml";
  private static final String CALIB_FILE = MAIN_DIR + "data/calibration.txt";
  
  public static void main(String[] args) {
    new FingertipTrackingTest();
  }
  
  private HandTrackingEngine engine;
  
  public FingertipTrackingTest() {
    HandEventsController heController = new HandEventsController();
    try {
      engine = new HandTrackingEngine(null, OPENNI_CONFIG_FILE, CALIB_FILE);
    } catch (GeneralException ge) {
      System.err.println(ge.getMessage());
      System.exit(-1);
    }
    engine.addListener(heController);
    while (!engine.isDone() && heController.isViewVisible())
      engine.step();
    engine.release();
    System.exit(0);
  }
}
