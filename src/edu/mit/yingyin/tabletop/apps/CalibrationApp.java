package edu.mit.yingyin.tabletop.apps;

import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.vecmath.Point2f;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import edu.mit.yingyin.calib.CalibLabelController;
import edu.mit.yingyin.calib.CalibLabelModel;
import edu.mit.yingyin.calib.CalibModel;
import edu.mit.yingyin.calib.CalibModel.CalibMethodName;
import edu.mit.yingyin.calib.OpenniViewThread;
import edu.mit.yingyin.image.ImageConvertUtils;
import edu.mit.yingyin.util.CommandLineOptions;
import edu.mit.yingyin.util.FileUtil;

/**
 * Application for labeling the calibration images and computing the extrinsic
 * camera parameters.
 *
 * After labeling, calibration is run once the user hits "Q" or Escape.
 * @author yingyin
 *
 */
public class CalibrationApp extends KeyAdapter {
  private static enum LabelState {
    Screen, Image, ScreenTest, ImageTest;
    
    public boolean isScrnCoord() {
      switch (this) {
        case Screen:
        case ScreenTest:
          return true;
        case Image:
        case ImageTest:
          return false;
        default:
          return false;
      }
    }
  };
  
  private static final Logger LOGGER = Logger.getLogger(
      CalibrationApp.class.getName());

  private static final int WIDTH = 640, HEIGHT = 480;
  
  private static final String DEFAULT_CALIB_DIR = FileUtil.join("data", 
                                                                "calibration");
  private static final String CONFIG_DIR = "config";
  private static final String APP_PROPS = FileUtil.join(CONFIG_DIR, 
      "calibration.properties"); 
  private static final String DEFAULT_OPENNI_CONFIG_FILE = FileUtil.join(
      CONFIG_DIR, "config.xml");
  
  public static void main(String args[]) {
    @SuppressWarnings("static-access")
    Option mainDirOpt = OptionBuilder.withArgName("main directory").withLongOpt(
        "dir").hasArg().withDescription(
        "The main directory for input and output. "
            + "The configuration file should be in <dir>/config/folder."
            + "The default dir is the current directory.").create("d");
    CommandLineOptions.addOption(mainDirOpt);
    CommandLineOptions.parse(args);
    String mainDir = CommandLineOptions.getOptionValue("d", ".");
    new CalibrationApp(mainDir);
  }

  private final String calibMethodStr;
  private final String fullSavePath;
  private final OpenniViewThread openniViewThread;
  private final String fullCalibDir;
  private final Iterator<Entry<LabelState, String>> stateIterator;
  private List<Point2f> screenPts, imagePts, screenTestPts, imageTestPts;
  private CalibLabelModel screenLabelModel, imageLabelModel;
  private CalibMethodName calibMethod = CalibMethodName.UNDISTORT;

