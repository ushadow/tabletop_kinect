package edu.mit.yingyin.tabletop.controllers;

import edu.mit.yingyin.tabletop.models.Background;
import edu.mit.yingyin.tabletop.views.BackgroundFrame;

public class BackgroundController {
  private BackgroundFrame bf;
  private boolean updated = false;
  
  public BackgroundController(int width, int height) {
    bf = new BackgroundFrame(width, height);
  }
  
  public void showUI() {
    bf.showUI();
  }
  
  public void update() {
    Background bg = Background.instance();
    if (bg == null)
      return;
    
    if (bg.isInitialized() && !updated ) {
      bf.updateData(bg.diffBuffer(), bg.diffBufferWidthStep());
      updated = true;
    }
  }
}
