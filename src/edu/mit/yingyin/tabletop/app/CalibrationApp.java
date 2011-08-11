package edu.mit.yingyin.tabletop.app;

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

import javax.imageio.ImageIO;
import javax.vecmath.Point2f;

import edu.mit.yingyin.calib.CalibView;
import edu.mit.yingyin.calib.GeoCalibModel;
import edu.mit.yingyin.tabletop.CalibrationExample;
import edu.mit.yingyin.tabletop.CalibrationExample.CalibMethod;
import edu.mit.yingyin.tabletop.OpenNIWrapper;
import edu.mit.yingyin.util.FileUtil;

public class CalibrationApp {
  private class CalibrationController extends KeyAdapter {
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
  }
  
  public static void main(String args[]) {
    new CalibrationApp(args);
  }
  
  private boolean isScrnCoord = true;
  private List<Point2f> screenPoints;
  private List<Point2f> cameraPoints;
  private List<Point2f> screenPointsTest;
  private List<Point2f> cameraPointsTest;
  private GeoCalibModel calibModel;
  private CalibMethod calibMethod = CalibMethod.Extrinsic;
  private String calibMethodStr; 
  private String savePath;
  
  @SuppressWarnings("static-access")
  public CalibrationApp(String args[]) {
    Properties config = new Properties();
    FileInputStream in = null;
    try {
      in = new FileInputStream(
          "./config/calibration.config");
      config.load(in);
      in.close();
    } catch (FileNotFoundException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } 
    
    String camImgPath = config.getProperty("cam-depth-image", null);
    String scrnImagePath = config.getProperty("screen-image", null);
    String screenPtsPath = config.getProperty("screen-points", null);
    String camPtsPath = config.getProperty("cam-points", null);
    String camPtsTestPath = config.getProperty("cam-points-t", null);
    String screenPtsTestPath = config.getProperty("screen-points-t", null);
    calibMethodStr = config.getProperty("calib-method", "extrinsic");
    savePath = config.getProperty("save", null);
    
    if (screenPtsPath != null)
      screenPoints = readPointsFromFile(screenPtsPath);
    
    if (camPtsPath != null)
      cameraPoints = readPointsFromFile(camPtsPath);
    
    if (screenPtsTestPath != null)
      screenPointsTest = readPointsFromFile(screenPtsTestPath);
    
    if (camPtsTestPath != null)
      cameraPointsTest = readPointsFromFile(camPtsTestPath);
    
    if (calibMethodStr.equals("homography"))
      calibMethod = calibMethod.Homography;
    else if (calibMethodStr.equals("distortion"))
      calibMethod = CalibMethod.Distortion;
    else calibMethod = calibMethod.Extrinsic;

    BufferedImage image = null;
    String ptsFileName = null;
    
    if (camImgPath != null || scrnImagePath != null) {
      if (camImgPath != null) {
        image = OpenNIWrapper.rawDepthToBufferedImage(camImgPath);
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
      CalibView view = new CalibView(calibModel);
      view.addKeyListener(new CalibrationController());
      view.showView();
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
  
  private void calibrate() {
    if (screenPoints != null && !screenPoints.isEmpty() && 
        cameraPoints != null && !cameraPoints.isEmpty()) {
      System.out.println("Calibration method: " + calibMethodStr);
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
      example.release();
    }
  }
}
