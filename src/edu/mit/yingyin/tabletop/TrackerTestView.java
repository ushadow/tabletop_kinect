package edu.mit.yingyin.tabletop;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JFrame;

import edu.mit.yingyin.util.SystemUtil;

public class TrackerTestView extends JFrame {
  private static final long serialVersionUID = -31105214851958675L;

  public TrackerTestView() {
    super("Tracker test view");
    setUndecorated(true);
    setResizable(false);
    
    Dimension screenSize = SystemUtil.getVirtualScreenBounds().getSize();
    this.setBounds(new Rectangle(screenSize));
    this.setLocation(0, 0);
  }
  
  public void showView() {
    pack();
    setVisible(true);
  }
}
