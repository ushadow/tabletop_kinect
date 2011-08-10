package edu.mit.yingyin.tabletop.app;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.vecmath.Point2f;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import edu.mit.yingyin.calib.CalibView;
import edu.mit.yingyin.calib.GeoCalibModel;
import edu.mit.yingyin.tabletop.CalibrationExample;
import edu.mit.yingyin.tabletop.CalibrationExample.CalibMethod;
import edu.mit.yingyin.tabletop.OpenNIWrapper;
import edu.mit.yingyin.util.CommandLineOptions;
import edu.mit.yingyin.util.FileUtil;

public class CalibrationApp {
  private static class CalibrationController extends KeyAdapter {
    public void keyPressed(KeyEvent ke) {
      switch (ke.getKeyCode()) {
      case KeyEvent.VK_Q:
      case KeyEvent.VK_ESCAPE:
        System.exit(0);
        break;
      default: break;
      }
    }
  }
  
  public static void main(String args[]) {
    new CalibrationApp(args);
  }
  
  private List<Point2f> screenPoints;
  private List<Point2f> cameraPoints;
  private List<Point2f> screenPointsTest;
  private List<Point2f> cameraPointsTest;
  
  @SuppressWarnings("static-access")
  public CalibrationApp(String args[]) {
    // Raw depth image.
    Option camDepthImgOption = OptionBuilder.withLongOpt("cam-depth-image").
        hasArg().create();
    Option camPtsOption = OptionBuilder.withLongOpt("cam-points").hasArg().
        create();
    Option camPtsTestOption = OptionBuilder.withLongOpt("cam-points-t").
        hasArg().create();
    Option scrnImgOption = OptionBuilder.withLongOpt("screen-image").hasArg().
    create();
    Option screenPtsOption = OptionBuilder.withLongOpt("screen-points").
        hasArg().create();
    Option screenPtsTestOption = OptionBuilder.withLongOpt("screen-points-t").
        hasArg().create();
    
    CommandLineOptions.addOption(camDepthImgOption);
    CommandLineOptions.addOption(screenPtsOption);
    CommandLineOptions.addOption(scrnImgOption);
    CommandLineOptions.addOption(camPtsOption);
    CommandLineOptions.addOption(camPtsTestOption);
    CommandLineOptions.addOption(screenPtsTestOption);
    
    CommandLineOptions.parse(args);
    
    String camImgPath = CommandLineOptions.getOptionValue("cam-depth-image", 
        null);
    String scrnImagePath = CommandLineOptions.getOptionValue("screen-image",
        null);
    String screenPtsPath = CommandLineOptions.getOptionValue("screen-points", 
        null);
    String camPtsPath = CommandLineOptions.getOptionValue("cam-points", null);
    String camPtsTestPath = CommandLineOptions.getOptionValue("cam-points-t", 
        null);
    String screenPtsTestPath = CommandLineOptions.getOptionValue(
        "screen-points-t", null);
    
    if (screenPtsPath != null)
      screenPoints = readPointsFromFile(screenPtsPath);
    
    if (camPtsPath != null)
      cameraPoints = readPointsFromFile(camPtsPath);
    
    if (screenPtsTestPath != null)
      screenPointsTest = readPointsFromFile(screenPtsTestPath);
    
    if (camPtsTestPath != null)
      cameraPointsTest = readPointsFromFile(camPtsTestPath);
    
    boolean isScrnCoord = true;
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
      CalibView view = new CalibView(
          new GeoCalibModel(image, ptsFileName, isScrnCoord));
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
      CalibrationExample example = 
        new CalibrationExample(screenPoints, cameraPoints, 
                               CalibMethod.Extrinsic);
      System.out.println(example.toString());
      System.out.println("Average reprojection squared error: " + 
          example.imageToDisplayCoordsError(screenPoints, cameraPoints));
      
      if (screenPointsTest != null && !screenPointsTest.isEmpty() &&
          cameraPointsTest != null && !cameraPointsTest.isEmpty()) {
        System.out.println("Average test squared error: " +
            example.imageToDisplayCoordsError(screenPointsTest, 
                cameraPointsTest));
      }
      example.release();
    }
  }
}
