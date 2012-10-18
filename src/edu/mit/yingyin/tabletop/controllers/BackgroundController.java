package edu.mit.yingyin.tabletop.controllers;

import edu.mit.yingyin.tabletop.models.HandTrackingEngine;
import edu.mit.yingyin.tabletop.views.BackgroundFrame;

public class BackgroundController {
  private BackgroundFrame bv;
  private HandTrackingEngine engine;
  
  public BackgroundController(HandTrackingEngine engine) {
    bv = new BackgroundFrame(engine.depthWidth(), engine.depthHeight());
    this.engine = engine;
  }
  
  public void showUI() {
    bv.showUI();
  }
  
  public void update() {
    if (engine.isBgInitialize()) {
      bv.updateData(engine.diffBg(), engine.diffBgWidth());
    }
  }
}
