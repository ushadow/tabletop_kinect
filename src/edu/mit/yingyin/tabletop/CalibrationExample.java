package edu.mit.yingyin.tabletop;

import static com.googlecode.javacv.cpp.opencv_calib3d.cvFindExtrinsicCameraParams2;
import static com.googlecode.javacv.cpp.opencv_calib3d.cvFindHomography;
import static com.googlecode.javacv.cpp.opencv_calib3d.cvRodrigues2;
import static com.googlecode.javacv.cpp.opencv_core.CV_32FC1;
import static com.googlecode.javacv.cpp.opencv_core.CV_32FC2;
import static com.googlecode.javacv.cpp.opencv_core.cvGEMM;
import static com.googlecode.javacv.cpp.opencv_core.cvTranspose;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvUndistortPoints;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import javax.vecmath.Point2f;

import com.googlecode.javacv.cpp.opencv_core.CvMat;

public class CalibrationExample {

  public enum CalibMethod {EXTRINSIC, HOMOGRAPHY, UNDISTORT};
  
  private class HomographyMethod {
    private CvMat homographyMat = CvMat.create(3, 3, CV_32FC1);
    
    /**
     * Given the correspondence of points in two planes, finds the projective 
     * mapping from one plane to another.
     * 
     * If the calibration is UNDISTORT, apply undistortion to the camera image 
     * points first.
     * 
     * @param objectPoints points in the projected display.
     * @param imagePoints corresponding points in the camera image plane.
     */
    public HomographyMethod(List<Point2f> objectPoints,
        List<Point2f> imagePoints) {
      
      CvMat objectPointsMat = CvMat.create(objectPoints.size(), 1, CV_32FC2);
      CvMat imagePointsMat = CvMat.create(imagePoints.size(), 1, CV_32FC2);
      
      for (int i = 0; i < objectPoints.size(); i++) {
        objectPointsMat.put(i * 2, objectPoints.get(i).x);
        objectPointsMat.put(i * 2 + 1, objectPoints.get(i).y);
        
        imagePointsMat.put(i * 2, imagePoints.get(i).x);
        imagePointsMat.put(i * 2 + 1, imagePoints.get(i).y);
      }
      
      if (method == CalibMethod.UNDISTORT)
        cvUndistortPoints(imagePointsMat, imagePointsMat, intrinsicMatrixMat, 
            distortionCoeffsMat, null, null);
      cvFindHomography(imagePointsMat, objectPointsMat, homographyMat);
    }
    
    public HomographyMethod(Scanner scanner) {
      for (int i = 0; i < 3; i++)
        for (int j = 0; j < 3; j++)
          homographyMat.put(i, j, scanner.nextFloat());
    }
    
    public void release() {
      homographyMat.release();
    }
    
    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("Homography matrix: [");
      for (int i = 0; i < 3; i++)
        for (int j = 0; j < 3; j++) 
          sb.append(homographyMat.get(i, j) + " ");
      sb.append("]");
      return sb.toString();
    }
    
