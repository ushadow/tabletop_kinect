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
import java.util.List;
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
  private static final Logger LOGGER = Logger.getLogger(
      CalibrationApp.class.getName());

  private static final int WIDTH = 640, HEIGHT = 480;
  
  private static final String DEFAULT_CALIB_DIR = FileUtil.join("data", 
                                                                "calibration");
  private static final String CONFIG_DIR = "config";
  private static final String APP_PROPS = FileUtil.join(CONFIG_DIR, 
      "calibration.properties"); 
  
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

  public void keyPressed(KeyEvent ke) {
    switch (ke.getKeyCode()) {
      case KeyEvent.VK_A:
        calibrate();
        break;
      case KeyEvent.VK_N:
        // Show the next image to label.
        if (calibLabelController != null)
          calibLabelController.dispose();
        updatePoints(isCurrentLabelImageScrnCoord , isCurrentLabelImageTest);
        if (!isCurrentLabelImageScrnCoord && !isCurrentLabelImageTest) {
          isCurrentLabelImageTest = true;
          labelImage(camTestImgPath, isCurrentLabelImageScrnCoord);
        }
        break;
      case KeyEvent.VK_Q:
      case KeyEvent.VK_ESCAPE:
        System.exit(0);
        break;
    default: break;
    }
  }

  private List<Point2f> screenPoints;
  private List<Point2f> cameraPoints;
  private List<Point2f> screenPointsTest;
  private List<Point2f> cameraPointsTest;
  private CalibLabelModel calibLabelModel;
  private CalibLabelController calibLabelController;
  private CalibMethodName calibMethod = CalibMethodName.UNDISTORT;
  private String calibMethodStr;
  private String savePath, camTestImgPath;
  private boolean isCurrentLabelImageTest = false;
  private boolean isCurrentLabelImageScrnCoord = true;

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
      e.printStackTrace();
      System.exit(-1);
    }

    String calibDir = config.getProperty("calibration-dir", DEFAULT_CALIB_DIR);
    calibDir = FileUtil.join(mainDir, calibDir);
    
    String camImgPath = config.getProperty("cam-depth-image", null);
    camImgPath = camImgPath == null ? null : 
                                      FileUtil.join(calibDir, camImgPath);
    
    camTestImgPath = config.getProperty("cam-depth-image-t", null);
    camTestImgPath = camTestImgPath == null ? 
        null : FileUtil.join(calibDir, camTestImgPath);
    
    String scrnImagePath = config.getProperty("screen-image", null);
    scrnImagePath = scrnImagePath == null ? 
        null : FileUtil.join(calibDir, scrnImagePath); 
    
    String screenPtsPath = config.getProperty("screen-points", null);
    screenPtsPath = screenPtsPath == null ? 
        null : FileUtil.join(calibDir, screenPtsPath);
    
    String camPtsPath = config.getProperty("cam-points", null);
    camPtsPath = camPtsPath == null ? 
        null : FileUtil.join(calibDir, camPtsPath);
    
    String camPtsTestPath = config.getProperty("cam-points-t", null);
    camPtsTestPath = camPtsTestPath == null ? 
        null : FileUtil.join(calibDir, camPtsTestPath);
    
    String screenPtsTestPath = config.getProperty("screen-points-t", null);
    screenPtsTestPath = screenPtsTestPath == null ? 
        null : FileUtil.join(calibDir, screenPtsTestPath);
    
    savePath = config.getProperty("save-path", null);
    savePath = savePath == null ? 
        null : FileUtil.join(calibDir, savePath);
    
    calibMethodStr = config.getProperty("calib-method", "UNDISTORT");
    
    if (screenPtsPath != null)
      screenPoints = readPointsFromFile(screenPtsPath);
    
    if (camPtsPath != null)
      cameraPoints = readPointsFromFile(camPtsPath);
    
    if (screenPtsTestPath != null)
      screenPointsTest = readPointsFromFile(screenPtsTestPath);
    
    if (camPtsTestPath != null)
      cameraPointsTest = readPointsFromFile(camPtsTestPath);
    
    try {
      calibMethod = CalibMethodName.valueOf(calibMethodStr);
    } catch (IllegalArgumentException e) {
      LOGGER.severe(e.getMessage());
      System.exit(-1);
    }
    
    String firstImage = null;
    
    if (camImgPath != null || camTestImgPath != null || scrnImagePath != null) {
      if (camImgPath != null) {
        firstImage = camImgPath;
        isCurrentLabelImageScrnCoord = false;
        isCurrentLabelImageTest = false;
      } else if (camTestImgPath != null) {
        firstImage = camTestImgPath;
        isCurrentLabelImageScrnCoord = false;
        isCurrentLabelImageTest = true;
      } else if (scrnImagePath != null) {
        firstImage = scrnImagePath;
        isCurrentLabelImageScrnCoord = true;
        isCurrentLabelImageTest = false;
      }
      labelImage(firstImage, isCurrentLabelImageScrnCoord);
    } else {
      calibrate();
    }
  }
  
  private void labelImage(String imagePath, boolean isScrnCoord) {
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
        LOGGER.info(imagePath);
        image = ImageIO.read(new File(imagePath));
      }
    } catch (IOException e) {
      LOGGER.severe(e.getMessage());
      System.exit(-1);
    }
    ptsFileName = FileUtil.setExtension(imagePath, "pts");
    calibLabelModel = new CalibLabelModel(image, ptsFileName, isScrnCoord);
    calibLabelController = new CalibLabelController(calibLabelModel);
    calibLabelController.addKeyListener(this);
    calibLabelController.showUI();
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
    
  private void updatePoints(boolean isScrnCoord, boolean isTest) {
    if (calibLabelModel == null) 
      return;
    
    List<Point2f> points = new ArrayList<Point2f>(
        calibLabelModel.getImagePoints().size());
    for (Point p : calibLabelModel.getImagePoints())
      points.add(new Point2f(p.x, p.y));
    
    if (isScrnCoord) {
      screenPoints = points;
      LOGGER.info("Updated screen points");
    } else if (isTest) {
      cameraPointsTest = points;
      LOGGER.info("Updated camera image test points");
    } else {
      cameraPoints = points;
      LOGGER.info("Updated camera image points");
    }
  }
  
  /**
   * Calibrates the extrinsic parameters of the camera.
   */
  private void calibrate() {
    if (screenPoints == null || screenPoints.isEmpty()) {
      LOGGER.warning("No screen points.");
      return;
    }
    if (cameraPoints == null || cameraPoints.isEmpty()) {
      LOGGER.warning("No camera points.");
      return;
    }
   
    if (screenPoints.size() != cameraPoints.size()) {
      LOGGER.warning("Dispaly image points and camera image points sizes " +
      		"are not the same. No calibraiton is done.");
      return;
    }
    
    LOGGER.info("Calibration method: " + calibMethodStr);
    CalibModel example = 
      new CalibModel(screenPoints, cameraPoints, calibMethod);
    System.out.println(example.toString());
    System.out.println("Average reprojection errors in pixels:"); 
    example.printImageToDisplayCoordsErrors(screenPoints, cameraPoints);
    
    if (screenPointsTest != null && !screenPointsTest.isEmpty() &&
        cameraPointsTest != null && !cameraPointsTest.isEmpty()) {
      System.out.println("Average test squared error:"); 
      example.printImageToDisplayCoordsErrors(screenPointsTest, 
          cameraPointsTest);
    } else {
      LOGGER.warning("No test data.");
    }
    
    if (savePath != null)
      example.save(savePath);
    example.release();
  }
}
