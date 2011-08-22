package edu.mit.yingyin.tabletop;

import java.nio.ShortBuffer;

import org.OpenNI.Context;
import org.OpenNI.DepthGenerator;
import org.OpenNI.DepthMetaData;
import org.OpenNI.GeneralException;
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
  private Player player = null;
  private DepthMetaData depthMD;
  private int depthWidth, depthHeight;
  
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
      } else if (type.equals(NodeType.PLAYER))
        player = (Player)node.getInstance();
    }
  }
  
  public int depthWidth() {
    return depthWidth;
  }

  public int depthHeight() {
    return depthHeight;
  }
  
  public ShortBuffer depthBuffer() throws StatusException {
    return depthMD.getData().createShortBuffer();
  }

  public int depthFrameID() {
    return depthMD.getFrameID();
  }
  
  public void waitAnyUpdateAll() throws StatusException {
    context.waitAnyUpdateAll();
    depthMD = depthGen.getMetaData();
  }
  
  public void waitDepthAndUpdateAll() throws StatusException {
    context.waitOneUpdateAll(depthGen);
    depthMD = depthGen.getMetaData();
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
}
