package edu.mit.yingyin.calib;

import java.awt.Dimension;

import javax.swing.JFrame;

import edu.mit.yingyin.gui.ImageComponent;
import edu.mit.yingyin.util.SystemUtil;

public class CalibFrame extends JFrame {
  
  private static final long serialVersionUID = -7639876874520124670L;

  /**
   * Creates a frame with an <code>ImageComponent</code> at the center of the 
   * screen.
   * @param ic an <code>ImageComponent</code>.
   */
  public CalibFrame(ImageComponent ic) {
    super("Calibration Pattern");
    setUndecorated(true);
    setResizable(false);

    getContentPane().add(ic);
    
    Dimension screenSize = SystemUtil.getVirtualScreenBounds().getSize();
    
    Dimension d = ic.getPreferredSize();
    int xLoc = (screenSize.width - (int) d.getWidth()) / 2;
    int yLoc = screenSize.height - (int) d.getHeight();
    xLoc = xLoc < 0 ? 0 : xLoc;
    yLoc = yLoc < 0 ? 0 : yLoc;
    this.setLocation(xLoc, yLoc);
  }

  public void showUI() {
    pack();
    setVisible(true);
  }
}
