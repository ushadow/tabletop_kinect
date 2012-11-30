package edu.mit.yingyin.tabletop.models;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.logging.Logger;

import org.OpenNI.Context;
import org.OpenNI.DepthGenerator;
import org.OpenNI.DepthMetaData;
import org.OpenNI.GeneralException;
import org.OpenNI.ImageGenerator;
import org.OpenNI.ImageMetaData;
import org.OpenNI.NodeInfo;
import org.OpenNI.NodeInfoList;
import org.OpenNI.NodeType;
import org.OpenNI.OutArg;
import org.OpenNI.Player;
import org.OpenNI.PlayerSeekOrigin;
import org.OpenNI.Point3D;
import org.OpenNI.ScriptNode;
import org.OpenNI.StatusException;

import edu.mit.yingyin.util.DirectBufferUtil;

/**
 * This class provides the full functionality of an OpenNI sensor device.
 * @author yingyin
 *
 */
public class FullOpenNIDevice implements OpenNIDevice {
  private static Logger logger = Logger.getLogger(
      FullOpenNIDevice.class.getName());
  
  private Context context;
  private OutArg<ScriptNode> scriptNode = new OutArg<ScriptNode>();
  private DepthGenerator depthGen = null;
  private ImageGenerator imageGen = null;
  private Player player = null;
  private DepthMetaData depthMD;
  private ImageMetaData imageMD;
  private int depthWidth, depthHeight, imageWidth, imageHeight;
  /**
   * Preallocated buffers for depth and RGB data.
   */
  private ByteBuffer depthBuffer, imageBuffer;
  private int depthByteBufferSize, imageByteBufferSize;
  
  /**
   * Creates a player that plays back a recorded file.
   * @param configFile
   * @throws GeneralException
   */
  public FullOpenNIDevice(String configFile) throws GeneralException {
    context = Context.createFromXmlFile(configFile, scriptNode);
    NodeInfoList list = context.enumerateExistingNodes();
    for (NodeInfo node : list) {
      NodeType type = node.getDescription().getType();
      if (type.equals(NodeType.DEPTH)) {
        depthGen = (DepthGenerator)node.getInstance();
        depthMD = depthGen.getMetaData();
        depthWidth = depthMD.getFullXRes();
        depthHeight = depthMD.getFullYRes();
        depthByteBufferSize = depthHeight * depthWidth * 
            depthMD.getData().getBytesPerPixel();
        depthBuffer = DirectBufferUtil.allocateByteBuffer(
            depthByteBufferSize);
      } else if (type.equals(NodeType.PLAYER)) {
        player = (Player)node.getInstance();
      } else if (type.equals(NodeType.IMAGE)) {
        imageGen = (ImageGenerator)node.getInstance();
        imageMD = imageGen.getMetaData();
        imageWidth = imageMD.getFullXRes();
        imageHeight = imageMD.getFullYRes();
        imageByteBufferSize = imageHeight * imageWidth 
            * imageMD.getData().getBytesPerPixel();
        imageBuffer = DirectBufferUtil.allocateByteBuffer(
            imageByteBufferSize);
      }
    }
    logger.fine(info());
  }
  
  public int getDepthWidth() { return depthWidth; }

  public int getDepthHeight() { return depthHeight; }
  
  /**
   * @return the number of bytes in the depth buffer frame.
   */
  public int getDepthByteBufferSize() {
    return depthByteBufferSize;
  }
  
  public int getImageWidth() { return imageWidth; }
  
  public int getImageHeight() { return imageHeight; }

  public String info() {
    return String.format("Maximum depth = %d", getZRes());
  }
  
  public int getZRes() { 
    if (depthMD != null) 
      return depthMD.getZRes();
    return 0;
  }
  
  /**
   * 
   * @return the depth buffer as a <code>ShortBuffer</code>.
   * @throws StatusException
   */
  public ShortBuffer getDepthBuffer() throws StatusException {
    updateDepthBuffer();
    return depthBuffer.asShortBuffer();
  }
  
  /**
   * Updates <code>depthArray</code> with data from depth buffer.
   * @param depthArray
   * @throws StatusException
   */
  public void getDepthArray(short[] depthArray) throws StatusException {
    getDepthBuffer().get(depthArray);
  }

  public void getDepthArray(int[] depthArray) throws StatusException {
    ShortBuffer sb = getDepthBuffer();
    sb.rewind();
    while (sb.remaining() > 0) {
      int pos = sb.position();
      depthArray[pos] = sb.get() & 0xffff;
    }
  }
  
  /**
   * Returns the image <code>ByteBuffer</code>.
   * @return
   * @throws GeneralException
   */
  public ByteBuffer getImageBuffer() throws GeneralException {
    updateImageBuffer();
    return imageBuffer;
  }
  
  public int getImageFrameID() { return imageMD.getFrameID(); }

  /**
   * Returns the current depth frame ID.
   * @return current depth frame ID.
   */
  public int getDepthFrameID() { return depthMD.getFrameID(); }
  
  /**
   * Waits for any node to have new data. Once new data is available from any 
   * node, all nodes are updated.
   * @throws StatusException
   */
  public void waitAnyUpdateAll() throws StatusException {
    context.waitAnyUpdateAll();
    updateMetaData();
  }
  
  public void waitDepthUpdateAll() throws StatusException {
    context.waitOneUpdateAll(depthGen);
    updateMetaData();
  }
  
  /**
   * Waits for all nodes to have new data available, and then updates them.
   * @throws StatusException
   */
  public void waitAndUpdateAll() throws StatusException { 
    context.waitAndUpdateAll();
    updateMetaData();
  }
  
  public void waitImageUpdateAll() throws StatusException {
    context.waitOneUpdateAll(imageGen);
    updateMetaData();
  }
  
  public int getDeviceMaxDepth() {
    return depthGen.getDeviceMaxDepth();
  }
  
  public void release() { 
    context.release(); 
    logger.info("OpenNI released.");
  }
  
  /**
   * Seeks a certain number of frames forward or backward.
   * 
   * @param diff the number of frames to seek from the current frame. Forward if
   *    diff is positive, backward otherwise.
   * @throws StatusException
   */
  public void seekFrameBy(int diff) throws StatusException {
    if (player != null)
      player.seekToFrame(depthGen, PlayerSeekOrigin.CURRENT, diff);
  }
  
  public Point3D[] convertProjectiveToRealWorld(Point3D[] points) 
      throws StatusException {
    return depthGen.convertProjectiveToRealWorld(points);
  }
  
  private void updateMetaData() {
    if (depthGen.isDataNew())
      depthMD = depthGen.getMetaData();
    if (imageGen.isDataNew())
      imageMD = imageGen.getMetaData();
  }
  
  /**
   * Updates <code>depthBuffer</code> from meta data.
   */
  private void updateDepthBuffer() {
    depthMD.getData().copyToBuffer(depthBuffer, depthByteBufferSize);
  }
  
  /**
   * Updates <code>imageBuffer</code> from meta data.
   */
  private void updateImageBuffer() {
    imageMD.getData().copyToBuffer(imageBuffer, imageByteBufferSize);
  }
}
