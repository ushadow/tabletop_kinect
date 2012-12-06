package edu.mit.yingyin.tabletop.apps;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.SwingUtilities;

import edu.mit.yingyin.tabletop.views.DisplayTargetFrame;

public class DiecticGestureTest extends KeyAdapter {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      
      @Override
      public void run() {
        new DiecticGestureTest();
        
      }
    });
  }

  public DiecticGestureTest() {
    DisplayTargetFrame frame = new DisplayTargetFrame();
    frame.addKeyListener(this);
    frame.showUI();
  }
  public void keyPressed(KeyEvent ke) {
    switch (ke.getKeyCode()) {
      case KeyEvent.VK_ESCAPE:
        System.exit(0);
        break;
      default:
        break;
    }
  }
}
