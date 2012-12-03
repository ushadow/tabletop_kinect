This code is part of the research project for gestural input on a tabletop display. One Microsoft's Kinect is used for tracking hands. The Kinect is placed above the tabletop about 1.2m above. This [picture] (http://people.csail.mit.edu/yingyin/resources/image/tabletop.png) shows the setup. Currently, the code is only tested with this setup. Certain parameters are set based on the assumptions according to this setup, e.g., the distance of the tabletop from the Kinect. A different setup may cause some issues.

# Environment 
* Currently developing on Ubuntu 12.04 with Sun Java 7. Tested on Windows 7.

# Prerequisites

## Install OpenNI framework and the corresponding Kinect driver
* [OpenNI] (https://github.com/OpenNI/OpenNI) (Version 1.5.4.0 unstable version - May 7th 2012)
  * Currently using with unstable branch commit 7409554df0761bca0213c900189885ff3c4ac852
  * Follow the Linux installation instructions in README
  * Or use the binaries availabe at http://www.openni.org/Downloads/OpenNIModules.aspx
* [SensorKinect] (https://github.com/avin2/SensorKinect) PrimeSense Sensor Module 
  for OpenNI (VVersion 5.1.2.1 unstable version - May 15th 2012) Kinect Mod:
  * Currently tested with unstable branch commit 2d139677f26c06fcd9fc8d4a7db033d70021624b
  * Follow the Linux installation instructions in README
  * Or use the binaries available at http://www.openni.org/Downloads/OpenNIModules.aspx. 
    Choose "OpenNI Compliant Hardward Binaries" and "Unstable".
* Test that the Kinect driver and OpenNI are installed properly by running the 
  NiViewer which is in `Platform/Linux/Bin/x86-Release/` under the OpenNI directory. 
  If you get a "failed to set USB inferface" error, you need to remove the 
  `gspca_kinect` kernel module by adding the following line to the file `/etc/modprobe.d/blacklist.conf`. 

  ```
  blacklist gspcs_kinect  
  ```
  You need `sudo` permission to edit that file.
  
## Jar files
### Put the following jar files in the *lib* directory:
* [JavaCV (javacv-0.3-bin.zip)] (http://code.google.com/p/javacv/) 
  * javacpp.jar  
  * javacv-linux-x86_64.jar (Make sure it has the same bitness as OpenCV. Rename it to javacv-os.jar.)
  * javacv.jar
* [Java3D API 1.5.1] (http://www.oracle.com/technetwork/java/javasebusiness/downloads/java-archive-downloads-java-client-419417.html#java3d-1.5.1-oth-JPR)
  * j3dcore.jar
  * j3dutils.jar
  * vecmath.jar
* [commons-cli-1.2.jar] (http://commons.apache.org/cli/)
* org.OpenNI.jar - compiled from OpenNI installation and by default is in the */usr/share/java/* folder. 

## Other dependencies:
* [OpenCV 2.4.3] (http://sourceforge.net/projects/opencvlibrary/files/)
  * For compilation instruction, follow this [guide] (http://opencv.willowgarage.com/wiki/InstallGuide%20%3A%20Debian).
  * There are a few changes needed to install all the necessary libaries:
	 
	  ```
	  sudo apt-get install libavcodec-dev libavcodec53 libavformat53 libavformat-dev
	  sudo apt-get install libtbb2 libtbb-dev
	  ```

# How to build and run
* To build all the code, type `ant build` at the command under the main project directory.
* The main class for hand tracking is `edu.mit.yingyin.tabletop.apps.HandTrackingApp`. To run it, type `ant HandTrackingAppController`. The program assumes the *config* and *data* directories are under the main project direcotry.
* For a sample .oni file, you can download it from [here](http://people.csail.mit.edu/yingyin/resources/share/sample.oni), and put it in the *data* directory. The default configuration for OpenNI is to run from the recording file *./data/sample.oni*. 
* The OpenNI config file *config.xml* in *config* folder is currently set to run from the actual sensor. 