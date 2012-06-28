This code is part of the research project for gestural input on a tabletop 
display. One Microsoft's Kinect is used for tracking hands. The Kinect is placed
above the tabletop about 1.2m above. This [picture] (http://people.csail.mit.edu/yingyin/resources/image/setup2.png) 
shows the setup. 

# Environment 
* Tested on Ubuntu 11.10 and 12.04.

# Prerequisites

## Install OpenNI framework and the corresponding Kinect driver

## Jars:
* JavaCV (20110820) http://code.google.com/p/javacv/downloads/detail?name=javacv-bin-20110820.zip&can=2&q=
 * javacpp.jar  
 * javacv-linux-x86_64.jar (Make sure it has the same bitness as OpenCV.)
 * javacv.jar
* org.OpenNI.jar
* vecmath.jar
* org.OpenNI.jar - Compiled from https://github.com/OpenNI/OpenNI unstable branch 
			(Version 1.5.2.23 - Dec 28th 2011)

## Other:
* OpenCV 2.3.1 http://sourceforge.net/projects/opencvlibrary/files/
 * For compilation instruction, follow this guide: http://opencv.willowgarage.com/wiki/InstallGuide%20%3A%20Debian
	 
	 There are a few changes needed to install all the necessary libaries:
	 
	 ```
	 sudo apt-get install libavcodec-dev libavcodec53 libavformat53 libavformat-dev
	 sudo apt-get libtbb2 libtbb-dev
	 ```
# How to run
* For a sample .oni file, you can download it from [here] (http://people.csail.mit.edu/yingyin/resources/share/sample.oni), 
  and put it in the *data* directory. You need to specify the recording file name 
  in the OpenNI config file *config/config.xml*.