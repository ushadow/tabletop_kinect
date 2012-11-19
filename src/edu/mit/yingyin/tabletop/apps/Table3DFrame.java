package edu.mit.yingyin.tabletop.apps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.LineArray;
import javax.media.j3d.PickInfo;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JFrame;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.pickfast.PickCanvas;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

import edu.mit.yingyin.tabletop.models.Table;

/**
 * 3D view of the tabletop.
 * @author yingyin
 *
 */
public class Table3DFrame extends JFrame {

  private static Logger logger = Logger.getLogger(Table3DFrame.class.getName());
  
  private static final long serialVersionUID = 1L;
  private int imageHeight = 256;
  private int imageWidth = 256;
  private Canvas3D canvas;
  private SimpleUniverse universe;
  private BranchGroup group = new BranchGroup();
  private PickCanvas pickCanvas;
  private BufferedImage frontImage;
  private Vector3f tableNormal;

  private static void addLights(BranchGroup group) {
    Color3f light1Color = new Color3f(0.7f, 0.8f, 0.8f);
    BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0),
        100.0);
    Vector3f light1Direction = new Vector3f(4.0f, -7.0f, -12.0f);
    DirectionalLight light1 = new DirectionalLight(light1Color, light1Direction);
    light1.setInfluencingBounds(bounds);
    group.addChild(light1);
    AmbientLight light2 = new AmbientLight(new Color3f(0.3f, 0.3f, 0.3f));
    light2.setInfluencingBounds(bounds);
    group.addChild(light2);
  }
  
  public Table3DFrame(Table table) {
    setPreferredSize(new Dimension(imageWidth, imageHeight));
    tableNormal = table.surfaceNormal();
    startDrawing();
  }
  
  public void showUI() {
    pack();
    setVisible(true);
  }

  private void startDrawing() {
    setLayout(new BorderLayout());
    GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
    canvas = new Canvas3D(config);
    universe = new SimpleUniverse(canvas);
    add("Center", canvas);
    positionViewer();
    getScene();
    universe.addBranchGraph(group);
    pickCanvas = new PickCanvas(canvas, group);
    pickCanvas.setMode(PickInfo.PICK_BOUNDS);
  }

  private void getScene() {
    addLights(group);

    frontImage = new BufferedImage(imageWidth, imageHeight,
        BufferedImage.TYPE_INT_RGB);
    Graphics2D g = (Graphics2D) frontImage.getGraphics();
    g.setColor(new Color(70, 70, 140));
    g.fillRect(0, 0, imageWidth, imageHeight);

    TransformGroup planeTransformGroup = new TransformGroup();
    planeTransformGroup.addChild(createPlaneGroup());
    planeTransformGroup.addChild(createAxesGroup());
    Transform3D rotation = new Transform3D();
    Vector3f v1 = new Vector3f(0, 0, 1);
    rotation.set(createRotation(v1, tableNormal));
    planeTransformGroup.setTransform(rotation);

    TransformGroup worldTransformGroup = new TransformGroup();
    worldTransformGroup.addChild(planeTransformGroup);
    MouseRotate behavior = new MouseRotate();
    BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0),
        100.0);
    behavior.setSchedulingBounds(bounds);
    behavior.setTransformGroup(worldTransformGroup);
    worldTransformGroup.addChild(behavior);
    worldTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
    worldTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    worldTransformGroup.addChild(createLine(new Point3f(0, 0, 0.5f), 
        new Point3f(0, 0, -.5f), new Color3f(Color.MAGENTA)));
   
    worldTransformGroup.setTransform(objectToWorldTransform());
    
    group.addChild(worldTransformGroup);
  }
  
  private Transform3D objectToWorldTransform() {
    Transform3D worldTransform = new Transform3D();
    worldTransform.rotZ(-Math.PI / 2);
    Transform3D rotY = new Transform3D();
    rotY.rotY(Math.PI);
    worldTransform.mul(rotY);
    return worldTransform;
  }

  private Group createPlaneGroup() {
    Group group = new Group();
    Point3f a = new Point3f(-0.5f, .5f, 0);
    Point3f b = new Point3f(0.5f, .5f, 0);
    Point3f c = new Point3f(0.5f, -.5f, 0);
    Point3f d = new Point3f(-0.5f, -.5f, 0);

    QuadArray plane = new QuadArray(4, GeometryArray.COORDINATES);

    plane.setCoordinate(0, a);
    plane.setCoordinate(1, b);
    plane.setCoordinate(2, c);
    plane.setCoordinate(3, d);

    group.addChild(new Shape3D(plane));
    
    QuadArray planeBack = new QuadArray(4, GeometryArray.COORDINATES);
    planeBack.setCoordinate(0, a);
    planeBack.setCoordinate(1, d);
    planeBack.setCoordinate(2, c);
    planeBack.setCoordinate(3, b);
    group.addChild(new Shape3D(planeBack));

    return group;
  }
  
  private Group createAxesGroup() {
    Group axesGroup = new Group();
    axesGroup.addChild(createLine(new Point3f(0, 0, 0), new Point3f (1, 0, 0),
        new Color3f(Color.RED)));
    axesGroup.addChild(createLine(new Point3f(0, 0, 0), new Point3f (0, 1, 0),
        new Color3f(Color.GREEN)));
    axesGroup.addChild(createLine(new Point3f(0, 0, 0), new Point3f (0, 0, 1),
        new Color3f(Color.BLUE)));
    return axesGroup;
  }
  
  private Shape3D createLine(Point3f p1, Point3f p2, Color3f c) {
    Point3f[] pts = new Point3f[2];
    pts[0] = p1;
    pts[1] = p2;
    LineArray lineArr = new LineArray(2, GeometryArray.COORDINATES); 
    lineArr.setCoordinates(0, pts);
    
    Appearance app = new Appearance();
    ColoringAttributes ca = new ColoringAttributes(c, 
        ColoringAttributes.SHADE_FLAT);
    app.setColoringAttributes(ca);
    return new Shape3D(lineArr, app);
  }
  
  private AxisAngle4f createRotation(Vector3f v1, Vector3f v2) {
    v1.normalize();
    v2.normalize();
    Vector3f normal = new Vector3f();
    normal.cross(v1, v2);
    double cosTheta = v1.dot(v2);
    double sineTheta = normal.length();
    double angle = Math.atan2(sineTheta, cosTheta);
    normal.normalize();
    logger.info("axis of rotation is " + normal);
    logger.info("angle of rotation is " + angle);
    return new AxisAngle4f(normal, (float) angle);
  }

  private void positionViewer() {
    ViewingPlatform vp = universe.getViewingPlatform();
    vp.setNominalViewingTransform();
  }
}
