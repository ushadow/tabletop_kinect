package edu.mit.yingyin.tabletop.controllers;

import edu.mit.yingyin.tabletop.models.HandTrackingEngine;
import edu.mit.yingyin.tabletop.views.BackgroundFrame;

public class BackgroundController {
  private BackgroundFrame bf;
  private HandTrackingEngine engine;
  private boolean updated = false;
  
  public BackgroundController(HandTrackingEngine engine) {
    bf = new BackgroundFrame(engine.depthWidth(), engine.depthHeight());
    this.engine = engine;
  }
  
  public void showUI() {
    bf.showUI();
  }
  
  public void update() {
    if (engine.isBgInitialized() && !updated ) {
      bf.updateData(engine.diffBg(), engine.diffBgWidth());
      updated = true;
    }
  }
}
