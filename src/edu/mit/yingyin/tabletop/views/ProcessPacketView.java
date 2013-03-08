package edu.mit.yingyin.tabletop.views;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCircle;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_GRAY2BGR;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
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

import edu.mit.yingyin.tabletop.controllers.ViewImageValueController;
import edu.mit.yingyin.tabletop.models.EnvConstant;
import edu.mit.yingyin.tabletop.models.HistogramImageComponent;
import edu.mit.yingyin.tabletop.models.InteractionSurface;
import edu.mit.yingyin.tabletop.models.ProcessPacket;
import edu.mit.yingyin.tabletop.models.ProcessPacket.ForelimbFeatures;
import edu.mit.yingyin.util.CvUtil;
import edu.mit.yingyin.util.ValConfidencePair;

public class ProcessPacketView {
  /**
   * View for debugging purpose.
   * @author yingyin
   *
   */
  public interface DebugView {
    public JFrame frame();
    public void showDebugImage(ProcessPacket pacekt);
    public void showStatusMessage(String message);
  }
  
  public class BackgroundDebugView implements DebugView {
    public static final String NAME = "Background debug";
    
    private final HistogramImageComponent imageComp;
    private final ViewImageValueController imageController;
    
    public BackgroundDebugView(int width, int height) {
      imageComp = new HistogramImageComponent(width, height, 
                                              EnvConstant.MAX_DEPTH);
      imageController = new ViewImageValueController(NAME, imageComp);
    }
    
    @Override
    public JFrame frame() { return imageController.frame(); }
    
    @Override
    public void showDebugImage(ProcessPacket packet) {
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
      imageComp.setImage(debugImage);
      imageController.update();
    }

    @Override
    public void showStatusMessage(String message) {
      imageController.frame().setStatus(message);
    }  
  }
  
  /**
   * Shows the histogram based depth image.
   * 
   * @param packet
   */
  public class DepthDebugView implements DebugView {
    public static final String NAME = "Depth debug";
    
    private final HistogramImageComponent imageComp;
    private final ViewImageValueController imageController;
    
    public DepthDebugView (int width, int height) {
      imageComp = new HistogramImageComponent(width, height, 
                                                EnvConstant.MAX_DEPTH);
      imageController = new ViewImageValueController(NAME, imageComp);
    }
    
    @Override
    public JFrame frame() { return imageController.frame(); }
    
    @Override
    public void showDebugImage(ProcessPacket packet) {
      imageComp.setImage(packet.depthRawData);
      imageController.update();
      // Draws labeled points.
      if (fingertipLabels != null) {
        for (Point p : fingertipLabels)
          imageComp.addLabel(p, Color.GREEN);
      }
      JFrame frame = frames.get(DEPTH_FRAME_TITLE);
      frame.setTitle("Processed FrameID = " + packet.depthFrameID);
    }
    
    public BufferedImage image() { return imageComp.image(); }
  
    public void drawCircle(int x, int y) {
      imageComp.addLabel(new Point(x, y), Color.RED);
    }

    @Override
    public void showStatusMessage(String message) {
      imageController.frame().setStatus(message);
    }
  }
  
  /**
   * Toggles for viewing different diagnostic frames.
   */
  public enum Toggles {
    SHOW_DEPTH_VIEW, SHOW_CONVEXITY_DEFECTS, SHOW_HULL, 
    SHOW_MORPHED, SHOW_FINGERTIP, SHOW_BOUNDING_BOX, SHOW_LABELS, SHOW_3D
  }

  private static final Logger LOGGER =
      Logger.getLogger(ProcessPacketView.class.getName());

  public static final String ANALYSIS_FRAME_TITLE = "Analysis";
  public static final String DEPTH_FRAME_TITLE = "Depth";
  public static final String TABLE3D_FRAME_TITLE = "Table3D";
  public static final String DEBUG_FRAME_TITLE = "Debug";

  private final HashMap<Toggles, Boolean> toggleMap =
      new HashMap<ProcessPacketView.Toggles, Boolean>();

  /**
   * Frames.
   */
  private final HashMap<String, JFrame> frames = 
      new LinkedHashMap<String, JFrame>();
  
  private final IplImage analysisImage;
  private final DebugView debugView;
  private DepthDebugView depthView;
  private Table3DFrame table3DView;
  private final int width, height;
  private final int[] debugImage;
  private List<Point> fingertipLabels;
  
