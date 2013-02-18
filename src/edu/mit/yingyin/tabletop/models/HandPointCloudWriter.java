package edu.mit.yingyin.tabletop.models;

import java.io.File;	
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.FloatBuffer;
import java.util.Properties;
import java.util.logging.Logger;

import com.googlecode.javacv.cpp.opencv_core.CvMat;

import edu.mit.yingyin.tabletop.models.ProcessPacket.ForelimbFeatures;
import edu.mit.yingyin.util.FileUtil;

public class HandPointCloudWriter {
  private static final String HAND_POINT_CLOUD_PROP = "hand-point-cloud-file";
  private static final Logger LOGGER = Logger.getLogger(
      HandPointCloudWriter.class.getName());
  
  private PrintWriter pw;
  
  public HandPointCloudWriter(String mainDir, Properties prop) {
    String file = prop.getProperty(HAND_POINT_CLOUD_PROP, null);
    if (file != null) {
      file = FileUtil.join(mainDir, EnvConstant.HAND_POINT_CLOUD_DIR, file);
      try {
        pw = new PrintWriter(new File(file));
      } catch (FileNotFoundException fnfe) {
        LOGGER.severe(fnfe.getMessage());
        System.exit(-1);
      }
    }
  }
  
  public void output(ProcessPacket packet) {
    if (pw != null) {
      for (ForelimbFeatures ff : packet.forelimbFeatures) {
        if (ff.handPose != null) {
          pw.println(packet.depthFrameID + "," + 
                     pointCloudToString(ff.handPose));
        }
      }
    }
  }

  public String pointCloudToString(CvMat pointCloud) {
    StringBuffer sb = new StringBuffer();
    FloatBuffer fb = pointCloud.getFloatBuffer();
    fb.rewind();
    float[] p = new float[3];
    for (int i = 0; i < pointCloud.rows(); i++) {
      fb.get(p);
    }
    return sb.toString();
  }
}
