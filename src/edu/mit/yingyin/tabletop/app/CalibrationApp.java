package edu.mit.yingyin.tabletop.app;

import java.awt.Point;
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
  
  private List<Point2f> screenPoints = new ArrayList<Point2f>();
  private List<Point2f> cameraPoints = new ArrayList<Point2f>();
  
  @SuppressWarnings("static-access")
  public CalibrationApp(String args[]) {
    // Raw depth image.
    Option camDepthImgOption = OptionBuilder.withLongOpt("cam-depth-image").
        hasArg().create();
    Option camPtsOption = OptionBuilder.withLongOpt("cam-points").hasArg().
        create();
    Option screenPtsOption = OptionBuilder.withLongOpt("screen-points").
        hasArg().create();
    Option scrnImgOption = OptionBuilder.withLongOpt("screen-image").hasArg().
        create();
    
    CommandLineOptions.addOption(camDepthImgOption);
    CommandLineOptions.addOption(screenPtsOption);
    CommandLineOptions.addOption(scrnImgOption);
    CommandLineOptions.addOption(camPtsOption);
    
    CommandLineOptions.parse(args);
    
    String camImgPath = CommandLineOptions.getOptionValue("cam-depth-image", 
        null);
    String screenPtsPath = CommandLineOptions.getOptionValue("screen-points", 
        null);
    String scrnImagePath = CommandLineOptions.getOptionValue("screen-image",
        null);
    String camPtsPath = CommandLineOptions.getOptionValue("cam-points", null);
    
    Scanner scanner = null;
    
    if (screenPtsPath != null) {
      try {
        scanner = new Scanner(new File(screenPtsPath));
        while (scanner.hasNext()) {
          screenPoints.add(new Point2f(scanner.nextInt(), scanner.nextInt()));
        }
        
      } catch (FileNotFoundException e) {
        System.err.println("CalibrationApp:" + e.getMessage());
      } finally {
        scanner.close();
      }
    }
    
    if (camPtsPath != null) {
      try {
        scanner = new Scanner(new File(camPtsPath));
        while (scanner.hasNext()) {
          cameraPoints.add(new Point2f(scanner.nextInt(), scanner.nextInt()));
        }
      } catch (FileNotFoundException e) {
        System.err.println("CalibrationApp:" + e.getMessage());
      } finally {
        scanner.close();
      }
    }
    
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
      if (!cameraPoints.isEmpty() && !screenPoints.isEmpty()) {
        CalibrationExample example = 
          new CalibrationExample(screenPoints, cameraPoints);
        System.out.println(example.toString());
        System.out.println("Conversion error: " + 
            example.imageToDisplayCoordsError(screenPoints, cameraPoints));
        example.release();
      }
      
    }
  }
  
  public static void main(String args[]) {
    new CalibrationApp(args);
  }
}
