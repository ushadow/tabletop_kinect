package edu.mit.yingyin.tabletop.models;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.OpenNI.Point3D;
import org.OpenNI.StatusException;

import com.googlecode.javacv.cpp.opencv_core.CvArr;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.mit.yingyin.tabletop.models.ProcessPacket.ForelimbFeatures;

public class HandPoseFeatureDetector {
  
  /**
   * Depth image width and height.
   */
  private final int width, height;
  private final OpenNIDevice openni;
  
  public HandPoseFeatureDetector(int width, int height, OpenNIDevice openni) {
    this.width = width;
    this.height = height;
    this.openni = openni;
  }
  
  public void detect(ProcessPacket packet) throws StatusException {
    for (ForelimbFeatures ff : packet.forelimbFeatures) {
      if (ff.handRegion != null) {
        Point3D[] world = preprocess(packet.depthRawData, ff.handRegion, 
                                     packet.foregroundMask); 
        
      }
    }
  }
  
  private Point3D[] preprocess(int[] rawDepthData, CvRect handRegion, 
                               IplImage mask) 
      throws StatusException 
  {
    ByteBuffer foregroundMask = mask.getByteBuffer();
    int maskWidthStep = mask.widthStep();
    int handWidth = handRegion.width();
    int handHeight = handRegion.height();
    ArrayList<Point3D> points = new ArrayList<Point3D>(handWidth * handHeight); 
    for (int y = handRegion.y(); y < handRegion.y() + handHeight; y++)
      for (int x = handRegion.x(); x < handRegion.x() + handWidth; x++) {
        if (Background.isForeground(foregroundMask.get(y * maskWidthStep + x)))
          points.add(new Point3D(x, y, rawDepthData[y * width + x]));
      }
    Point3D[] projective = new Point3D[points.size()];
    points.toArray(projective);
    return openni.convertProjectiveToRealWorld(projective);
  }
  
  private void doPCA(Point3D[] world) {
    
  }

}
