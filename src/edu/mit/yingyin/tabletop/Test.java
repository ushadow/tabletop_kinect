package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.mit.yingyin.gui.ImageFrame;

public class Test {
  public class MouseController extends MouseAdapter {
    public void mousePressed(MouseEvent me) {
      Point p = me.getPoint();
      System.out.println("(" + p.x + ", " + p.y + ")");
    }
  }
  
  public class KeyController extends KeyAdapter {
    public void keyPressed(KeyEvent ke) {
      System.out.println("Key pressed");
    }
  }
  
  public Test() {
   CanvasFrame frame = new CanvasFrame("Test");
   for (Component c : frame.getComponents()) {
     System.out.println("component " + c);
     c.addMouseListener(new MouseController());
   }
     
   frame.addMouseListener(new MouseController());
   IplImage appImage = IplImage.create(100, 100, IPL_DEPTH_8U, 1);
   frame.showImage(appImage);
  }
  
  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      
      @Override
      public void run() {
        // TODO Auto-generated method stub
        new Test(); 
      }
    });
  }
}
