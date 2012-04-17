package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCircle;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.List;

import javax.vecmath.Point3f;

import rywang.viewer.FPSCounter;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.mit.yingyin.gui.ImageComponent;
import edu.mit.yingyin.gui.ImageFrame;
import edu.mit.yingyin.image.ImageConvertUtils;
import edu.mit.yingyin.tabletop.Forelimb.ValConfiPair;
import edu.mit.yingyin.tabletop.ProcessPacket.ForelimbFeatures;

/**
 * Visualization for the ProcessPacket.
 * @author yingyin
 *
 */
public class ProcessPacketController extends KeyAdapter implements MouseListener 
{
  private class RgbImageComponent extends ImageComponent {
    private static final long serialVersionUID = 3880292315260748112L;
    private static final int OVAL_WIDTH = 6;
    
    public RgbImageComponent(Dimension d) {
      super(d);
    }
    
    @Override
    public void paint(Graphics g) {
      super.paint(g);
      
      if (packet == null)
        return;
      
      Graphics2D g2d = (Graphics2D) g;
      g2d.setColor(Color.green);
      for (Forelimb limb : packet.forelimbs) {
        for (List<Point3f> finger : limb.fingers) {
          for (Point3f p : finger) {
            g2d.drawLine((int)p.x, (int)p.y, (int)p.x, (int)p.y);
          }
        }
      }
      
      // Draws labeled points.
      if (packet.labels != null) {
        for (Point p : packet.labels)
          g2d.drawOval(p.x - OVAL_WIDTH / 2, p.y - OVAL_WIDTH / 2, OVAL_WIDTH,
              OVAL_WIDTH);
      }
      
      // Draws measured points.
      synchronized (packet.forelimbs) {
        for (Forelimb forelimb : packet.forelimbs){
          g2d.setColor(Color.red);
          for (ValConfiPair<Point3f> p : forelimb.fingertips) {
            if (p.confidence > 0.5)
              g2d.drawOval((int)p.value.x - OVAL_WIDTH / 2, 
                  (int)p.value.y - OVAL_WIDTH / 2, 
                  OVAL_WIDTH, OVAL_WIDTH);
          }
          g2d.setColor(Color.blue);
          for (Point3f p : forelimb.filteredFingertips) {
            g2d.drawOval((int)p.x - OVAL_WIDTH / 2, 
                (int)p.y - OVAL_WIDTH / 2, 
                OVAL_WIDTH, OVAL_WIDTH);
          }
        }
      }
    }
  }
  
  public String derivativeSaveDir = "data/derivative/";
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
  private BufferedImage bufferedImage;
  
  /**
   * Initializes the data structures.
   * @param width
   * @param height
   */
  public ProcessPacketController(int width, int height) {
    frames[0] = new CanvasFrame("Processed");
    frames[1] = new CanvasFrame("Depth");
    for (CanvasFrame frame : frames)
      frame.setCanvasSize(width, height);

    fpsCounter = new FPSCounter("Processed", frames[0]);

    analysisImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    appImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    
    CanvasFrame.tile(frames);
    rgbFrame = new ImageFrame("RGB", 
        new RgbImageComponent(new Dimension(width, height)));
    Rectangle rect = frames[0].getBounds();
    rgbFrame.setLocation(0, rect.y + rect.height);
    
    histogram = new float[HandAnalyzer.MAX_DEPTH];
    
    addKeyListener(this);
    rgbFrame.addMouseListenerToImageComponent(this);
    
    bufferedImage = new BufferedImage(width, height, 
        BufferedImage.TYPE_USHORT_GRAY);
  }
  
  public void keyPressed(KeyEvent ke) {
    switch (ke.getKeyCode()) {
    case KeyEvent.VK_B:
      showBoundingBox = !showBoundingBox;
      break;
    case KeyEvent.VK_D:
      showConvexityDefects = !showConvexityDefects;
      break;
    case KeyEvent.VK_F:
      // Shows all the detected fingertips.
      System.out.println("Toggled show fingertip.");
      showFingertip = !showFingertip;
      break;
    case KeyEvent.VK_H:
      showHull = !showHull;
      break;
    case KeyEvent.VK_M:
      showMorphed = !showMorphed;
      break;
    case KeyEvent.VK_S:
      PrintWriter pw = null;
      try {
        pw = new PrintWriter(
            String.format(derivativeSaveDir + "%03d", packet.depthFrameID));
        CvUtil.saveImage(pw, packet.derivative);
        System.out.println("Saved image.");
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } finally {
        if (pw != null)
          pw.close();
      }
      break;
    default: 
      break;
    }
  }
  
