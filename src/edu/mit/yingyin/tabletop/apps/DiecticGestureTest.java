package edu.mit.yingyin.tabletop.apps;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import edu.mit.yingyin.tabletop.views.DisplayTargetFrame;
import edu.mit.yingyin.util.CommandLineOptions;
import edu.mit.yingyin.util.FileUtil;

public class DiecticGestureTest extends KeyAdapter {
  private static final String CONFIG_DIR = "config";
  private static final String DATA_DIR = "data";
  private static final String DEFAULT_OPENNI_CONFIG_FILE = FileUtil.join(
      CONFIG_DIR, "config.xml");
  private static final String DEFAULT_CALIB_FILE = FileUtil.join(DATA_DIR, 
      "calibration", "calibration.txt");
  
  public static void main(String[] args) {
    @SuppressWarnings("static-access")
    Option mainDirOpt = OptionBuilder.withArgName("main directory").withLongOpt(
        "dir").hasArg().create("d");
    CommandLineOptions.addOption(mainDirOpt);
    CommandLineOptions.parse(args);
    final String mainDir = CommandLineOptions.getOptionValue("d", ".");
    SwingUtilities.invokeLater(new Runnable() {
      
      @Override
      public void run() {
        new DiecticGestureTest(mainDir);
      }
    });
  }

  public DiecticGestureTest(String mainDir) {
    String openniConfigFile = FileUtil.join(mainDir, 
        DEFAULT_OPENNI_CONFIG_FILE);

    String calibrationFile = FileUtil.join(mainDir, DEFAULT_CALIB_FILE); 
    
    
    DisplayTargetFrame frame = new DisplayTargetFrame();
    frame.addKeyListener(this);
    frame.showUI();
  }
  
  public void keyPressed(KeyEvent ke) {
    switch (ke.getKeyCode()) {
      case KeyEvent.VK_ESCAPE:
        System.exit(0);
        break;
      default:
        break;
    }
  }
}
