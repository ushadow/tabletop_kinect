package edu.mit.yingyin.tabletop.apps {
import edu.mit.yingyin.tabletop.models.HandTrackingEngine
  object HandTrackingApp {
    def main(args:Array[String]) = {
      val mainDir = "/afs/csail/u/y/yingyin/research/kinect/"
      val openniConfigFile = mainDir + "config/config.xml"
      val calibrationFile = mainDir + "data/calibration/calibration.txt"
      var engine = new HandTrackingEngine(openniConfigFile, calibrationFile, 
          1600)
    }
  }
}