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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.vecmath.Point3f;

import org.OpenNI.GeneralException;
import org.OpenNI.Point3D;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.mit.yingyin.gui.ImageComponent;
import edu.mit.yingyin.gui.ImageFrame;
import edu.mit.yingyin.image.ImageConvertUtils;
import edu.mit.yingyin.tabletop.controllers.ViewImageValueController;
import edu.mit.yingyin.tabletop.models.EnvConstant;
import edu.mit.yingyin.tabletop.models.Forelimb;
import edu.mit.yingyin.tabletop.models.Forelimb.ValConfiPair;
import edu.mit.yingyin.tabletop.models.HistogramImageComponent;
import edu.mit.yingyin.tabletop.models.InteractionSurface;
import edu.mit.yingyin.tabletop.models.ProcessPacket;
import edu.mit.yingyin.tabletop.models.ProcessPacket.ForelimbFeatures;
import edu.mit.yingyin.util.CvUtil;

public class ProcessPacketView {
  /**
   * Image component for visualizing forelimbs.
   * 
   * @author yingyin
   * 
   */
  private class ForelimbView extends ImageComponent {
    private static final long serialVersionUID = 3880292315260748112L;
    private static final int OVAL_WIDTH = 6;

    private List<Point3f> fingertips;
    private List<ValConfiPair<Point3f>> unfilteredFingertips;
    private List<Point> labels;

    public ForelimbView(Dimension d) {
      super(d);
    }

    public void setFingertips(List<Forelimb> forelimbs, 
        List<ForelimbFeatures> ffs, List<Point> labels) {
      fingertips = new ArrayList<Point3f>();
      unfilteredFingertips = new ArrayList<ValConfiPair<Point3f>>();
      for (Forelimb fl : forelimbs) {
        fingertips.addAll(fl.getFingertipsI());
      }
      for (ForelimbFeatures ff : ffs) {
        unfilteredFingertips.addAll(ff.fingertips);
      }
      this.labels = labels;
    }

    @Override
    public void paint(Graphics g) {
      super.paint(g);

      Graphics2D g2d = (Graphics2D) g;
      g2d.setColor(Color.green);

      // Draws labeled points.
      if (labels != null) {
        for (Point p : labels)
          g2d.drawOval(p.x - OVAL_WIDTH / 2, p.y - OVAL_WIDTH / 2, OVAL_WIDTH,
              OVAL_WIDTH);
      }
      
      if (unfilteredFingertips != null) {
          // Draws measured points.
          g2d.setColor(Color.red);
          for (ValConfiPair<Point3f> vcp : unfilteredFingertips) {
            Point3f p = vcp.value;
            g2d.drawOval((int) p.x - OVAL_WIDTH / 2,
                (int) p.y - OVAL_WIDTH / 2, OVAL_WIDTH, OVAL_WIDTH);
          }
      }
      
      if (fingertips != null) {
        g2d.setColor(Color.blue);
        for (Point3f p : fingertips) {
          g2d.drawOval((int) p.x - OVAL_WIDTH / 2,
              (int) p.y - OVAL_WIDTH / 2, OVAL_WIDTH, OVAL_WIDTH);
        }
      }
    }
  }

  /**
   * Toggles for viewing different diagnostic frames.
   */
  public enum Toggles {
    SHOW_RGB_IMAGE, SHOW_DEPTH_IMAGE, SHOW_DIAGNOSTIC_IMAGE,
    SHOW_CONVEXITY_DEFECTS, SHOW_HULL, SHOW_MORPHED, SHOW_FINGERTIP,
    SHOW_BOUNDING_BOX, SHOW_LABELS
  }

  private static final Logger LOGGER =
      Logger.getLogger(ProcessPacketView.class.getName());

  private static final String DIAGNOSTIC_FRAME = "Diagnostic";
  private static final String ANALYSIS_FRAME = "Analysis";
  private static final String DEPTH_FRAME = "Depth";
  private static final String RGB_FRAME = "RGB";
  private static final String TABLE3D_FRAME = "Table3D";
  private static final String DEBUG_FRAME = "Debug";

  private static final boolean DEFAULT_SHOW_DIAGNOSTIC_IMAGE = false;

  private final HashMap<Toggles, Boolean> toggleMap =
      new HashMap<ProcessPacketView.Toggles, Boolean>();

  /**
   * Frames.
   */
  private final HashMap<String, JFrame> frames = 
      new LinkedHashMap<String, JFrame>();
  
  private final IplImage analysisImage;
  private final HistogramImageComponent depthImageComp, debugImageComp;
  private final ViewImageValueController depthImageController, 
                                         debugImageController;
  private final int width, height;
  private final int[] debugImage;
  private BufferedImage bufferedImage;
  private ForelimbView fingertipView;
  