  public ProcessPacketView(int width, int height) {
    initToggles();

    this.width = width;
    this.height = height;
    analysisImage = IplImage.create(width, height, IPL_DEPTH_8U, 3);
    debugImage = new int[width * height];
    
    CanvasFrame cf = new CanvasFrame(ANALYSIS_FRAME_TITLE);
    cf.setPreferredSize(new Dimension(width, height));
    frames.put(ANALYSIS_FRAME_TITLE, cf);

    debugView = new HandPoseDebugView();
    frames.put(DEBUG_FRAME_TITLE, debugView.frame());
    
    if (toggleMap.get(Toggles.SHOW_DEPTH_VIEW)) {
      depthView = new DepthDebugView(width, height);
      frames.put(DEPTH_FRAME_TITLE, depthView.frame());
    }
    
    if (toggleMap.get(Toggles.SHOW_3D)) {
      table3DView = new Table3DFrame(width, height);
      frames.put(TABLE3D_FRAME_TITLE, table3DView);
    }

    tile();
  }
  
  public void redrawIntersections(Point3D[] intersections) {
    if (table3DView != null)
      table3DView.redrawIntersections(intersections);
  }

  /**
   * Show the visualization of the packet.
   * 
   * @param packet
   * @param fingertipLabels can be null.
   * @throws GeneralException
   */
  public void show(ProcessPacket packet, List<Point> fingertipLabels, 
      int classLabel) throws GeneralException {
    this.fingertipLabels = fingertipLabels;

    showAnalysisImage(packet);
    debugView.showDebugImage(packet);
    debugView.showStatusMessage("hand pose class: " + classLabel);

    if (toggleMap.get(Toggles.SHOW_DEPTH_VIEW))
      depthView.showDebugImage(packet);
    
    if (toggleMap.get(Toggles.SHOW_3D))
      showTable3DFrame(packet);
    
  }

  public Rectangle getBounds() {
    Rectangle rect = frames.get(ANALYSIS_FRAME_TITLE).getBounds();
    return new Rectangle(0, 0, rect.width * 2, rect.height * 2);
  }

  public void addKeyListener(KeyListener kl) {
    for (JFrame frame : frames.values())
      frame.addKeyListener(kl);
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
    return frames.get(ANALYSIS_FRAME_TITLE);
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

  public void toggle(Toggles name) {
    toggleMap.put(name, !toggleMap.get(name));
  }

  public void setToggle(Toggles name, boolean status) {
    toggleMap.put(name, status);
  }

  public BufferedImage depthImage() { return depthView.image(); }
  
  /**
   * Draws a circle at (x, y).
   * 
   * @param x
   * @param y
   */
  public void drawCircle(int x, int y) {
    if (depthView != null)
      depthView.drawCircle(x, y);
  }

  private void initToggles() {
    toggleMap.put(Toggles.SHOW_DEPTH_VIEW, true);
    toggleMap.put(Toggles.SHOW_3D, true);
    toggleMap.put(Toggles.SHOW_CONVEXITY_DEFECTS, false);
    toggleMap.put(Toggles.SHOW_HULL, false);
    toggleMap.put(Toggles.SHOW_MORPHED, true);
    toggleMap.put(Toggles.SHOW_FINGERTIP, true);
    toggleMap.put(Toggles.SHOW_BOUNDING_BOX, true);
    toggleMap.put(Toggles.SHOW_LABELS, false);
  }
  
  private void showTable3DFrame(ProcessPacket packet) {
    if (table3DView != null) {
      if (!table3DView.talbeInitialized() && 
          InteractionSurface.instanceInitialized()) {
        table3DView.initTable(InteractionSurface.instance());
      }
      
      if (table3DView.talbeInitialized())
        table3DView.redraw(packet);
    }
  }

  /**
   * Shows the analysis image that displays intermediate processing steps by the
   * {@code ForelimbFeatureDetector}.
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
        rect = ff.handRegion;
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
        for (ValConfidencePair<Point3f> vcp : ff.fingertips) {
          Point3f p = vcp.value;
          cvCircle(analysisImage, new CvPoint((int) p.x, (int) p.y), 4,
              CvScalar.GREEN, -1, 8, 0);
        }
      }
    }
    ((CanvasFrame)frames.get(ANALYSIS_FRAME_TITLE)).showImage(analysisImage);
  }
}
