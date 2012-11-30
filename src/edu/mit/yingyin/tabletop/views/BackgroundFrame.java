package edu.mit.yingyin.tabletop.views;

import java.nio.FloatBuffer;

import javax.swing.JFrame;

import edu.mit.yingyin.util.Gradient;
import edu.mit.yingyin.util.HeatMap;

/**
 * Visualization of the background.
 * @author yingyin
 *
 */
public class BackgroundFrame extends JFrame {
  private static final long serialVersionUID = -3270421934205678686L;
  private static final boolean USE_GRAPHICS_YAXIS = true;

  private HeatMap hm;
  private double[][] data;
  private int width, height;

  public BackgroundFrame(int width, int height) {
    super("Background View");
    this.width = width;
    this.height = height;

    data = new double[width][height];
    hm = new HeatMap(data, USE_GRAPHICS_YAXIS, Gradient.GRADIENT_HOT);
    this.getContentPane().add(hm);
    setSize(hm.getPreferredSize());
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
        data[w][h] = clip(val, 0, (float) 1.5);
      }
    hm.updateData(data, USE_GRAPHICS_YAXIS);
  }
  
  private float clip(float v, float lb, float hb) {
    v = v > lb ? v : lb;
    v = v < hb ? v : hb;
    return v;
  }
}
