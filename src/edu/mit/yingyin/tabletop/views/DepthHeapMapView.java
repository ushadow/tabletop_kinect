package edu.mit.yingyin.tabletop.views;

import javax.swing.JFrame;

import edu.mit.yingyin.util.HeatMap;
import edu.mit.yingyin.util.Gradient;

public class DepthHeapMapView extends JFrame {
  private static final long serialVersionUID = 3171330523281764709L;

  private HeatMap panel;
  public DepthHeapMapView(double[][] data) {
    super("Depth Heap Map");
    panel = new HeatMap(data, true, Gradient.GRADIENT_HOT);
    this.getContentPane().add(panel);
    setSize(panel.getPreferredSize());
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
  
  public void showUI() {
    setVisible(true);
  }
  
  public void update(double[][] data) {
    panel.updateData(data, true);
  }
}