  public CalibrationApp(String mainDir) {
    Properties config = new Properties();
    FileInputStream in = null;

    try {
      in = new FileInputStream(FileUtil.join(mainDir, APP_PROPS));
      config.load(in);
      in.close();
    } catch (FileNotFoundException fnfe) {
      LOGGER.severe(fnfe.getMessage());
      System.exit(-1);
    } catch (IOException e) {
      LOGGER.severe(e.getMessage());
      System.exit(-1);
    }
    
    String openniConfigFile = FileUtil.join(mainDir,
        config.getProperty("openni-config", DEFAULT_OPENNI_CONFIG_FILE));
    openniViewThread = new OpenniViewThread(openniConfigFile);
    openniViewThread.addKeyListener(this);
    openniViewThread.start();

    String calibDir = config.getProperty("calibration-dir", DEFAULT_CALIB_DIR);
    fullCalibDir = FileUtil.join(mainDir, calibDir);
    
    String depthImagePath = config.getProperty("depth-image", null);
    depthImagePath = depthImagePath == null ? null : 
                     FileUtil.join(fullCalibDir, depthImagePath);
    
    String depthImageTestPath = config.getProperty("depth-image-test", null);
    depthImageTestPath = depthImageTestPath == null ? 
        null : FileUtil.join(fullCalibDir, depthImageTestPath);
    
    String scrnImagePath = config.getProperty("screen-image", null);
    scrnImagePath = scrnImagePath == null ? 
        null : FileUtil.join(fullCalibDir, scrnImagePath); 
    
    String screenPtsPath = config.getProperty("screen-pts", null);
    screenPtsPath = screenPtsPath == null ? 
        null : FileUtil.join(fullCalibDir, screenPtsPath);
    
    String imagePtsPath = config.getProperty("image-pts", null);
    imagePtsPath = imagePtsPath == null ? 
        null : FileUtil.join(fullCalibDir, imagePtsPath);
    
    String imageTestPtsPath = config.getProperty("image-test-pts", null);
    imageTestPtsPath = imageTestPtsPath == null ? 
        null : FileUtil.join(fullCalibDir, imageTestPtsPath);
    
    String screenTestPtsPath = config.getProperty("screen-test-pts", null);
    screenTestPtsPath = screenTestPtsPath == null ? 
        null : FileUtil.join(fullCalibDir, screenTestPtsPath);
    
    String savePath = config.getProperty("save-path", null);
    fullSavePath = savePath == null ? 
        null : FileUtil.join(fullCalibDir, savePath);
    
    calibMethodStr = config.getProperty("calib-method", "UNDISTORT");
    
    if (screenPtsPath != null)
      screenPts = readPointsFromFile(screenPtsPath);
    
    if (imagePtsPath != null)
      imagePts = readPointsFromFile(imagePtsPath);
    
    if (screenTestPtsPath != null)
      screenTestPts = readPointsFromFile(screenTestPtsPath);
    
    if (imageTestPtsPath != null)
      imageTestPts = readPointsFromFile(imageTestPtsPath);
    
    try {
      calibMethod = CalibMethodName.valueOf(calibMethodStr);
    } catch (IllegalArgumentException e) {
      LOGGER.severe(e.getMessage());
      System.exit(-1);
    }
    
    final Map<LabelState, String> calibStates = 
        new LinkedHashMap<LabelState, String>();
    calibStates.put(LabelState.Screen, scrnImagePath);
    calibStates.put(LabelState.Image, depthImagePath);
    calibStates.put(LabelState.ImageTest, depthImageTestPath);

    stateIterator = calibStates.entrySet().iterator();
    Entry<LabelState, String> entry = stateIterator.next();
    while (entry.getValue() == null && stateIterator.hasNext())
      entry = stateIterator.next();
    
    if (entry.getValue() != null)
      labelImage(entry);
    else 
      calibrate();
  }

  public void keyPressed(KeyEvent ke) {
    switch (ke.getKeyCode()) {
      case KeyEvent.VK_1:
        updatePoints(LabelState.Screen, screenLabelModel);
        break;
      case KeyEvent.VK_2:
        updatePoints(LabelState.Image, imageLabelModel);
        break;
      case KeyEvent.VK_3:
        updatePoints(LabelState.ScreenTest, screenLabelModel);
        break;
      case KeyEvent.VK_4:
        updatePoints(LabelState.ImageTest, imageLabelModel);
        break;
      case KeyEvent.VK_A:
        calibrate();
        break;
      case KeyEvent.VK_P:
        BufferedImage bi = openniViewThread.capture();
        imageLabelModel = createLabelModel(bi, "cam.pts", false);
        break;
      case KeyEvent.VK_Q:
      case KeyEvent.VK_ESCAPE:
        openniViewThread.stopRunning();
        try {
          openniViewThread.join();
        } catch (InterruptedException e) {
          LOGGER.severe(e.getMessage());
        }
        System.exit(0);
        break;
      default: break;
    }
  }
  
