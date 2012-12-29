package edu.mit.yingyin.tabletop.views;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.swing.JFrame;

import com.googlecode.javacv.cpp.opencv_core.CvMat;

import edu.mit.yingyin.gui.ImageFrame;
import edu.mit.yingyin.tabletop.models.ProcessPacket;
import edu.mit.yingyin.tabletop.models.ProcessPacket.ForelimbFeatures;
import edu.mit.yingyin.tabletop.views.ProcessPacketView.DebugView;

public class HandPoseDebugView implements DebugView {
  private static final Logger LOGGER = Logger.getLogger(
      HandPoseDebugView.class.getName());
  private static final int[] bounds = {640, 480, 256};
  private static final String TITLE = "Hand pose debug view";
  private final ImageFrame frame;
  private final BufferedImage bi;
  private final float[][] minMax = new float[3][2];
  
  public HandPoseDebugView() {
    bi = new BufferedImage(bounds[0], bounds[1], BufferedImage.TYPE_BYTE_GRAY);
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
        findMinMax(ff.handPose);
        FloatBuffer fb = ff.handPose.getFloatBuffer();
        fb.rewind();
        float[] p = new float[3];
        for (int i = 0; i < ff.handPose.rows(); i++) {
          fb.get(p);
          for (int j = 0; j < 3; j++) {
            p[j] = p[j] - minMax[j][0];
          }
          int v = (int) p[2];
          if (v != 0)
            v += 100;
          imageArray[((int) p[1] * bounds[0]) + (int) p[0]] = (byte) v;
        }
      }
    }
    frame.updateImage(bi);
  }
  
  /**
   * 
   * @param points a matrix of float numbers. Each row is a 3 dimensional point.
   */
  private void findMinMax(CvMat points) {
    FloatBuffer fb = points.getFloatBuffer();
    fb.rewind();
    for (int i = 0; i < 3; i++) {
      minMax[i][0] = Float.POSITIVE_INFINITY;
      minMax[i][1] = Float.NEGATIVE_INFINITY;
    }
    float[] p = new float[3];
    for (int i = 0; i < points.rows(); i++) {
      fb.get(p);
      for (int j = 0; j < 3; j++) {
        minMax[j][0] = Math.min(p[j], minMax[j][0]);
        minMax[j][1] = Math.max(p[j], minMax[j][1]);
      }
    }
    for (int i = 0; i < 3; i++)
      LOGGER.info("boundary: " + Arrays.toString(minMax[i]));
  }
}  