    public void save(PrintStream ps) {
      for (int i = 0; i < 3; i++)
        for (int j = 0; j < 3; j++)
          ps.print(homographyMat.get(i, j) + " ");
      ps.println();
    }
  }
  
  private class ExtrinsicMethod {
    private CvMat rotationMat = CvMat.create(3, 3, CV_32FC1);
    private CvMat translationMat = CvMat.create(3, 1, CV_32FC1);
    
    /**
     * Requires objectPoints.size = imagePoints.size
     * @param objectPoints
     * @param imagePoints
     */
    public ExtrinsicMethod(List<Point2f> objectPoints,
        List<Point2f> imagePoints) {
      
      CvMat objectPointsMat = CvMat.create(objectPoints.size(), 3, CV_32FC1);
      CvMat imagePointsMat = CvMat.create(imagePoints.size(), 2, CV_32FC1);
      CvMat rodrigues = CvMat.create(3, 1, CV_32FC1);
      
      for (int i = 0; i < objectPoints.size(); i++) {
        objectPointsMat.put(i, 0, objectPoints.get(i).x);
        objectPointsMat.put(i, 1, objectPoints.get(i).y);
        objectPointsMat.put(i, 2, 0);
        
        imagePointsMat.put(i, 0, imagePoints.get(i).x);
        imagePointsMat.put(i, 1, imagePoints.get(i).y);
      }
      
      cvFindExtrinsicCameraParams2(objectPointsMat, imagePointsMat, 
          intrinsicMatrixMat, distortionCoeffsMat, rodrigues, translationMat);
      cvRodrigues2(rodrigues, rotationMat, null);
      objectPointsMat.release();
      imagePointsMat.release();
      rodrigues.release();
    }
    
    public ExtrinsicMethod(Scanner scanner) {
      for (int i = 0; i < 3; i++)
        for (int j = 0; j < 3; j++) 
          rotationMat.put(i, j, scanner.nextFloat());
      for (int i = 0; i < 3; i++)
        translationMat.put(i, scanner.nextFloat());
    }
    
    public void release() {
      translationMat.release();
      rotationMat.release();
    }
    
    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("Rotation matrix: [");
      for (int i = 0; i < 3; i++)
        for (int j = 0; j < 3; j++) {
          sb.append(rotationMat.get(i, j) + " ");
        }
      sb.append("]\n");
      sb.append("Translation matrixt: [");
      for (int i = 0; i < 3; i++)
        sb.append(translationMat.get(i) + " ");
      sb.append("]");
      return sb.toString();
    }
    
    public void save(PrintStream ps) {
      for (int i = 0; i < 3; i++)
        for (int j = 0; j < 3; j++)
          ps.print(rotationMat.get(i, j) + " ");
      ps.println();
      for (int i = 0; i < 3; i++)
        ps.print(translationMat.get(i) + " ");
      ps.println();
    }
    
    /**
     * Transforms image coordinates to display coordinates P_d.
     * 
     * @param imagePoint 2D point in image coordinates.
     * @return 2D point in display coordinates.
     */
    public Point2f imageToDisplayCoordsExtrinsic(Point2f imagePoint) {
      CvMat imageCoords = CvMat.create(1, 1, CV_32FC2);
      imageCoords.put(0, imagePoint.x);
      imageCoords.put(1, imagePoint.y);
      cvUndistortPoints(imageCoords, imageCoords, intrinsicMatrixMat, 
          distortionCoeffsMat, null, null);
      
      // P_cn = [X_c / Z_c, Y_c / Z_c, 1]
      // P_c = Z_c * P_cn
      CvMat normalizedCameraCoords = CvMat.create(3, 1, CV_32FC1);
      normalizedCameraCoords.put(0, imageCoords.get(0));
      normalizedCameraCoords.put(1, imageCoords.get(1));
      normalizedCameraCoords.put(2, 1);

      // P_c = R * P_d + T
      // P_d = R^-1 * (P_c - T) = Z_c * (R^-1 * P_cn) - R^-1 * T
      CvMat rotationInverse = CvMat.create(3, 3, CV_32FC1);
      cvTranspose(rotationMat, rotationInverse);
      
      // result1 = R^-1 * P_cn
      CvMat result1 = CvMat.create(3, 1, CV_32FC1);
      // result2 = R^-1 * T
      CvMat result2 = CvMat.create(3, 1, CV_32FC1);
      cvGEMM(rotationInverse, normalizedCameraCoords, 1, null, 0, result1, 0);
      cvGEMM(rotationInverse, translationMat, 1, null, 0, result2, 0);
      
      float cameraZ = (float)(result2.get(2) / result1.get(2));

      Point2f converted = new Point2f(
          (float)(result1.get(0) * cameraZ - result2.get(0)),
          (float)(result1.get(1) * cameraZ - result2.get(1)));
      
      imageCoords.release();
      imageCoords.release();
      rotationInverse.release();
      result1.release();
      result2.release();

      return converted;
    }
  }
  
  private static final float[][] INTRINSIC_MATRIX = {
      {(float)5.9421434211923247e+02, 0, (float)3.3930780975300314e+02},
      {0, (float)5.9104053696870778e+02, (float)2.4273913761751615e+02},
      {0, 0, 1}};
  
  private static final float[] DISTORTION_COEFFS = {
      (float)-2.6386489753128833e-01, (float)9.9966832163729757e-01, 
      (float)-7.6275862143610667e-04, (float)5.0350940090814270e-03,
      (float)-1.3053628089976321e+00};
  
  private CvMat intrinsicMatrixMat, distortionCoeffsMat;
  private CalibMethod method;
  private HomographyMethod homographyMethod;
  private ExtrinsicMethod extrinsicMethod;
  
  /**
   * Constructs a CalibrationExample from corresponding object points and image
   * points using the specified calibration method.
   * 
   * @param objectPoints a list of object points in 2D.
   * @param imagePoints a list of image points in 2D.
   * @param method calibration method.
   */
  public CalibrationExample(List<Point2f> objectPoints, 
      List<Point2f> imagePoints, CalibMethod method) {
    
    intrinsicMatrixMat = CvMat.create(3, 3, CV_32FC1);
    distortionCoeffsMat = CvMat.create(5, 1, CV_32FC1);
    
    for (int i = 0; i < 3; i++) 
      for (int j = 0; j < 3; j++) {
        intrinsicMatrixMat.put(i, j, INTRINSIC_MATRIX[i][j]);
      }
    for (int i = 0; i < 5; i++) {
      distortionCoeffsMat.put(i, DISTORTION_COEFFS[i]);
    }
    this.method = method;
    if (method == CalibMethod.EXTRINSIC) {
      extrinsicMethod = new ExtrinsicMethod(objectPoints, imagePoints);
    } else {
      homographyMethod = new HomographyMethod(objectPoints, imagePoints);
    }
  }
  
  /**
   * Constructs a CalibrationExample from a calibraion result file.
   * @param fileName
   */
  public CalibrationExample(String fileName) {
    try {
      Scanner scanner = new Scanner(new File(fileName));
      method = CalibMethod.valueOf(scanner.next());
      if (method == CalibMethod.EXTRINSIC) {
        extrinsicMethod = new ExtrinsicMethod(scanner);
      } else {
        homographyMethod = new HomographyMethod(scanner);
      }
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public CalibMethod calibMethod() {
    return method;
  }
  
  public CvMat homographyMat() {
    return homographyMat;
  }
  
  /**
   * Converts a point in the image coordinate to a point in the display 
   * coordinate.
   * @param imagePoint a point in the image coordinate.
   * @return corresponding point in the display coordinate.
   */
  public Point2f imageToDisplayCoords(Point2f imagePoint) {
    if (method == CalibMethod.EXTRINSIC)
      return imageToDisplayCoordsExtrinsic(imagePoint);
    else return imageToDisplayCoordsHomography(imagePoint);
  }
  
  /**
   * Calculates and prints the average error between the converted display 
   * coordinates from the image coordinates and the actual display coordinates.
   * 
   * @param displayCoords list of actaul display coordinates.
   * @param imageCoords list of corresponding image coordinates of the same size
   *     as displayCoords.
   */
  public void printImageToDisplayCoordsErrors(List<Point2f> displayCoords,
      List<Point2f> imageCoords) {
    float error = 0;
    float xError = 0;
    float yError = 0;
    int numPoints = displayCoords.size(); 
    for (int i = 0; i < numPoints; i++) {
      Point2f converted = imageToDisplayCoords(imageCoords.get(i));
      Point2f display = displayCoords.get(i);
      error += converted.distanceSquared(display);
      xError += (converted.x - display.x) * (converted.x - display.x);
      yError += (converted.y - display.y) * (converted.y - display.y);
    }
    System.out.println("X-axis average error = " + 
        Math.sqrt(xError / numPoints));
    System.out.println("Y-axis average error = " + 
        Math.sqrt(yError / numPoints));
    System.out.println("Average error = " + Math.sqrt(error / numPoints));
  }
  
  public void release() {
    if (intrinsicMatrixMat != null)
      intrinsicMatrixMat.release();
    
    if (distortionCoeffsMat != null)
      distortionCoeffsMat.release();
    
    if (homographyMethod != null)
      homographyMethod.release();
    
    if (extrinsicMethod != null)
      extrinsicMethod.release();
  }
  
  public String toString() {
    if (method == CalibMethod.EXTRINSIC) {
     return extrinsicMethod.toString();
    } else {
      return homographyMethod.toString();
    }
  }
  
  /**
   * Saves the calibration results in a text file.
   * @param fileName
   */
  public void save(String fileName) {
    PrintStream ps = null;
    try {
      ps = new PrintStream(new File(fileName));
      ps.println(method.toString());
      if (method == CalibMethod.EXTRINSIC) {
        extrinsicMethod.save(ps);
      } else {
        homographyMethod.save(ps);
      }
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.exit(-1);
    } finally {
      if (ps != null)
        ps.close();
    }
  }
  
  private Point2f imageToDisplayCoordsHomography(Point2f imagePoint) {
    CvMat imageCoords = null;
    if (method == CalibMethod.UNDISTORT) {
      imageCoords = CvMat.create(1, 1, CV_32FC2);
      imageCoords.put(0, imagePoint.x);
      imageCoords.put(1, imagePoint.y);
      cvUndistortPoints(imageCoords, imageCoords, intrinsicMatrixMat, 
          distortionCoeffsMat, null, null);
    } else {
      imageCoords = CvMat.create(2, 1, CV_32FC1);
      imageCoords.put(0, imagePoint.x);
      imageCoords.put(1, imagePoint.y);
    }
    CvMat src = CvMat.create(3, 1, CV_32FC1);
    CvMat dst = CvMat.create(3, 1, CV_32FC1);
    src.put(0, imageCoords.get(0));
    src.put(1, imageCoords.get(1));
    src.put(2, 1);
    cvGEMM(homographyMat, src, 1, null, 0, dst, 0);
    Point2f displayPoint = new Point2f((float)(dst.get(0) / dst.get(2)), 
                                       (float)(dst.get(1) / dst.get(2)));
    src.release();
    dst.release();
    imageCoords.release();
    return displayPoint;
  }
}
