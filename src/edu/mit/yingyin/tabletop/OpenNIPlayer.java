package edu.mit.yingyin.tabletop;

import java.nio.ShortBuffer;

import org.OpenNI.Context;
import org.OpenNI.DepthGenerator;
import org.OpenNI.DepthMetaData;
import org.OpenNI.GeneralException;
import org.OpenNI.OutArg;
import org.OpenNI.Player;
import org.OpenNI.PlayerSeekOrigin;
import org.OpenNI.ScriptNode;
import org.OpenNI.StatusException;

public class OpenNIPlayer {
  
  private Context context;
  private OutArg<ScriptNode> scriptNode = new OutArg<ScriptNode>();
  private DepthGenerator depthGen;
  private Player player;
  private DepthMetaData depthMD;
  private int width, height;
  
  /**
   * Creates a player that plays back a recorded file.
   * @param configFile
   * @throws GeneralException
   */
  public OpenNIPlayer(String configFile) throws GeneralException {
    context = Context.createFromXmlFile(configFile, scriptNode);
    depthGen = DepthGenerator.create(context);
    player = Player.create(context, null);
    depthMD = depthGen.getMetaData();
    width = depthMD.getFullXRes();
    height = depthMD.getFullYRes();
  }
  
  public int depthWidth() {
    return width;
  }

  public int depthHeight() {
    return height;
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
  
  private void seekFrame(int diff) throws StatusException {
    player.seekToFrame(depthGen, PlayerSeekOrigin.CURRENT, diff);
  }
}
