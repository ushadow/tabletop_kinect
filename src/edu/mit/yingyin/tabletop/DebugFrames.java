package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCircle;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;

import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.ByteBuffer;

import rywang.viewer.FPSCounter;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.mit.yingyin.tabletop.ProcessPacket.ForelimbFeatures;
import edu.mit.yingyin.tabletop.ProcessPacket.ForelimbFeatures.ValConfiPair;

public class DebugFrames {
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
  private static final long serialVersionUID = 1L;
  private IplImage canvasImage;
  private CanvasFrame[] frames = new CanvasFrame[2];
  private FPSCounter fpsCounter;
 
  private boolean showConvexityDefects = false;
  private boolean showHull = false;
  private boolean showMorphed = true;
  private boolean showFingertip = true;
  private boolean showBoundingBox = true;
  
  public DebugFrames(int width, int height) {
    frames[0] = new CanvasFrame("Processed");
    fpsCounter = new FPSCounter("Processed", frames[0]);
    frames[1] = new CanvasFrame("Depth");
    for (CanvasFrame frame : frames)
      frame.setCanvasSize(width, height);
    canvasImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    frames[0].addKeyListener(new KeyController());
    CanvasFrame.tile(frames);
  }
  
  public void show(ProcessPacket packet) {
    if (showMorphed)
      cvCopy(packet.morphedImage, canvasImage);
    else
      cvCopy(packet.depthImage, canvasImage);
    
    if (showBoundingBox)
      for (CvRect rect : packet.boundingBoxes){
        cvRectangle(canvasImage, 
            new CvPoint(rect.x(), 
                        rect.y() + rect.height() - HandAnalyzer.HAND_YCUTOFF), 
            new CvPoint(rect.x() + rect.width(), rect.y() + rect.height()), 
            CvScalar.WHITE, 1, 8, 0);
      }
    
    if (showFingertip)
      for (ForelimbFeatures forelimb : packet.foreLimbsFeatures)
        for (ValConfiPair<Point> p : forelimb.fingertips) {
          if (p.confidence > 0.5)
            cvCircle(canvasImage, new CvPoint(p.value.x, p.value.y), 4, 
                     CvScalar.WHITE, 5, 8, 0);
      }
    
    if (showConvexityDefects) {
      for (CvSeq seq : packet.convexityDefects) 
        CvUtil.drawConvexityDefects(seq, canvasImage);
    }
    
    if (showHull) {
      for (int i = 0; i < packet.hulls.size(); i++) {
        CvUtil.drawHullCorners(packet.hulls.get(i),packet.approxPolys.get(i), 
                               canvasImage);
      }
    }
    
    frames[0].showImage(canvasImage);
    ByteBuffer ib = canvasImage.getByteBuffer();
    for (int i = 0; i < packet.depthRawData.length; i++) {
      ib.put(i, (byte)((HandAnalyzer.MAX_DEPTH - packet.depthRawData[i]) *
                       255 / HandAnalyzer.MAX_DEPTH));
    }
    frames[1].showImage(canvasImage);
    fpsCounter.computeFPS();
  }
  
  public void cleanUp() {
    canvasImage.release();
    for (CanvasFrame frame : frames)
      frame.dispose();
    System.out.println("DebugFrame cleaned up.");
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
}    