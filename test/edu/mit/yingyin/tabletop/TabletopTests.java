package edu.mit.yingyin.tabletop;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import edu.mit.yingyin.tabletop.models.BackgroundTest;
import edu.mit.yingyin.tabletop.models.CalibrationExampleTest;
import edu.mit.yingyin.tabletop.models.OpenNIDeviceTest;
import edu.mit.yingyin.tabletop.models.HandTrackingEngineTest;
import edu.mit.yingyin.tabletop.models.TableTest;

@RunWith(Suite.class)
@SuiteClasses({OpenNIDeviceTest.class,
               TableTest.class, EnvTest.class,
               CvUtilTest.class, BackgroundTest.class, 
               CalibrationExampleTest.class, JavaCVTest.class,
               HandTrackingEngineTest.class, 
               TableTest.class})

public class TabletopTests {}
