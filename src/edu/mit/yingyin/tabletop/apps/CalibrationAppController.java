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

import edu.mit.yingyin.calib.CalibLabelController;
import edu.mit.yingyin.calib.CalibLabelModel;
import edu.mit.yingyin.calib.CalibModel;
import edu.mit.yingyin.calib.CalibModel.CalibMethodName;
import edu.mit.yingyin.image.ImageConvertUtils;
import edu.mit.yingyin.util.FileUtil;

/**
 * Application for labeling the calibration images and computing the extrinsic
 * camera parameters.
 *
 * After labeling, calibration is run once the user hits "Q" or Escape.
 * @author yingyin
 *
 */
public class CalibrationAppController extends KeyAdapter {
  private static final Logger logger = Logger.getLogger(
      CalibrationAppController.class.getName());

  private static final int WIDTH = 640, HEIGHT = 480;
  
  private static final String DEFAULT_CALIB_DIR = "data/calibraiton/";
  
  public static void main(String args[]) {
    new CalibrationAppController(args);
  }

  public void keyPressed(KeyEvent ke) {
    switch (ke.getKeyCode()) {
      case KeyEvent.VK_C:
        calibrate();
        break;
      case KeyEvent.VK_N:
        // Show the next image to label.
        if (calibLabelController != null)
          calibLabelController.dispose();
        updatePoints(isCurrentLabelImageScrnCoord , isCurrentLabelImageTest);
        if (!isCurrentLabelImageScrnCoord && !isCurrentLabelImageTest) {
          isCurrentLabelImageTest = true;
          labelImage(camTestImgPath, isCurrentLabelImageScrnCoord, 
              isCurrentLabelImageTest);
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

  public CalibrationAppController(String args[]) {
    Properties config = new Properties();
    FileInputStream in = null;
    if (args.length < 1) {
      System.out.println("Usage: CalibrationAppController <config_file_name>");
      System.exit(-1);
    }

    try {
      in = new FileInputStream(args[0]);
      config.load(in);
      in.close();
    } catch (FileNotFoundException fnfe) {
      System.err.println(fnfe.getMessage());
      System.exit(-1);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(-1);
    }

    String calibDir = config.getProperty("calibration-dir", DEFAULT_CALIB_DIR);
    
    String camImgPath = config.getProperty("cam-depth-image", null);
    camImgPath = camImgPath == null ? null : calibDir + camImgPath;
    
    camTestImgPath = config.getProperty("cam-depth-test-image", null);
    camTestImgPath = camTestImgPath == null ? null : calibDir + camTestImgPath;
    
    String scrnImagePath = config.getProperty("screen-image", null);
    scrnImagePath = scrnImagePath == null ? null : calibDir + scrnImagePath; 
    
    String screenPtsPath = config.getProperty("screen-points", null);
    screenPtsPath = screenPtsPath == null ? null : calibDir + screenPtsPath;
    
    String camPtsPath = config.getProperty("cam-points", null);
    camPtsPath = camPtsPath == null ? null : calibDir + camPtsPath;
    
    String camPtsTestPath = config.getProperty("cam-points-t", null);
    camPtsTestPath = camPtsTestPath == null ? null : calibDir + camPtsTestPath;
    
    String screenPtsTestPath = config.getProperty("screen-points-t", null);
    screenPtsTestPath = screenPtsTestPath == null ? null : 
      calibDir + screenPtsTestPath;
    
    savePath = config.getProperty("save-path", null);
    savePath = savePath == null ? null : calibDir + savePath;
    
    calibMethodStr = config.getProperty("calib-method", "EXTRINSIC");
    
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
      System.err.println(e.getMessage());
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
      labelImage(firstImage, isCurrentLabelImageScrnCoord, 
                 isCurrentLabelImageTest);
    } else {
      calibrate();
    }
  }
  
  private void labelImage(String imagePath, boolean isScrnCoord, boolean isTest) 
  {
    if (imagePath == null) 
      return;

    BufferedImage image = null;
    String ptsFileName = null;
    image = ImageConvertUtils.readRawDepth(imagePath, WIDTH, HEIGHT);
    try {
      ImageIO.write(image, "PNG", 
          new File(FileUtil.setExtension(imagePath, "png")));
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.exit(-1);
    }
    isScrnCoord = false;
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
      System.err.println("CalibrationApp:" + e.getMessage());
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
      logger.info("Updated screen points");
    } else if (isTest) {
      cameraPointsTest = points;
      logger.info("Updated camera image test points");
    } else {
      cameraPoints = points;
      logger.info("Updated camera image points");
    }
  }
  
  /**
   * Calibrates the extrinsic parameters of the camera.
   */
  private void calibrate() {
    if (screenPoints == null || screenPoints.isEmpty()) {
      logger.warning("No screen points.");
      return;
    }
    if (cameraPoints == null || cameraPoints.isEmpty()) {
      logger.warning("No camera points.");
      return;
    }
   
    if (screenPoints.size() != cameraPoints.size()) {
      logger.warning("Dispaly image points and camera image points sizes " +
      		"are not the same. No calibraiton is done.");
      return;
    }
    
    logger.info("Calibration method: " + calibMethodStr);
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
      logger.warning("No test data.");
    }
    
    if (savePath != null)
      example.save(savePath);
    example.release();
  }
}