  /**
   * Shows different visualizations of the ProcessPacket.
   * @param packet
   */
  public void show(ProcessPacket packet) {
    this.packet = packet;
    showAnalysisImage();
    showRGBImage();
    showAppImage();
  }
  
  /**
   * Draws a circle at (x, y).
   * @param x
   * @param y
   */
  public void drawCircle(int x, int y) {
    //cvCircle(img, center, radius, color, thickness, lineType, shift)
    cvCircle(appImage, new CvPoint(x, y), 4, CvScalar.WHITE, -1, 8, 0);
    frames[1].showImage(appImage);
  }
  
  /**
   * Releases memory.
   */
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

  @Override
  public void mouseClicked(MouseEvent me) {
    Point p = me.getPoint();
    IplImage image = packet.derivative;
    float value = image.getFloatBuffer().get(p.y * image.widthStep() / 4 + p.x);
    rgbFrame.setStatus("x = " + p.x + " y = " + p.y + " value = " + value);
  }

  /**
   * Displays the image for analysis.
   */
  private void showAnalysisImage() {
    if (showMorphed)
      cvCopy(packet.morphedImage, analysisImage);
    else
      cvCopy(packet.depthImage8U, analysisImage);
    
    for (ForelimbFeatures ff : packet.forelimbFeatures){
      if (showBoundingBox) {
        CvRect rect = ff.boundingBox;
        cvRectangle(analysisImage, 
            new CvPoint(rect.x(), rect.y()), 
            new CvPoint(rect.x() + rect.width(), rect.y() + rect.height()), 
            CvScalar.WHITE, 1, 8, 0);
      }
    
      if (showConvexityDefects) {
         CvUtil.drawConvexityDefects(ff.convexityDefects, analysisImage);
      }
      
      if (showHull) {
        CvUtil.drawHullCorners(ff.hull, ff.approxPoly, analysisImage);
      }
    }

    if (showFingertip)
      for (Forelimb forelimb : packet.forelimbs)
        for (ValConfiPair<Point3f> p : forelimb.fingertips) {
          if (p.confidence > 0.5)
            cvCircle(analysisImage, new CvPoint((int)p.value.x, (int)p.value.y), 
                4, CvScalar.WHITE, -1, 8, 0);
        }

    frames[0].showImage(analysisImage);
    fpsCounter.computeFPS();
  }
  
  /**
   * Displays the application image.
   * @param packet
   */
  private void showAppImage() {
    ImageConvertUtils.arrayToHistogram(packet.depthRawData, histogram);
    ByteBuffer ib = appImage.getByteBuffer();
    int widthStep = appImage.widthStep();
    for (int h = 0; h < packet.height; h++) 
      for (int w = 0; w < packet.width; w++) {
        int depth = packet.depthRawData[h * packet.width + w];
        ib.put(h * widthStep + w, (byte)(histogram[depth] * 255));  
      }
    // Draws labeled points.
    if (packet.labels != null) {
      for (Point p : packet.labels)
        cvCircle(appImage, new CvPoint(p.x, p.y), 3, CvScalar.BLACK, 1, 8, 0);
    }
    frames[1].showImage(appImage);
    frames[1].setTitle("Processed FrameID = " + packet.depthFrameID);
  }
  
  private void showRGBImage() {
    ImageConvertUtils.floatBuffer2UShortGrayBufferedImage( 
        packet.derivative.getFloatBuffer(), bufferedImage, 
        packet.derivative.widthStep() / 4);
    rgbFrame.updateImage(bufferedImage);
  }

  @Override
  public void mouseEntered(MouseEvent arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void mouseExited(MouseEvent arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void mousePressed(MouseEvent arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void mouseReleased(MouseEvent arg0) {
    // TODO Auto-generated method stub
    
  }
}    