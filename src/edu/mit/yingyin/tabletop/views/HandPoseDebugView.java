package edu.mit.yingyin.tabletop.views;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.swing.JFrame;

import edu.mit.yingyin.gui.ImageFrame;
import edu.mit.yingyin.tabletop.models.ProcessPacket;
import edu.mit.yingyin.tabletop.models.ProcessPacket.ForelimbFeatures;
import edu.mit.yingyin.tabletop.views.ProcessPacketView.DebugView;

public class HandPoseDebugView implements DebugView {
  private static final int WIDTH = 640, HEIGHT = 480;
  private static final String TITLE = "Hand pose debug view";
  
  private final ImageFrame frame;
  private final BufferedImage bi;
 
  public HandPoseDebugView() {
    bi = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
    frame = new ImageFrame(TITLE, bi);
  }
  
  @Override
  public JFrame frame() { return frame; }

  @Override
  public void showDebugImage(ProcessPacket pacekt) {
    byte[] imageArray = ((DataBufferByte)bi.getRaster().getDataBuffer()).
                        getData();
    Arrays.fill(imageArray, (byte) 0);
    for (ForelimbFeatures ff : pacekt.forelimbFeatures) {
      if (ff.handPose != null) {
        int radius = (int) ff.hpd.radius();
        FloatBuffer fb = ff.handPose.getFloatBuffer();
        fb.rewind();
        float[] p = new float[3];
        for (int i = 0; i < ff.handPose.rows(); i++) {
          fb.get(p);
          for (int j = 0; j < 2; j++) {
            p[j] = p[j] + radius;
          }
          if (p[0] >=0 && p[1] >= 0)
            imageArray[((int) p[1] * WIDTH) + (int) p[0]] = (byte) 255;
        }
        Graphics2D g = (Graphics2D) bi.getGraphics();
        g.drawOval(0, 0, radius * 2, radius * 2);
      }
    }
    frame.updateImage(bi);
  }

  @Override
  public void showStatusMessage(String message) {
    frame.setStatus(message);
  }
}  