  public ProcessPacketView(int width, int height) {
    initToggles();

    this.width = width;
    this.height = height;
    analysisImage = IplImage.create(width, height, IPL_DEPTH_8U, 3);
    debugImage = new int[width * height];
    
    depthImageComp = new HistogramImageComponent(width, height, 
                                                 EnvConstant.MAX_DEPTH);
    debugImageComp = new HistogramImageComponent(width, height, 
                                                 EnvConstant.MAX_DEPTH);
    CanvasFrame cf = new CanvasFrame(ANALYSIS_FRAME);
    cf.setPreferredSize(new Dimension(width, height));
    
    depthImageController = new ViewImageValueController(DEPTH_FRAME, 
                                                        depthImageComp);
    debugImageController = new ViewImageValueController(DEBUG_FRAME, 
                                                        debugImageComp);
    frames.put(ANALYSIS_FRAME, cf);
    frames.put(DEPTH_FRAME, depthImageController.frame());
    frames.put(TABLE3D_FRAME, new Table3DFrame(width, height));
    frames.put(DEBUG_FRAME, debugImageController.frame());
    tile();
  }
  
  public void redrawIntersections(Point3D[] intersections) {
    ((Table3DFrame) frames.get(TABLE3D_FRAME)).redrawIntersections(
        intersections);
  }

  /**
   * Show the visualization of the packet.
   * 
   * @param packet
   * @param fingertipLabels
   * @throws GeneralException
   */
  public void show(ProcessPacket packet, List<Point> fingertipLabels)
      throws GeneralException {
    showAnalysisImage(packet);

    if (toggleMap.get(Toggles.SHOW_DEPTH_IMAGE))
      showDepthImage(packet, fingertipLabels);
    
    if (toggleMap.get(Toggles.SHOW_DIAGNOSTIC_IMAGE))
      showDiagnosticImage(packet, fingertipLabels);

    if (toggleMap.get(Toggles.SHOW_RGB_IMAGE))
      showRgbImage(packet);

    showTable3DFrame(packet);
    showDebugImage(packet);
  }

  public Rectangle getBounds() {
    Rectangle rect = frames.get(ANALYSIS_FRAME).getBounds();
    return new Rectangle(0, 0, rect.width * 2, rect.height * 2);
  }

  public void addKeyListener(KeyListener kl) {
    for (JFrame frame : frames.values())
      frame.addKeyListener(kl);
  }

  public void addMouseListener(MouseListener ml) {
    ImageFrame f = (ImageFrame) frames.get(DIAGNOSTIC_FRAME);
    if (f != null)
      f.addMouseListenerToImageComponent(ml);
  }

  public void tile() {
    int x = 0, y = 0;
    int counter = 0;
    for (JFrame f : frames.values()) {
      f.setLocation(x, y);
      f.pack();
      f.setVisible(true);
      counter ++;
      Dimension d = f.getPreferredSize();
      x += d.width;
      if (counter % 2 == 0) {
        x = 0;
        y += d.height;
      }
    }
  }
  
  public JFrame analysisFrame() {
    return frames.get(ANALYSIS_FRAME);
  }

  public void release() {
    analysisImage.release();
    LOGGER.info("ProcessPacketView released.");
  }

  public void hide() {
    for (JFrame frame : frames.values())
      frame.setVisible(false);
  }

  public boolean isVisible() {
    boolean isVisible = true;
    for (JFrame frame : frames.values())
      isVisible = isVisible && frame.isVisible();
    return isVisible;
  }

  public void setStatus(String status) {
    ImageFrame f = (ImageFrame) frames.get(DIAGNOSTIC_FRAME);
    if (f != null)
      f.setStatus(status);
  }

  public void toggle(Toggles name) {
    toggleMap.put(name, !toggleMap.get(name));
  }

  public void setToggle(Toggles name, boolean status) {
    toggleMap.put(name, status);
  }

  public BufferedImage depthImage() { return depthImageComp.image(); }
  
  /**
   * Draws a circle at (x, y).
   * 
   * @param x
   * @param y
   */
  public void drawCircle(int x, int y) {
    depthImageComp.addLabel(new Point(x, y), Color.RED);
  }

  private void initToggles() {
    toggleMap.put(Toggles.SHOW_RGB_IMAGE, false);
    toggleMap.put(Toggles.SHOW_DEPTH_IMAGE, true);
    toggleMap.put(Toggles.SHOW_DIAGNOSTIC_IMAGE, DEFAULT_SHOW_DIAGNOSTIC_IMAGE);
    toggleMap.put(Toggles.SHOW_CONVEXITY_DEFECTS, false);
    toggleMap.put(Toggles.SHOW_HULL, false);
    toggleMap.put(Toggles.SHOW_MORPHED, true);
    toggleMap.put(Toggles.SHOW_FINGERTIP, true);
    toggleMap.put(Toggles.SHOW_BOUNDING_BOX, true);
    toggleMap.put(Toggles.SHOW_LABELS, false);
  }
  
