This code is part of the research project for gestural input on a tabletop 
display. One Microsoft's Kinect is used for tracking hands. The Kinect is placed
above the tabletop about 1.2m above. This [picture] 
(http://people.csail.mit.edu/yingyin/resources/image/tabletop.png) shows the setup. 

# Environment 
* Tested on Ubuntu 11.10 and 12.04.

# Prerequisites

## Install OpenNI framework and the corresponding Kinect driver
* [OpenNI] (https://github.com/OpenNI/OpenNI) (Version 1.3.2.1 Stable version - Jul 4th 2011)
  * Currently tested with master branch commit f8467404e3e433ded1c7e960200e54e7f6fc0ce5
  * Follow the Linux installation instructions in README
* [SensorKinect] (https://github.com/avin2/SensorKinect) PrimeSense Sensor Module 
  for OpenNI (Version 5.0.3.4 Unstable version - Jul 19th 2011) Kinect Mod:
  * Currently tested with unstable branch commit 2d139677f26c06fcd9fc8d4a7db033d70021624b
  * Follow the Linux installation instructions in README
  
## Put the following jar files in the *lib* directory:
* JavaCV (20110820) http://code.google.com/p/javacv/downloads/detail?name=javacv-bin-20110820.zip&can=2&q=
  * javacpp.jar  
  * javacv-linux-x86_64.jar (Make sure it has the same bitness as OpenCV.)
  * javacv.jar
* vecmath.jar
* org.OpenNI.jar - compiled from OpenNI installation

## Other dependencies:
* OpenCV 2.3.1 http://sourceforge.net/projects/opencvlibrary/files/
  * For compilation instruction, follow this guide: http://opencv.willowgarage.com/wiki/InstallGuide%20%3A%20Debian
  * There are a few changes needed to install all the necessary libaries:
	 
	  ```
	  sudo apt-get install libavcodec-dev libavcodec53 libavformat53 libavformat-dev
	  sudo apt-get libtbb2 libtbb-dev
	  ```

# How to run
* The main class for hand tracking is `edu.mit.yingyin.tabletop.apps.HandTrackingAppController`.
* For a sample .oni file, you can download it from [here] (http://people.csail.mit.edu/yingyin/resources/share/sample.oni), and put it in the *data* directory. You need to specify the recording file name in the OpenNI config file *config/config.xml*.