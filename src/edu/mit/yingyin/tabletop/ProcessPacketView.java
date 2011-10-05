package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCircle;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.vecmath.Point3f;

import rywang.viewer.FPSCounter;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.mit.yingyin.tabletop.ProcessPacket.ForelimbModel;
import edu.mit.yingyin.tabletop.ProcessPacket.ForelimbModel.ValConfiPair;

/**
 * Visualization for the ProcessPacket.
 * @author yingyin
 *
 */
public class ProcessPacketView {
  private class KeyController extends KeyAdapter {
    public void keyPressed(KeyEvent ke) {
      switch (ke.getKeyChar()) {
      case 'b':
        showBoundingBox = !showBoundingBox;
        break;
      case 'd':
        showConvexityDefects = !showConvexityDefects;
        break;
      case 'f':
        showFingertip = !showFingertip;
        break;
      case 'h':
        showHull = !showHull;
        break;
      case 'm':
        showMorphed = !showMorphed;
        break;
      default: 
        break;
      }
    }
  }
  
  private IplImage analysisImage;
  private IplImage appImage;
  private CanvasFrame[] frames = new CanvasFrame[2];
  private FPSCounter fpsCounter;
  private float[] histogram;
  private boolean showConvexityDefects = false;
  private boolean showHull = false;
  private boolean showMorphed = true;
  private boolean showFingertip = false;
  private boolean showBoundingBox = true;
  
  /**
   * Initializes the data structures.
   * @param width
   * @param height
   */
  public ProcessPacketView(int width, int height) {
    frames[0] = new CanvasFrame("Processed");
    fpsCounter = new FPSCounter("Processed", frames[0]);
    frames[1] = new CanvasFrame("Depth");
    for (CanvasFrame frame : frames)
      frame.setCanvasSize(width, height);
    analysisImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    appImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    frames[0].addKeyListener(new KeyController());
    CanvasFrame.tile(frames);
    
    histogram = new float[HandAnalyzer.MAX_DEPTH];
  }
  
  /**
   * Shows different visualizations of the ProcessPacket.
   * @param packet
   */
  public void show(ProcessPacket packet) {
    if (showMorphed)
      cvCopy(packet.morphedImage, analysisImage);
    else
      cvCopy(packet.depthImage8U, analysisImage);
    
    if (showBoundingBox)
      for (CvRect rect : packet.boundingBoxes){
        cvRectangle(analysisImage, 
            new CvPoint(rect.x(), 
                        rect.y() + rect.height() - HandAnalyzer.HAND_YCUTOFF), 
            new CvPoint(rect.x() + rect.width(), rect.y() + rect.height()), 
            CvScalar.WHITE, 1, 8, 0);
      }
    
    if (showFingertip)
      for (ForelimbModel forelimb : packet.foreLimbsFeatures)
        for (ValConfiPair<Point3f> p : forelimb.fingertips) {
          if (p.confidence > 0.5)
            cvCircle(analysisImage, new CvPoint((int)p.value.x, (int)p.value.y), 
                     4, CvScalar.WHITE, 5, 8, 0);
      }
    
    if (showConvexityDefects) {
      for (CvSeq seq : packet.convexityDefects) 
        CvUtil.drawConvexityDefects(seq, analysisImage);
    }
    
    if (showHull) {
      for (int i = 0; i < packet.hulls.size(); i++) {
        CvUtil.drawHullCorners(packet.hulls.get(i),packet.approxPolys.get(i), 
                               analysisImage);
      }
    }
    frames[0].showImage(analysisImage);
    fpsCounter.computeFPS();

    showAppImage(packet);
  }
  
  public void drawCircle(int x, int y) {
    cvCircle(appImage, new CvPoint(x, y), 4, CvScalar.WHITE, 5, 8, 0);
    frames[1].showImage(appImage);
  }
  
  public void release() {
    analysisImage.release();
    appImage.release();
    for (CanvasFrame frame : frames)
      frame.dispose();
    System.out.println("ProcessPacketView released.");
  }
  
  public void addKeyListener(KeyListener kl) {
    for (CanvasFrame frame : frames)
      frame.addKeyListener(kl);
  }
  
  public boolean isVisible() {
    boolean isVisible = true;
    for (CanvasFrame frame : frames)
      isVisible = isVisible && frame.isVisible();
    return isVisible;
  }
  
  public void hide() {
    for (CanvasFrame frame: frames)
      frame.setVisible(false);
  }
  
  private void showAppImage(ProcessPacket packet) {
    calcHist(packet.depthRawData);
    ByteBuffer ib = appImage.getByteBuffer();
    int widthStep = appImage.widthStep();
    for (int h = 0; h < packet.height; h++) 
      for (int w = 0; w < packet.width; w++) {
        int depth = packet.depthRawData[h * packet.width + w];
        ib.put(h * widthStep + w, (byte)(histogram[depth] * 255));  
      }
    frames[1].showImage(appImage);
    frames[1].setTitle("Processed FrameID = " + packet.depthFrameID);
  }
  
  private void calcHist(int[] depthRawData) {
    Arrays.fill(histogram, 0);
    
    int totalPoints = 0;
    for (int v : depthRawData) {
      if (v > 0) {
        histogram[v]++;
        totalPoints++;
      }
    }
    
    for (int i = 1; i < histogram.length; i++) 
      histogram[i] += histogram[i - 1];
  
    if (totalPoints > 0) {
      for (int i = 1; i < histogram.length; i++)
        histogram[i] = (totalPoints - histogram[i]) / (float)totalPoints;
    }
  }
}    