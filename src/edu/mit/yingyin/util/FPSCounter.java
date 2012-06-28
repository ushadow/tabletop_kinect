/*
 * Created on Jul 17, 2005
 */
package edu.mit.yingyin.util;

import java.awt.Frame;

/**
 * @author rywang
 */
public class FPSCounter {
  private Timer fpsTimer;

  private int fpsFrameCount;

  private String windowTitle;

  private Frame frame;

  private float lastFPS;

  public FPSCounter(String windowTitle, Frame frame) {
    this.windowTitle = windowTitle;
    this.frame = frame;

    fpsTimer = new Timer();
    fpsFrameCount = 0;
    lastFPS = 0;
  }

  public void setWindowTitle(String windowTitle) {
    this.windowTitle = windowTitle;
  }

  public float getLastFPS() {
    return lastFPS;
  }

  public void computeFPS() {
    fpsFrameCount += 1;
    int nFrames = 100;

    if (!frame.getTitle().startsWith(windowTitle)) {
      frame.setTitle(windowTitle);
    }
    if (fpsFrameCount == nFrames) {
      double fps = nFrames * 1000. / ((double) fpsTimer.getToc());
      lastFPS = (float) fps;
      // 1000/ms/nFrames --> fps
      try {
        frame.setTitle(windowTitle + ", FPS=" + (int) fps);
      } catch (Exception e) {
        System.out.println("Couldn't set FPS title: " + e);
      }
      fpsFrameCount = 0;
      fpsTimer.tic();
    }
  }
}
