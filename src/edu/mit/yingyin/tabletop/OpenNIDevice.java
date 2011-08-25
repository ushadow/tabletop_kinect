package edu.mit.yingyin.tabletop;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

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
import org.OpenNI.ScriptNode;
import org.OpenNI.StatusException;

public class OpenNIDevice {
  
  private Context context;
  private OutArg<ScriptNode> scriptNode = new OutArg<ScriptNode>();
  private DepthGenerator depthGen = null;
  private ImageGenerator imageGen = null;
  private Player player = null;
  private DepthMetaData depthMD;
  private ImageMetaData imageMD;
  private int depthWidth, depthHeight, imageWidth, imageHeight;
  
  /**
   * Creates a player that plays back a recorded file.
   * @param configFile
   * @throws GeneralException
   */
  public OpenNIDevice(String configFile) throws GeneralException {
    context = Context.createFromXmlFile(configFile, scriptNode);
    NodeInfoList list = context.enumerateExistingNodes();
    for (NodeInfo node : list) {
      NodeType type = node.getDescription().getType();
      if (type.equals(NodeType.DEPTH)) {
        depthGen = (DepthGenerator)node.getInstance();
        depthMD = depthGen.getMetaData();
        depthWidth = depthMD.getFullXRes();
        depthHeight = depthMD.getFullYRes();
      } else if (type.equals(NodeType.PLAYER)) {
        player = (Player)node.getInstance();
      } else if (type.equals(NodeType.IMAGE)) {
        imageGen = (ImageGenerator)node.getInstance();
        imageMD = imageGen.getMetaData();
        imageWidth = imageMD.getFullXRes();
        imageHeight = imageMD.getFullYRes();
      }
    }
  }
  
  public int depthWidth() { return depthWidth; }

  public int depthHeight() { return depthHeight; }
  
  public int imageWidth() { return imageWidth; }
  
  public int imageHeight() { return imageHeight; }
  
  public ShortBuffer depthBuffer() throws StatusException {
    return depthMD.getData().createShortBuffer();
  }
  
  public void depthArray(short[] depthArray) throws StatusException {
    depthBuffer().get(depthArray);
  }

  public void depthArray(int[] depthArray) throws StatusException {
    ShortBuffer sb = depthBuffer();
    sb.rewind();
    while (sb.remaining() > 0) {
      int pos = sb.position();
      depthArray[pos] = sb.get() & 0xffffffff;
    }
  }
  
  public ByteBuffer imageBuffer() throws GeneralException {
    return imageGen.getImageMap().createByteBuffer();
  }
  
  public int imageFrameID() { return imageMD.getFrameID(); }

  public int depthFrameID() { return depthMD.getFrameID(); }
  
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
  
  public int deviceMaxDepth() {
    return depthGen.getDeviceMaxDepth();
  }
  
  public void release() { 
    context.release(); 
    System.out.println("OpenNI released.");
  }
  
  public void seekFrame(int diff) throws StatusException {
    if (player != null)
      player.seekToFrame(depthGen, PlayerSeekOrigin.CURRENT, diff);
  }
  
  private void updateMetaData() {
    depthMD = depthGen.getMetaData();
    imageMD = imageGen.getMetaData();
  }
    
}
