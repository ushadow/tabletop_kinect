package edu.mit.yingyin.tabletop;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
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
public class HandEventsController extends KeyAdapter 
    implements IHandEventListener {
  
  /**
   * A frame to show visualization of hand events.
   * @author yingyin
   *
   */
  private class HandEventsFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String IMAGE_FILE_NAME = 
        "/afs/csail/u/y/yingyin/research/kinect/data/checkerboard.png";
    
    private ImageComponent ic;
    
    public HandEventsFrame() {
      super("Hand events view");
      setUndecorated(true);
      setResizable(false);
      
      Dimension screenSize = SystemUtil.getVirtualScreenBounds().getSize();
      this.setBounds(new Rectangle(screenSize));
      this.setLocation(0, 0);
    
      ic = new ImageComponent(screenSize);
      try {
        ic.setImage(ImageIO.read(new File(IMAGE_FILE_NAME)));
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(-1);
      }
      getContentPane().add(ic);
    }
    
    public void showUI() {
      pack();
      setVisible(true);
    }
  }
  
  private HandEventsFrame handEventView = new HandEventsFrame();
  private List<FingerEvent> feList;
  
  public HandEventsController() {
    handEventView.addKeyListener(this);
    handEventView.showUI();
  }
  
  @Override
  public void fingerPressed(List<FingerEvent> feList) {
    this.feList = feList;
    handEventView.repaint();
  }
  
  @Override
  public void keyPressed(KeyEvent ke) {
    switch (ke.getKeyCode()) {
      case KeyEvent.VK_ESCAPE:
      case KeyEvent.VK_Q:
        handEventView.setVisible(false);
        System.exit(0);
        break;
      default:
        break;
    }
  }
}
