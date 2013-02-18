package edu.mit.yingyin.tabletop.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.vecmath.Point3f;

import edu.mit.yingyin.tabletop.models.ProcessPacket.ForelimbFeatures;
import edu.mit.yingyin.util.FileUtil;

public class HandPointCloudWriter {
  private static final String HAND_POINT_CLOUD_PROP = "hand-point-cloud-file";
  private static final Logger LOGGER = Logger.getLogger(
      HandPointCloudWriter.class.getName());
  
  private PrintWriter pw;
  
  public HandPointCloudWriter(String mainDir, Properties prop) {
    String filename = prop.getProperty(HAND_POINT_CLOUD_PROP, null);
    if (filename != null) {
      String dir = FileUtil.join(mainDir, EnvConstant.HAND_POINT_CLOUD_DIR);
      (new File(dir)).mkdir();
      filename = FileUtil.join(dir, filename);
      File file = new File(filename);
      try {
        pw = new PrintWriter(file);
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

  public String pointCloudToString(List<Point3f> pointCloud) {
    StringBuffer sb = new StringBuffer();
    for (Point3f p : pointCloud) {
      sb.append(String.format("%.3f,%.3f,%.3f,", p.x, p.y, p.z));
    }
    return sb.toString();
  }
}
