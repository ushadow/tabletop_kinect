package edu.mit.yingyin.tabletop.views;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCircle;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_GRAY2BGR;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.vecmath.Point3f;

import org.OpenNI.GeneralException;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.mit.yingyin.gui.ImageComponent;
import edu.mit.yingyin.gui.ImageFrame;
import edu.mit.yingyin.image.ImageConvertUtils;
import edu.mit.yingyin.tabletop.models.Forelimb;
import edu.mit.yingyin.tabletop.models.ProcessPacket;
import edu.mit.yingyin.tabletop.models.ProcessPacket.ForelimbFeatures;
import edu.mit.yingyin.util.CvUtil;

public class ProcessPacketView {
  /**
   * Image component for visualizing forelimbs.
   * @author yingyin
   *
   */
  private class ForelimbView extends ImageComponent {
    private static final long serialVersionUID = 3880292315260748112L;
    private static final int OVAL_WIDTH = 6;
    
    private List<Forelimb> forelimbs;
    private List<Point> labels;
    
    public ForelimbView(Dimension d) {
      super(d);
    }
    
    public void setFingertips(List<Forelimb> forelimbs, List<Point> labels) {
      this.forelimbs = forelimbs;
      this.labels = labels;
    }
    
    @Override
    public void paint(Graphics g) {
      super.paint(g);
      
      if (forelimbs == null)
        return;
      
      Graphics2D g2d = (Graphics2D) g;
      g2d.setColor(Color.green);
      
      // Draws labeled points.
      if (labels != null) {
        for (Point p : labels)
          g2d.drawOval(p.x - OVAL_WIDTH / 2, p.y - OVAL_WIDTH / 2, OVAL_WIDTH,
              OVAL_WIDTH);
      }
      
      // Draws measured points.
      synchronized (forelimbs) {
        for (Forelimb forelimb : forelimbs){
          g2d.setColor(Color.red);
          for (Point3f p : forelimb.getFingertips()) {
            g2d.drawOval((int)p.x - OVAL_WIDTH / 2, (int)p.y - OVAL_WIDTH / 2, 
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

  /**
   * Toggles for viewing different diagnostic frames.
   */
  public enum Toggles {SHOW_RGB_IMAGE, SHOW_DEPTH_IMAGE, SHOW_DIAGNOSTIC_IMAGE, 
    SHOW_CONVEXITY_DEFECTS, SHOW_HULL, SHOW_MORPHED, SHOW_FINGERTIP, 
    SHOW_BOUNDING_BOX, SHOW_LABELS
  }

  private static final Logger logger = Logger.getLogger(ProcessPacketView.class.getName());
  
  private static final String DIAGNOSTIC_FRAME_TITLE = "Diagnostic";
  
  private HashMap<Toggles, Boolean> toggleMap = 
      new HashMap<ProcessPacketView.Toggles, Boolean>();
  private CanvasFrame[] frames = new CanvasFrame[2];
  private IplImage analysisImage;
  private IplImage appImage;
  private ImageFrame diagnosticFrame, rgbFrame;
  private BufferedImage bufferedImage;
  private ForelimbView fingertipView;
  private float[] histogram;
  private int width, height;
  
  public ProcessPacketView(int width, int height) {
    this.width = width;
    this.height = height;
    frames[0] = new CanvasFrame("Processed");
    frames[1] = new CanvasFrame("Depth");
    for (CanvasFrame frame : frames)
      frame.setCanvasSize(width, height);

    analysisImage = IplImage.create(width, height, IPL_DEPTH_8U, 3);
    appImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    
    fingertipView = new ForelimbView(new Dimension(width, height));
    diagnosticFrame = new ImageFrame(DIAGNOSTIC_FRAME_TITLE, fingertipView);
    Rectangle rect = frames[0].getBounds();
    diagnosticFrame.setLocation(0, rect.y + rect.height);
    bufferedImage = new BufferedImage(width, height, 
        BufferedImage.TYPE_USHORT_GRAY);
    
    initToggles();
  }

  public void update(ProcessPacket packet, List<Point> fingertipLabels) 
      throws GeneralException {
    if (histogram == null)
      histogram = new float[packet.maxDepth() + 1];
    
    fingertipView.setFingertips(packet.forelimbs, fingertipLabels);

    showAnalysisImage(packet);
    if (toggleMap.get(Toggles.SHOW_DIAGNOSTIC_IMAGE))
      showDiagnosticImage(packet);
    
    if (toggleMap.get(Toggles.SHOW_DEPTH_IMAGE))
      showDepthImage(packet, fingertipLabels);
    
    if (toggleMap.get(Toggles.SHOW_RGB_IMAGE))
      showRgbImage(packet);
    
    showHeatMap(packet);
  }
  
  public Rectangle getBunnds() {
    Rectangle rect = frames[0].getBounds();
    return new Rectangle(0, 0, rect.width * 2, rect.height * 2);
  }
  
  public void addKeyListener(KeyListener kl) {
    for (CanvasFrame frame : frames)
      frame.addKeyListener(kl);
    diagnosticFrame.addKeyListener(kl);
  }
  
  public void addMouseListener(MouseListener ml) {
    diagnosticFrame.addMouseListenerToImageComponent(ml);
  }
  
  public void showUI() {
    CanvasFrame.tile(frames);
    if (toggleMap.get(Toggles.SHOW_DIAGNOSTIC_IMAGE))
      diagnosticFrame.showUI();
  }
  
  public void showDepthImage(boolean show) {
    frames[1].setVisible(show);
  }
  
  public void showDiagnosticImage(boolean show) {
    diagnosticFrame.setVisible(show);
  }
  
  public CanvasFrame analysisFrame() {
    return frames[0];
  }
  
  public void release() {
    analysisImage.release();
    appImage.release();
    for (CanvasFrame frame : frames)
      frame.dispose();
    System.out.println("ProcessPacketView released.");
  }
  
  public void hide() {
    for (CanvasFrame frame: frames)
      frame.setVisible(false);
  }
  
  public boolean isVisible() {
    boolean isVisible = true;
    for (CanvasFrame frame : frames)
      isVisible = isVisible && frame.isVisible();
    return isVisible;
  }
  
  public void setStatus(String status) {
    diagnosticFrame.setStatus(status);
  }
  
  public void toggle(Toggles name) {
    toggleMap.put(name, !toggleMap.get(name));
  }
  
  public void setToggle(Toggles name, boolean status) {
    toggleMap.put(name, status);
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
  
  
  private void initToggles() {
    toggleMap.put(Toggles.SHOW_RGB_IMAGE, false);
    toggleMap.put(Toggles.SHOW_DEPTH_IMAGE, true);
    toggleMap.put(Toggles.SHOW_DIAGNOSTIC_IMAGE, true);
    toggleMap.put(Toggles.SHOW_CONVEXITY_DEFECTS, false);
    toggleMap.put(Toggles.SHOW_HULL, false);
    toggleMap.put(Toggles.SHOW_MORPHED, true);
    toggleMap.put(Toggles.SHOW_FINGERTIP, true);
    toggleMap.put(Toggles.SHOW_BOUNDING_BOX, true);
    toggleMap.put(Toggles.SHOW_LABELS, false);
  }
  
  /**
   * Shows the analysis image that displays intermediate processing steps by
   * the <code>HandAnalyzer</code>.
   */
  private void showAnalysisImage(ProcessPacket packet) {
    if (toggleMap.get(Toggles.SHOW_MORPHED))
      cvCvtColor(packet.morphedImage, analysisImage, CV_GRAY2BGR);
    else
      cvCvtColor(packet.depthImage8U, analysisImage, CV_GRAY2BGR);
    
    for (ForelimbFeatures ff : packet.forelimbFeatures){
      if (toggleMap.get(Toggles.SHOW_BOUNDING_BOX)) {
        CvRect rect = ff.boundingBox;
        cvRectangle(analysisImage, 
            new CvPoint(rect.x(), rect.y()), 
            new CvPoint(rect.x() + rect.width(), rect.y() + rect.height()), 
            CvScalar.WHITE, 1, 8, 0);
        rect = ff.armJointRegion;
        if (rect != null) {
          cvRectangle(analysisImage, 
              new CvPoint(rect.x(), rect.y()), 
              new CvPoint(rect.x() + rect.width(), rect.y() + rect.height()), 
              CvScalar.WHITE, 1, 8, 0);
        }
      }
    
      if (toggleMap.get(Toggles.SHOW_CONVEXITY_DEFECTS)) {
         CvUtil.drawConvexityDefects(ff.convexityDefects, analysisImage, 
             toggleMap.get(Toggles.SHOW_LABELS));
      }
      
      if (toggleMap.get(Toggles.SHOW_HULL)) {
        CvUtil.drawHull(ff.hull, ff.approxPoly, analysisImage);
      }
    }

    // Shows unfiltered fingertips.
    if (toggleMap.get(Toggles.SHOW_FINGERTIP)) {
      for (Forelimb forelimb : packet.forelimbs) {
        for (Point3f p : forelimb.getFingertips()) {
          cvCircle(analysisImage, new CvPoint((int)p.x, (int)p.y), 
              4, CvScalar.GREEN, -1, 8, 0);
        }
        Point3f armJoint = forelimb.armJointI();
        if (armJoint != null) {
          cvCircle(analysisImage, new CvPoint((int)armJoint.x, (int)armJoint.y), 4, CvScalar.CYAN, -1, 
                   8, 0);
        }
        
        Point3f armJointW = forelimb.armJointW();
        if (armJointW != null)
          logger.fine("arm joint world coordinate: " + armJointW);
      }
    }

    frames[0].showImage(analysisImage);
  }
  
  /**
   * Shows the histogram based depth image.
   * @param packet
   */
  private void showDepthImage(ProcessPacket packet, List<Point> labels) {
    ImageConvertUtils.arrayToHistogram(packet.depthRawData, histogram);
    ByteBuffer ib = appImage.getByteBuffer();
    int widthStep = appImage.widthStep();
    for (int h = 0; h < packet.height; h++) 
      for (int w = 0; w < packet.width; w++) {
        int depth = packet.depthRawData[h * packet.width + w];
        if (depth > packet.maxDepth())
          depth = packet.maxDepth();
        ib.put(h * widthStep + w, (byte)(histogram[depth] * 255));  
      }
    // Draws labeled points.
    if (labels != null) {
      for (Point p : labels)
        cvCircle(appImage, new CvPoint(p.x, p.y), 3, CvScalar.BLACK, 1, 8, 0);
    }
    frames[1].showImage(appImage);
    frames[1].setTitle("Processed FrameID = " + packet.depthFrameID);
  }
  
  /**
   * Shows the image with diagnostic and debugging information.
   * @param packet
   */
  private void showDiagnosticImage(ProcessPacket packet) {
    ImageConvertUtils.floatBufferToGrayBufferedImage(
        packet.depthImage32F.getFloatBuffer(),
        bufferedImage);
    diagnosticFrame.updateImage(bufferedImage);
  }

  /**
   * Shows the RGB image from the camera.
   * @param packet
   * @throws GeneralException
   */
  private void showRgbImage(ProcessPacket packet) throws GeneralException {
    if (rgbFrame == null) {
      rgbFrame = new ImageFrame("RGB", new Dimension(width, height));
      Rectangle bounds = diagnosticFrame.getBounds();
      rgbFrame.setLocation(bounds.x + bounds.width, bounds.y);
      rgbFrame.showUI();
    }
    rgbFrame.updateImage(packet.rgbImage());
  }
  
  private void showHeatMap(ProcessPacket packet) {
    double[][] data = new double[width][height];
    int[] depthRaw = packet.depthRawData;
    int min = Integer.MAX_VALUE;
    for (int h = 0; h < height; h++)
      for (int w = 0; w < width; w++) {
        int val = depthRaw[h * width + w];
        if (val != 0) {
          min = val < min ? val : min;
          data[w][h] = val;
        }
      }
    
    for (int h = 0; h < height; h++)
      for (int w = 0; w < width; w++) {
        if (depthRaw[h * width + w] == 0)
          data[w][h] = min;
      }
  }
}
