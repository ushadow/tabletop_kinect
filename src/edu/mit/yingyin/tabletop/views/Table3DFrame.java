package edu.mit.yingyin.tabletop.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.event.KeyListener;
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
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.swing.JFrame;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import org.OpenNI.Point3D;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

import edu.mit.yingyin.tabletop.models.Forelimb;
import edu.mit.yingyin.tabletop.models.InteractionSurface;
import edu.mit.yingyin.tabletop.models.ProcessPacket;
import edu.mit.yingyin.util.Geometry;

/**
 * 3D view of the tabletop.
 * @author yingyin
 *
 */
public class Table3DFrame extends JFrame {

  private static final Logger LOGGER = Logger.getLogger(
      Table3DFrame.class.getName());
  private static final Color3f RED = new Color3f(Color.RED);
  private static final int TABLE_WIDTH = 1200; // mm
  private static final int TABLE_HEIGHT = 920; // mm
  
  private static final long serialVersionUID = 1L;
  private Canvas3D canvas;
  private SimpleUniverse universe;
  private BranchGroup scene = new BranchGroup();
  private BranchGroup forelimbGroup;
  private BranchGroup intersectionGroup;
  private BufferedImage frontImage;
  private InteractionSurface table;
  private TransformGroup worldTransformGroup = new TransformGroup();
  private Point3d viewrLoc = new Point3d(-700, 0, -300);
  
  public Table3DFrame(int width, int height) {
    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(width, height));
    GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
    canvas = new Canvas3D(config);
    universe = new SimpleUniverse(canvas);
    add("Center", canvas);
    
    worldTransformGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
    worldTransformGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
  }
  
  public void initTable(InteractionSurface table) {
    this.table = table;
    startDrawing();
  }
  
  public boolean talbeInitialized() { return table != null; }
  
  public void showUI() {
    pack();
    setVisible(true);
  }
  
  @Override
  public void addKeyListener(KeyListener kl) {
    canvas.addKeyListener(kl);
  }
  
  public void redraw(ProcessPacket packet) {
    if (forelimbGroup != null) {
      // Detach from parent.
      forelimbGroup.detach();
      worldTransformGroup.removeChild(forelimbGroup);
    }
    forelimbGroup = new BranchGroup();
    forelimbGroup.setCapability(BranchGroup.ALLOW_DETACH);
    for (Forelimb fl : packet.forelimbs) {
      forelimbGroup.addChild(createForelimb(fl));
    }
    worldTransformGroup.addChild(forelimbGroup);
  }
  
  /**
   * Redraws intersection points.
   */
  public void redrawIntersections(Point3D[] intersections) {
    if (intersectionGroup != null) {
      intersectionGroup.detach();
      worldTransformGroup.removeChild(intersectionGroup);
    }
    intersectionGroup = new BranchGroup();
    intersectionGroup.setCapability(BranchGroup.ALLOW_DETACH);
    for (Point3D p : intersections) {
      intersectionGroup.addChild(createSphere(10, 
          new Vector3f(p.getX(), p.getY(), p.getZ()), 
          new Color3f(Color.BLACK)));
    }
    worldTransformGroup.addChild(intersectionGroup);
  }
  
  private TransformGroup createSphere(float radius, Vector3f translation, 
      Color3f color) {
    Sphere sphere = createSphere(radius, color);
    TransformGroup tg = new TransformGroup();
    Transform3D t = new Transform3D();
    t.setTranslation(new Vector3f(translation));
    tg.addChild(sphere);
    tg.setTransform(t);
    return tg;
  }
  
  private Sphere createSphere(float radius, Color3f color) {
    Appearance ap = new Appearance();
    ap.setMaterial(new Material(color, color, color, color, 1));
    return new Sphere(radius, ap);
  }
  
  private Group createForelimb(Forelimb fl) {
    Group group = new Group();
    if (fl.armJointW() == null || fl.numFingertips() <= 0)
      return null;
    
    Point3f armJointLoc = fl.armJointW();
    Point3f fingerLoc = fl.getFingertipsW().get(0);
    
    group.addChild(createSphere(30, new Vector3f(armJointLoc), RED));
    group.addChild(createSphere(10, new Vector3f(fingerLoc), RED));
    group.addChild(createLine(armJointLoc, fingerLoc, new Color3f(Color.CYAN)));
    return group;
  }
  
  private void addLights(BranchGroup group) {
    Color3f light1Color = new Color3f(0.7f, 0.8f, 0.8f);
    BoundingSphere bounds = new BoundingSphere(new Point3d(0, 0, -200),
        1200);
    Vector3f light1Direction = new Vector3f(0, 0, 1);
    DirectionalLight light1 = new DirectionalLight(light1Color, 
        light1Direction);
    light1.setInfluencingBounds(bounds);
    AmbientLight light2 = new AmbientLight(new Color3f(1f, 1f, 1f));
    light2.setInfluencingBounds(bounds);
  }

  private void startDrawing() {
    positionViewer();
    getScene();
    universe.addBranchGraph(scene);
  }

  private void getScene() {
    if (table.center().isNone())
      return;
    
    Point3f tableCenter = table.center().value();
    addLights(scene);

    Dimension d = getPreferredSize();
    frontImage = new BufferedImage(d.width, d.height, 
        BufferedImage.TYPE_INT_RGB);
    Graphics2D g = (Graphics2D) frontImage.getGraphics();
    g.setColor(new Color(70, 70, 140));
    g.fillRect(0, 0, d.width, d.height);

    TransformGroup axesTransformGroup = new TransformGroup();
    axesTransformGroup.addChild(createAxesGroup());
    Transform3D axesTransform = new Transform3D();
    axesTransform.setScale(200);
    axesTransform.setTranslation(new Vector3f(0, 0, tableCenter.z + 100));
    axesTransformGroup.setTransform(axesTransform);
    
    Group tableGroup = createTable();
    if (tableGroup != null)
      worldTransformGroup.addChild(tableGroup);
    worldTransformGroup.addChild(axesTransformGroup);
    MouseRotate behavior = new MouseRotate();
    BoundingSphere bounds = new BoundingSphere(new Point3d(tableCenter), 1200);
    behavior.setSchedulingBounds(bounds);
    behavior.setTransformGroup(worldTransformGroup);
    worldTransformGroup.addChild(behavior);
    worldTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
    worldTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
   
    scene.addChild(worldTransformGroup);
  }
  
  private Group createTable() {
    if (table.center().isNone())
      return null;
    Point3f center = table.center().value();
    Vector3f n = table.surfaceNormal();
    Point3f p1 = Geometry.pointOnPlaneZ(center.x + TABLE_WIDTH / 2, 
        center.y + TABLE_HEIGHT / 2, center, n);
    Point3f p2 = Geometry.pointOnPlaneZ(center.x + TABLE_WIDTH / 2, 
        center.y - TABLE_HEIGHT / 2, center, n);
    Point3f p3 = Geometry.pointOnPlaneZ(center.x - TABLE_WIDTH / 2, 
        center.y - TABLE_HEIGHT / 2, center, n);
    Point3f p4 = Geometry.pointOnPlaneZ(center.x - TABLE_WIDTH / 2, 
        center.y + TABLE_HEIGHT / 2, center, n);
    LOGGER.info("p1: " + p1);
    LOGGER.info("p2: " + p2);
    LOGGER.info("p3: " + p3);
    LOGGER.info("p4: " + p4);
    return createPlaneGroup(p1, p2, p3, p4);
  }
  
  private Group createPlaneGroup(Point3f p1, Point3f p2, Point3f p3, Point3f p4) 
  {
    Group group = new Group();
    QuadArray plane = new QuadArray(4, GeometryArray.COORDINATES);

    plane.setCoordinate(0, p1);
    plane.setCoordinate(1, p2);
    plane.setCoordinate(2, p3);
    plane.setCoordinate(3, p4);

    group.addChild(new Shape3D(plane));
    
    QuadArray planeBack = new QuadArray(4, GeometryArray.COORDINATES);
    planeBack.setCoordinate(0, p1);
    planeBack.setCoordinate(1, p4);
    planeBack.setCoordinate(2, p3);
    planeBack.setCoordinate(3, p2);
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
    
    LineAttributes la = new LineAttributes();
    la.setLineWidth(5);
    Appearance app = new Appearance();
    ColoringAttributes ca = new ColoringAttributes(c, 
        ColoringAttributes.SHADE_FLAT);
    app.setColoringAttributes(ca);
    app.setLineAttributes(la);
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
    LOGGER.info("axis of rotation is " + normal);
    LOGGER.info("angle of rotation is " + angle);
    return new AxisAngle4f(normal, (float) angle);
  }

  private void positionViewer() {
    ViewingPlatform vp = universe.getViewingPlatform();
    Transform3D lookAt = new Transform3D();
    lookAt.lookAt(viewrLoc, new Point3d(0, 0, -1100), new Vector3d(0, 0, 1));
    lookAt.invert();
    vp.getViewPlatformTransform().setTransform(lookAt);
    View view = universe.getViewer().getView();
    view.setBackClipDistance(1200);
    view.setFrontClipDistance(1);
    view.setFieldOfView(2 * Math.atan2(600, 1200));
  }
}
