package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCircle;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.ByteBuffer;
import java.util.List;

import javax.vecmath.Point3f;

import rywang.viewer.FPSCounter;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.mit.yingyin.gui.ImageComponent;
import edu.mit.yingyin.gui.ImageFrame;
import edu.mit.yingyin.image.ImageConvertUtils;
import edu.mit.yingyin.tabletop.Forelimb.ValConfiPair;

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
        // Shows all the detected fingertips.
        System.out.println("Toggled show fingertip.");
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
  
  private class RGBComponent extends ImageComponent {
    private static final long serialVersionUID = 3880292315260748112L;

    public RGBComponent(Dimension d) {
      super(d);
    }
    
    @Override
    public void paint(Graphics g) {
      super.paint(g);
      
      if (packet == null)
        return;
      
      Graphics2D g2d = (Graphics2D) g;
      g2d.setColor(Color.red);
      for (Forelimb limb : packet.foreLimbsFeatures) {
        for (List<Point3f> finger : limb.fingers) {
          for (Point3f p : finger) {
            g2d.drawLine((int)p.x, (int)p.y, (int)p.x, (int)p.y);
          }
        }
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
  private boolean showFingertip = true;
  private boolean showBoundingBox = true;
  private ImageFrame rgbFrame;
  private ProcessPacket packet;
  
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
    
    CanvasFrame.tile(frames);
    rgbFrame = new ImageFrame("RGB", 
                              new RGBComponent(new Dimension(width, height)));
    Rectangle rect = frames[0].getBounds();
    rgbFrame.setLocation(0, rect.y + rect.height);

    histogram = new float[HandAnalyzer.MAX_DEPTH];
    
    KeyController kc = new KeyController();
    for (CanvasFrame f : frames)
      f.addKeyListener(kc);
    rgbFrame.addKeyListener(kc);
  }
  
  /**
   * Shows different visualizations of the ProcessPacket.
   * @param packet
   */
  public void show(ProcessPacket packet) {
    this.packet = packet;
    
    if (showMorphed)
      cvCopy(packet.morphedImage, analysisImage);
    else
      cvCopy(packet.depthImage8U, analysisImage);
    
    if (showBoundingBox)
      for (CvRect rect : packet.boundingBoxes){
        cvRectangle(analysisImage, 
            new CvPoint(rect.x(), rect.y()), 
            new CvPoint(rect.x() + rect.width(), rect.y() + rect.height()), 
            CvScalar.WHITE, 1, 8, 0);
      }
    
    if (showFingertip)
      for (Forelimb forelimb : packet.foreLimbsFeatures)
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

    rgbFrame.show(packet.morphedImage.getBufferedImage());

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
    rgbFrame.addKeyListener(kl);
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
  
  /**
   * Displays the application image.
   * @param packet
   */
  private void showAppImage(ProcessPacket packet) {
    ImageConvertUtils.arrayToHistogram(packet.depthRawData, histogram);
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
}    