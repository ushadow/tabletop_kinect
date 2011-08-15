package edu.mit.yingyin.tabletop.app;

import edu.mit.yingyin.tabletop.OpenNIWrapper;

public class ManualLabelApp {
  private static String openniConfigFile = "config/config.xml";
  
  public static void main(String[] args) {
    OpenNIWrapper openni = new OpenNIWrapper();
    openni.initFromXmlFile(openniConfigFile);
    
    while (true) {
      openni.waitAnyUpdateAll();
    }
  }
}
