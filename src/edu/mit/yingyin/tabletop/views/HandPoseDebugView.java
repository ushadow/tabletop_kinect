package edu.mit.yingyin.tabletop.views;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.vecmath.Point3f;

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
  public void showDebugImage(ProcessPacket packet) {
    byte[] imageArray = ((DataBufferByte)bi.getRaster().getDataBuffer()).
                        getData();
    Arrays.fill(imageArray, (byte) 0);
    Point topleft = new Point();
    for (ForelimbFeatures ff : packet.forelimbFeatures) {
      if (ff.handPose != null) {
        int width = (int) ff.handPoseWidth;
        for (Point3f p : ff.handPose) {
          p.x += width / 2 + topleft.x;
          p.y += width / 2 + topleft.y;
          if (p.x >= 0 && p.y >= 0)
            imageArray[((int) p.y * WIDTH) + (int) p.x] = (byte) 255;
        }
        Graphics2D g = (Graphics2D) bi.getGraphics();
        g.drawRect(topleft.x, topleft.y, width, width);
        topleft.move(width, 0);
      }
    }
    frame.updateImage(bi);
    frame.setTitle(String.format("%s, frame id = %d", TITLE, 
                                 packet.depthFrameID));
  }

  @Override
  public void showStatusMessage(String message) {
    frame.setStatus(message);
  }
}  
