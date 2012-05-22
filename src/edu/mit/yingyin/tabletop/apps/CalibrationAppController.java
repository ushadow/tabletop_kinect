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

import edu.mit.yingyin.calib.CalibController;
import edu.mit.yingyin.calib.GeoCalibModel;
import edu.mit.yingyin.tabletop.models.CalibrationExample;
import edu.mit.yingyin.tabletop.models.PartialOpenNIDevice;
import edu.mit.yingyin.tabletop.models.CalibrationExample.CalibMethodName;
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
  private static Logger logger = Logger.getLogger(
      CalibrationAppController.class.getName());
  
  public static void main(String args[]) {
    new CalibrationAppController(args);
  }

  public void keyPressed(KeyEvent ke) {
    switch (ke.getKeyCode()) {
    case KeyEvent.VK_Q:
    case KeyEvent.VK_ESCAPE:
      if (calibModel != null) {
        List<Point2f> points = new ArrayList<Point2f>(
            calibModel.getImagePoints().size());
        for (Point p : calibModel.getImagePoints()) 
          points.add(new Point2f(p.x, p.y));
        if (isScrnCoord)
          screenPoints = points;
        else cameraPoints = points;
        calibrate();
      }
      System.exit(0);
      break;
    default: break;
    }
  }
  
  private boolean isScrnCoord = true;
  private List<Point2f> screenPoints;
  private List<Point2f> cameraPoints;
  private List<Point2f> screenPointsTest;
  private List<Point2f> cameraPointsTest;
  private GeoCalibModel calibModel;
  private CalibMethodName calibMethod = CalibMethodName.EXTRINSIC;
  private String calibMethodStr; 
  private String savePath;
  
  public CalibrationAppController(String args[]) {
    Properties config = new Properties();
    FileInputStream in = null;
    if (args.length < 1) {
      System.out.println("Usage: CalibrationApp <config_file_name>");
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
    
    String camImgPath = config.getProperty("cam-depth-image", null);
    String scrnImagePath = config.getProperty("screen-image", null);
    String screenPtsPath = config.getProperty("screen-points", null);
    String camPtsPath = config.getProperty("cam-points", null);
    String camPtsTestPath = config.getProperty("cam-points-t", null);
    String screenPtsTestPath = config.getProperty("screen-points-t", null);
    calibMethodStr = config.getProperty("calib-method", "EXTRINSIC");
    savePath = config.getProperty("save-path", null);
    
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
    
    BufferedImage image = null;
    String ptsFileName = null;
    
    if (camImgPath != null || scrnImagePath != null) {
      if (camImgPath != null) {
        image = PartialOpenNIDevice.rawDepthToBufferedImage(camImgPath);
        try {
          ImageIO.write(image, "PNG", new File(FileUtil.setExtension(camImgPath, 
                                               "png")));
        } catch (IOException e) {
          System.err.println(e.getMessage());
          System.exit(-1);
        }
        isScrnCoord = false;
        ptsFileName = FileUtil.setExtension(camImgPath, "pts");
      } else if (scrnImagePath != null) {
        try {
          image = ImageIO.read(new File(scrnImagePath));
          isScrnCoord = true;
          ptsFileName = FileUtil.setExtension(scrnImagePath, "pts");
        } catch (IOException e) {
          System.err.println(e.getMessage());
          System.exit(-1);
        }
      }
      calibModel = new GeoCalibModel(image, ptsFileName, isScrnCoord);
      CalibController controller = new CalibController(calibModel);
      controller.addKeyListener(this);
      controller.showUI();
    } else {
      calibrate();
    }
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
  
  /**
   * Calibrates the extrinsic parameters of the camera.
   */
  private void calibrate() {
    if (screenPoints != null && !screenPoints.isEmpty() && 
        cameraPoints != null && !cameraPoints.isEmpty()) {
      
      if (screenPoints.size() != cameraPoints.size()) {
        logger.warning("Dispaly image points and camera image points sizes " +
        		"are not the same. No calibraiton is done.");
        return;
      }
      
      logger.info("Calibration method: " + calibMethodStr);
      CalibrationExample example = 
        new CalibrationExample(screenPoints, cameraPoints, calibMethod);
      System.out.println(example.toString());
      System.out.println("Average reprojection errors in pixels:"); 
      example.printImageToDisplayCoordsErrors(screenPoints, cameraPoints);
      
      if (screenPointsTest != null && !screenPointsTest.isEmpty() &&
          cameraPointsTest != null && !cameraPointsTest.isEmpty()) {
        System.out.println("Average test squared error:"); 
        example.printImageToDisplayCoordsErrors(screenPointsTest, 
            cameraPointsTest);
      }
      
      if (savePath != null)
        example.save(savePath);
      example.release();
    }
  }
}
