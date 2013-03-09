package edu.mit.yingyin.tabletop.views;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.vecmath.Point3f;

import edu.mit.yingyin.gui.ImageFrame;
import edu.mit.yingyin.tabletop.models.FeatureBuilder;
import edu.mit.yingyin.tabletop.models.Forelimb;
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
    if (packet.forelimbs.size() > 0)
      drawFeatureImage(imageArray, packet.forelimbs.get(0));
    frame.updateImage(bi);
    frame.setTitle(String.format("%s, frame id = %d", TITLE, 
                                 packet.depthFrameID));
  }
  
  public void drawFeatureImage(byte[] imageArray, Forelimb forelimb) {
    int width = FeatureBuilder.IMAGE_WIDTH;
    int offset = FeatureBuilder.CONTINUOUS_FEATURE_SIZE;
    float[] features = FeatureBuilder.create(forelimb);
    for (int y = 0; y < width; y++) 
      for (int x = 0; x < width; x++) {
        float z = features[offset + (width - 1 - y) * width + x];
        if (z > 0)
          z += 100;
        imageArray[y * WIDTH + x] = (byte) z;
      }
  }

  public void drawPointCloud(byte[] imageArray, 
                             List<ForelimbFeatures> featureList) {
    Point topleft = new Point();
    for (ForelimbFeatures ff : featureList) {
      if (ff.hf != null && ff.hf.handPose != null) {
        int width = (int) ff.hf.handPoseWidth;
        for (Point3f p : ff.hf.handPose) {
          p.x += width / 2 + topleft.x;
          p.y = -p.y + width / 2 + topleft.y;
          if (p.x >= 0 && p.y >= 0)
            imageArray[((int) p.y * WIDTH) + (int) p.x] = (byte) 255;
        }
        Graphics2D g = (Graphics2D) bi.getGraphics();
        g.drawRect(topleft.x, topleft.y, width, width);
        topleft.move(width, 0);
      }
    }
  }
  
  @Override
  public void showStatusMessage(String message) {
    frame.setStatus(message);
  }
}  
