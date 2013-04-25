package edu.mit.yingyin.tabletop.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.logging.Logger;

public class FeatureWriter {
  private static final Logger LOGGER = Logger.getLogger(
      FeatureWriter.class.getName());
  
  private PrintWriter pw;
  
  public FeatureWriter(String filename) {
    if (filename != null) {
      File file = new File(filename);
      try {
        pw = new PrintWriter(file);
        outputHeader();
      } catch (FileNotFoundException fnfe) {
        LOGGER.severe(fnfe.getMessage());
        System.exit(-1);
      }
    }
  }
  
  public void outputHeader() {
    if (pw != null) {
      pw.println(String.format("frame_id,continuous_feature_size,%d," +
      		"image_width,%d", FeatureBuilder.CONTINUOUS_FEATURE_SIZE, 
      		FeatureBuilder.imageWidth()));
    }
  }
  
  public void output(ProcessPacket packet) {
    if (pw != null) {
      for (Forelimb forelimb : packet.forelimbs) {
        pw.println(packet.depthFrameID + "," + 
                   featureToString(FeatureBuilder.create(forelimb)));
      }
    }
  }
  
  public void close() { 
    if (pw != null)
      pw.close();
  }

  public String featureToString(float[] features) {
    StringBuffer sb = new StringBuffer();
    for (float f : features) {
      sb.append(f + ",");
    }
    return sb.toString();
  }

}
