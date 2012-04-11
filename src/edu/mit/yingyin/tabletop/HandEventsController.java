package edu.mit.yingyin.tabletop;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import edu.mit.yingyin.gui.ImageComponent;
import edu.mit.yingyin.tabletop.HandTracker.FingerEvent;
import edu.mit.yingyin.tabletop.HandTracker.IHandEventListener;
import edu.mit.yingyin.util.SystemUtil;

/**
 * Listens to hand events.
 * @author yingyin
 *
 */
public class HandEventsController implements IHandEventListener {
  
  /**
   * A frame to show visualization of hand events.
   * @author yingyin
   *
   */
  private class HandEventsFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String IMAGE_FILE_NAME = 
        "/afs/csail/u/y/yingyin/research/kinect/data/checker.png";
    
    private ImageComponent ic;
    
    public HandEventsFrame() {
      super("Hand events view");
      setUndecorated(true);
      setResizable(false);
      
      Dimension screenSize = SystemUtil.getVirtualScreenBounds().getSize();
      this.setBounds(new Rectangle(screenSize));
      this.setLocation(0, 0);
    
      ic = new ImageComponent(screenSize);
      getContentPane().add(ic);
    }
    
    public void showUI() {
      pack();
      setVisible(true);
    }
  }
  
  private HandEventsFrame handEventView = new HandEventsFrame();
  
  public HandEventsController() {
    handEventView.showUI();
  }
  
  @Override
  public void fingerPressed(List<FingerEvent> feList) {
  }
}