  private void labelImage(Entry<LabelState, String> entry) {
    String imagePath = entry.getValue();
    if (imagePath == null) 
      return;

    BufferedImage image = null;
    String ptsFileName = null;
    try {
      if (imagePath.endsWith(".raw")) {
        image = ImageConvertUtils.readRawDepth(imagePath, WIDTH, HEIGHT);
        ImageIO.write(image, "PNG", 
            new File(FileUtil.setExtension(imagePath, "png")));
      } else if (imagePath.endsWith("png")) {
        image = ImageIO.read(new File(imagePath));
      }
    } catch (IOException e) {
      LOGGER.severe(e.getMessage());
      System.exit(-1);
    }
    ptsFileName = FileUtil.setExtension(imagePath, "pts");
    
    boolean isScrnCoord = entry.getKey().isScrnCoord();
    CalibLabelModel model = createLabelModel(image, ptsFileName, isScrnCoord);
    if (isScrnCoord)
      screenLabelModel = model;
    else
      imageLabelModel = model;
  }
  
  private CalibLabelModel createLabelModel(BufferedImage bi, String ptsFileName, 
                                           boolean isScrnCoord) {
    CalibLabelModel calibLabelModel = new CalibLabelModel(bi, ptsFileName, 
                                                          isScrnCoord);
    CalibLabelController calibLabelController = 
        new CalibLabelController(calibLabelModel);
    calibLabelController.addKeyListener(this);
    calibLabelController.showUI();
    return calibLabelModel;
  }
  
  private List<Point2f> readPointsFromFile(String file) {
    Scanner scanner = null;
    List<Point2f> points = new ArrayList<Point2f>();
    try {
      scanner = new Scanner(new File(file));
      while (scanner.hasNext()) 
        points.add(new Point2f(scanner.nextInt(), scanner.nextInt()));
    } catch (FileNotFoundException e) {
      LOGGER.severe("CalibrationApp:" + e.getMessage());
      System.exit(-1);
    } finally {
      scanner.close();
    }
    return points;
  }
    
  private void updatePoints(LabelState labelState, 
                            CalibLabelModel calibLabelModel) {
    if (calibLabelModel == null) 
      return;
    
    List<Point2f> points = new ArrayList<Point2f>(
        calibLabelModel.getImagePoints().size());
    for (Point p : calibLabelModel.getImagePoints())
      points.add(new Point2f(p.x, p.y));
    
    calibLabelModel.saveImagePoints(FileUtil.join(fullCalibDir, 
        labelState.toString() + ".pts"));
    
    switch (labelState) {
      case Screen:
        screenPts = points;
        LOGGER.info("Updated screen points");
        break;
      case ScreenTest:
        screenTestPts = points;
        LOGGER.info("Updated screen test points");
        break;
      case Image:
        imagePts = points;
        LOGGER.info("Updated image points");
        break;
      case ImageTest:
        imageTestPts = points;
        LOGGER.info("Updated image test points");
        break;
      default:
        break;
    }
  }
  
  /**
   * Calibrates the extrinsic parameters of the camera.
   */
  private void calibrate() {
    if (screenPts == null || screenPts.isEmpty()) {
      LOGGER.warning("No screen points.");
      return;
    }
    if (imagePts == null || imagePts.isEmpty()) {
      LOGGER.warning("No camera points.");
      return;
    }
   
    if (screenPts.size() != imagePts.size()) {
      LOGGER.warning("Dispaly image points and camera image points sizes " +
      		"are not the same. No calibraiton is done.");
      return;
    }
    
    LOGGER.info("Calibration method: " + calibMethodStr);
    CalibModel example = 
      new CalibModel(screenPts, imagePts, calibMethod);
    LOGGER.info(example.toString());
    LOGGER.info("Average reprojection errors in pixels:"); 
    example.logImageToDisplayCoordsErrors(screenPts, imagePts);
    
    if (screenTestPts != null && !screenTestPts.isEmpty() &&
        imageTestPts != null && !imageTestPts.isEmpty()) {
      LOGGER.info("Average test squared error:"); 
      example.logImageToDisplayCoordsErrors(screenTestPts, 
          imageTestPts);
    } else {
      LOGGER.warning("No test data.");
    }
    
    if (fullSavePath != null)
      example.save(fullSavePath);
    example.release();
  }
}
