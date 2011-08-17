package edu.mit.yingyin.tabletop.app;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import edu.mit.yingyin.gui.ImageComponent;
import edu.mit.yingyin.gui.ImageViewer;
import edu.mit.yingyin.image.ImageConvertUtils;
import edu.mit.yingyin.tabletop.OpenNIWrapper;

public class ManualLabelApp {
  private class LabelView extends ImageComponent{

    private static final long serialVersionUID = -516675143416854662L;

    public LabelView(Dimension d) {
      super(d);
    }
    
    @Override
    public void paint(Graphics g) {
      super.paint(g);
    }
  }

  private class Controller extends KeyAdapter implements MouseListener {
    public void keyPressed(KeyEvent ke) {
      switch (ke.getKeyCode()) {
      case KeyEvent.VK_ESCAPE:
      case KeyEvent.VK_Q:
        exit();
        break;
      case KeyEvent.VK_N:
        nextImage();
        break;
      case KeyEvent.VK_UP:
        rate++;
        break;
      case KeyEvent.VK_DOWN:
        rate--;
        break;
      default: break;
      }
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
      // TODO Auto-generated method stub
      
    }
  }

  private static String openniConfigFile = "config/config.xml";

  int[] depthRawData;

  ImageViewer viewer;
  OpenNIWrapper openni;
  BufferedImage image;
  int width, height;
  int frameID = 0;
  int rate = 1;
  
  public ManualLabelApp() {
    openni = new OpenNIWrapper(openniConfigFile);
    width = openni.getDepthWidth();
    height = openni.getDepthHeight();
    depthRawData = new int[width * height];
    image = null;
    viewer = new ImageViewer("Label", 
                             new LabelView(new Dimension(width, height)));
    viewer.addKeyListener(new Controller());
    viewer.addWindowListener(new WindowAdapter() {
      
      public void windowClosing(WindowEvent arg0) {
        exit();
      }
    });
  }
  
  private void exit() {
    openni.cleanUp();
    System.exit(0);
  }
  
  private void nextImage() {
    openni.waitAnyUpdateAll();
    frameID++;
    while (frameID % rate != 0) {
      openni.waitAnyUpdateAll();
      frameID++;
    }
    openni.getDepthMap(depthRawData);
    image = ImageConvertUtils.depthToGrayBufferedImage(depthRawData, width,
        height);
    viewer.show(image);
    viewer.setTitle("Frame = " + frameID);
  }

  public static void main(String[] args) {
    new ManualLabelApp();
  }
}