  private void showTable3DFrame(ProcessPacket packet) {
    Table3DFrame f = (Table3DFrame) frames.get(TABLE3D_FRAME);
    if (!f.talbeInitialized() && InteractionSurface.instanceInitialized()) {
      f.initTable(InteractionSurface.instance());
    }
    
    if (f.talbeInitialized())
      f.redraw(packet);
  }

  /**
   * Shows the analysis image that displays intermediate processing steps by the
   * <code>HandAnalyzer</code>.
   */
  private void showAnalysisImage(ProcessPacket packet) {
    if (toggleMap.get(Toggles.SHOW_MORPHED))
      cvCvtColor(packet.morphedImage, analysisImage, CV_GRAY2BGR);
    else
      cvCvtColor(packet.depthImage8U, analysisImage, CV_GRAY2BGR);

    for (ForelimbFeatures ff : packet.forelimbFeatures) {
      if (toggleMap.get(Toggles.SHOW_BOUNDING_BOX)) {
        CvRect rect = ff.boundingBox;
        cvRectangle(analysisImage, new CvPoint(rect.x(), rect.y()),
                    new CvPoint(rect.x() + rect.width() - 1, 
                                rect.y() + rect.height() - 1),
                    CvScalar.WHITE, 1, 8, 0);
        rect = ff.armJointRegion;
        if (rect != null) {
          cvRectangle(analysisImage, new CvPoint(rect.x(), rect.y()),
                      new CvPoint(rect.x() + rect.width() - 1, 
                          rect.y() + rect.height() - 1),
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

      // Shows unfiltered finger tips.
      if (toggleMap.get(Toggles.SHOW_FINGERTIP)) {
        for (ValConfiPair<Point3f> vcp : ff.fingertips) {
          Point3f p = vcp.value;
          cvCircle(analysisImage, new CvPoint((int) p.x, (int) p.y), 4,
              CvScalar.GREEN, -1, 8, 0);
        }
      }
    }
    ((CanvasFrame)frames.get(ANALYSIS_FRAME)).showImage(analysisImage);
  }

  /**
   * Shows the histogram based depth image.
   * 
   * @param packet
   */
  private void showDepthImage(ProcessPacket packet, List<Point> labels) {
    depthImageComp.setImage(packet.depthRawData);
    depthImageController.update();
    // Draws labeled points.
    if (labels != null) {
      for (Point p : labels)
        depthImageComp.addLabel(p, Color.GREEN);
    }
    JFrame frame = frames.get(DEPTH_FRAME);
    frame.setTitle("Processed FrameID = " + packet.depthFrameID);
  }

  /**
   * Shows the image with diagnostic and debugging information.
   * 
   * @param packet
   */
  private void showDiagnosticImage(ProcessPacket packet, 
      List<Point> fingertipLabels) {
    ImageFrame f = (ImageFrame) frames.get(DIAGNOSTIC_FRAME);
    if (f == null) {
      fingertipView = new ForelimbView(new Dimension(width, height));
      f = new ImageFrame(DIAGNOSTIC_FRAME, fingertipView);
      frames.put(DIAGNOSTIC_FRAME, f);
      tile();
      f.showUI();
      bufferedImage =
          new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY);
    }
    fingertipView.setFingertips(packet.forelimbs, packet.forelimbFeatures,
        fingertipLabels);

    ImageConvertUtils.floatBufferToGrayBufferedImage(
        packet.depthImage32F.getFloatBuffer(), bufferedImage);
    f.updateImage(bufferedImage);
  }

  /**
   * Shows the RGB image from the camera.
   * 
   * @param packet
   * @throws GeneralException
   */
  private void showRgbImage(ProcessPacket packet) throws GeneralException {
    ImageFrame f = (ImageFrame) frames.get(RGB_FRAME);
    if (f == null) {
      f = new ImageFrame(RGB_FRAME, new Dimension(width, height));
      frames.put(RGB_FRAME, f);
      tile();
      f.showUI();
    }
    f.updateImage(packet.rgbImage());
  }
  
  private void showDebugImage(ProcessPacket packet) {
    Arrays.fill(debugImage, 0);
    ByteBuffer maskBuffer = packet.foregroundMask.getByteBuffer();
    int maskWidthStep = packet.foregroundMask.widthStep();
    for (int h = 0; h < height; h++)
      for (int w = 0; w < width; w++) {
        int pos = h * width + w;
        if ((maskBuffer.get(h * maskWidthStep + w) & 0xff) == 255) {
          debugImage[pos] = packet.depthRawData[pos];
        }
      }
    debugImageComp.setImage(debugImage);
    debugImageController.update();
  }
}
