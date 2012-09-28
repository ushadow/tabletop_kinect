package edu.mit.yingyin.tabletop.views;

import java.nio.FloatBuffer;

import javax.swing.JFrame;

import edu.mit.yingyin.util.Gradient;
import edu.mit.yingyin.util.HeatMap;

public class BackgroundView extends JFrame {

  private static final long serialVersionUID = -3270421934205678686L;
  private static final boolean USE_GRAPHICS_YAXIS = true;

  private HeatMap panel;
  private double[][] data;
  private int width, height;

  public BackgroundView(int width, int height) {
    super("Background View");
    this.width = width;
    this.height = height;

    data = new double[width][height];
    panel = new HeatMap(data, USE_GRAPHICS_YAXIS, Gradient.GRADIENT_HOT);
    this.getContentPane().add(panel);
    setSize(panel.getPreferredSize());
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  public void showUI() {
    pack();
    setVisible(true);
  }

  public void updateData(FloatBuffer fb, int fbWidth) {
    fb.rewind();
    for (int h = 0; h < height; h++)
      for (int w = 0; w < width; w++) {
        int index = h * fbWidth + w;
        float val = fb.get(index);
        data[w][h] = val;
      }
    panel.updateData(data, USE_GRAPHICS_YAXIS);
  }
}
