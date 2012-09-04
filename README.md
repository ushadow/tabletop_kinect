This code is part of the research project for gestural input on a tabletop display. One Microsoft's Kinect is used for tracking hands. The Kinect is placed above the tabletop about 1.2m above. This [picture] (http://people.csail.mit.edu/yingyin/resources/image/tabletop.png) shows the setup. Currently, the code is only tested with this setup. Certain parameters are set based on the assumptions according to this setup, e.g., the distance of the tabletop from the Kinect. A different setup may cause some issues.

# Environment 
* Tested on Ubuntu 11.10 and 12.04.

# Prerequisites

## Install OpenNI framework and the corresponding Kinect driver
* [OpenNI] (https://github.com/OpenNI/OpenNI) (Version 1.5.4.0 unstable version - May 7th 2012)
  * Currently tested with master branch commit f8467404e3e433ded1c7e960200e54e7f6fc0ce5
  * Follow the Linux installation instructions in README
* [SensorKinect] (https://github.com/avin2/SensorKinect) PrimeSense Sensor Module 
  for OpenNI (VVersion 5.1.2.1 unstable version - May 15th 2012) Kinect Mod:
  * Currently tested with unstable branch commit 2d139677f26c06fcd9fc8d4a7db033d70021624b
  * Follow the Linux installation instructions in README
  
## Jar files
### Put the following jar files in the *lib* directory:
* [JavaCV (javacv-0.2-bin.zip)] (http://code.google.com/p/javacv/) 
  * javacpp.jar  
  * javacv-linux-x86_64.jar (Make sure it has the same bitness as OpenCV.)
  * javacv.jar
* [vecmath-1.3.1.jar] (http://www.findjar.com/jar/java3d/jars/vecmath-1.3.1.jar.html)
* [commons-cli-1.2.jar] (http://commons.apache.org/cli/)

### Other jar files
* org.OpenNI.jar - compiled from OpenNI installation and by default is in the */usr/share/java/* folder. The build.xml file assumes the jar file is in that folder.

## Other dependencies:
* [OpenCV 2.4.2] (http://sourceforge.net/projects/opencvlibrary/files/)
  * For compilation instruction, follow this [guide] (http://opencv.willowgarage.com/wiki/InstallGuide%20%3A%20Debian).
  * There are a few changes needed to install all the necessary libaries:
	 
	  ```
	  sudo apt-get install libavcodec-dev libavcodec53 libavformat53 libavformat-dev
	  sudo apt-get libtbb2 libtbb-dev
	  ```

# How to build and run
* To build all the code, type `ant build` at the command under the main project directory.
* The main class for hand tracking is `edu.mit.yingyin.tabletop.apps.HandTrackingAppController`. To run it, type `ant HandTrackingAppController`. The program assumes the *config* and *data* directories are under the main project direcotry.
* For a sample .oni file, you can download it from [here](http://people.csail.mit.edu/yingyin/resources/share/sample.oni), and put it in the *data* directory. The default configuration for OpenNI is to run from the recording file *./data/sample.oni*. You can change it to run from other recordings or to use actual sensors in *config/config.xml*. 