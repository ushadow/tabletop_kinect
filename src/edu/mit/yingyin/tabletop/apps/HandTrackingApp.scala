package edu.mit.yingyin.tabletop.apps {
import edu.mit.yingyin.tabletop.models.HandTrackingEngine
import org.apache.commons.cli.OptionBuilder
import edu.mit.yingyin.util.CommandLineOptions
import edu.mit.yingyin.tabletop.models.EnvConstants
  
object HandTrackingApp {
  def main(args:Array[String]) = {
    // Parse command line arguments.
    OptionBuilder.withArgName("main direction")
    OptionBuilder.withLongOpt("dir")
    OptionBuilder.hasArg
    OptionBuilder.withDescription("The main directory for input and output. " +
        "The configuration file should be in <dir>/config/folder." + 
        "The default dir is the current directory.")
    val mainDirOpt = OptionBuilder.create("d")
    CommandLineOptions.addOption(mainDirOpt)
    CommandLineOptions.parse(args)
    val mainDir = CommandLineOptions.getOptionValue("d", "./")
    
    val openniConfigFile = mainDir + "config/config.xml"
    val calibrationFile = mainDir + "data/calibration/calibration.txt"
    
    val engine = new HandTrackingEngine(openniConfigFile, calibrationFile, 
        EnvConstants.DEFAULT_MAX_DEPTH)
  }
}}