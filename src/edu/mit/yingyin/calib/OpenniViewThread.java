package edu.mit.yingyin.calib;

import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import org.OpenNI.GeneralException;
import org.OpenNI.StatusException;

import edu.mit.yingyin.tabletop.controllers.ProcessPacketController;
import edu.mit.yingyin.tabletop.models.HandAnalyzer;
import edu.mit.yingyin.tabletop.models.OpenNIDevice;
import edu.mit.yingyin.tabletop.models.ProcessPacket;

public class OpenniViewThread extends Thread {
  private static final Logger LOGGER = Logger.getLogger(
      OpenniViewThread.class.getName());
  private OpenNIDevice openni;
  private ProcessPacketController packetController;
  private HandAnalyzer analyzer;
  private boolean running = true;
  private int depthWidth, depthHeight;
  
  public OpenniViewThread(String openniConfigFile) {
    try {
      openni = new OpenNIDevice(openniConfigFile);
      depthWidth = openni.getDepthWidth();
      depthHeight = openni.getDepthHeight();
      analyzer = new HandAnalyzer(depthWidth, depthHeight, openni);
      packetController = new ProcessPacketController(depthWidth, depthHeight, 
          null);
    } catch (GeneralException e) {
      LOGGER.severe(e.getMessage());
      System.exit(-1);
    }
  }
  
  public void step() {
    try {
      openni.waitDepthUpdateAll();
      ProcessPacket packet = new ProcessPacket(depthWidth, depthHeight, openni);
      openni.getDepthArray(packet.depthRawData);
      packet.depthFrameID = openni.getDepthFrameID();
      analyzer.analyzeData(packet);
      synchronized (packetController) {
        packetController.show(packet);
      }
      packet.release();
    } catch (StatusException se) {
      LOGGER.severe(se.getMessage());
      System.exit(-1);
    } catch (GeneralException e) {
      LOGGER.severe(e.getMessage());
      System.exit(-1);
    }
  }
  
  @Override
  public void run() {
    while (running) {
      step();
    }
    openni.release();
    analyzer.release();
    packetController.release();
  }
  
  public void stopRunning() { running = false; }
  
  public BufferedImage capture() { 
    BufferedImage bi;
    synchronized (packetController) {
      bi = packetController.depthImage();
    }
    return bi;
  }
  
  public void addKeyListener(KeyListener kl) {
    packetController.addKeyListener(kl);
  }
}
