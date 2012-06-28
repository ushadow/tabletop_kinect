package edu.mit.yingyin.gui;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class WindowUtils {
  public static JFrame makeWindowWrapComponent(Component component, String name, 
      final boolean killOnExit) {
    final JFrame frame = new JFrame(name);
    frame.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
          frame.setVisible(false);
          frame.dispose();
          if (killOnExit)
            System.exit(1);
        }
    });
    
    frame.add("Center", component);
    
    frame.pack();
    frame.setVisible(true);
    
    return frame;
  }
}



