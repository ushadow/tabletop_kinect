package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_16U;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_MOP_OPEN;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvMorphologyEx;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_calib3d.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;

public class HandProcessor {
  static public class DebugImage {
    private IplImage image;
    public DebugImage(int width, int height) {
      image = IplImage.create(width, height, IPL_DEPTH_16U, 1);
    }
    
    public BufferedImage draw(CvSeq contours) {
      cvZero(image);
      for (CvSeq c = contours; c != null; c = contours.h_next()){
        cvDrawContours(image, c, CvScalar.WHITE, CvScalar.BLACK, -1, CV_FILLED, 
                       8);
      }
      return image.getBufferedImage();
    }
  }
  
  private static final int CVCLOSE_ITR = 2;
  private static final int CVCONTOUR_APPROX_LEVEL = 2;
  private static final int MAX_DEPTH = 1600;
  
  private int[] bgDepthMap;
  private IplImage depthImage;
  private CvMemStorage tempMem;
  private CvSeq contours;
  
  public HandProcessor(int width, int height) {
    depthImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    tempMem = cvCreateMemStorage(0);
  }
  
  public void processData(int[] depthMap) {
    if (bgDepthMap == null) {
      bgDepthMap = depthMap.clone();
    }
    
    ByteBuffer ib = depthImage.getByteBuffer();
    for (int i = 0; i < depthMap.length; i++) {
      if (bgDepthMap[i] - depthMap[i] < 5)
        ib.put(i, (byte)0);
      else ib.put(i, (byte)((char)depthMap[i] * 255 / MAX_DEPTH));
    }
    
    findConnectedComponents(depthImage, 1, 4);
  }
  
  public void findConnectedComponents(IplImage mask, int poly1_hull0, 
                                      float perimScale) {
    cvMorphologyEx(mask, mask, null, null, CV_MOP_OPEN, CVCLOSE_ITR);
    cvClearMemStorage(tempMem);
    CvContourScanner scanner = cvStartFindContours(mask, tempMem, 
        Loader.sizeof(CvContour.class), CV_RETR_EXTERNAL, 
        CV_CHAIN_APPROX_SIMPLE);
    CvSeq c;
    int numCont = 0;
    double q = (mask.height() + mask.width()) / perimScale;
    while( (c = cvFindNextContour(scanner)) != null ) {
      double len = cvContourPerimeter(c);
      if (len < q) {
        cvSubstituteContour(scanner, null);
      } else {
        CvSeq cNew;
        if (poly1_hull0 == 1) {
          cNew = cvApproxPoly(c, Loader.sizeof(CvContour.class), tempMem,
              CV_POLY_APPROX_DP, CVCONTOUR_APPROX_LEVEL, 0);
        } else {
          cNew = cvConvexHull2(c, tempMem, CV_CLOCKWISE, 1);
        }
        cvSubstituteContour(scanner, cNew);
        numCont++;
      } 
    }
    contours = cvEndFindContours(scanner);
    System.out.println("Number of contours = " + numCont);
  }
  
  public CvSeq getContours() {  return contours; }
  
}
