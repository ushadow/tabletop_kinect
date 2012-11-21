package edu.mit.yingyin.tabletop;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import edu.mit.yingyin.tabletop.models.BackgroundTest;
import edu.mit.yingyin.tabletop.models.CalibrationExampleTest;
import edu.mit.yingyin.tabletop.models.FullOpenNIDeviceTest;
import edu.mit.yingyin.tabletop.models.HandAnalyzerTest;
import edu.mit.yingyin.tabletop.models.HandTrackingEngineTest;
import edu.mit.yingyin.tabletop.models.TableTest;

@RunWith(Suite.class)
@SuiteClasses({FullOpenNIDeviceTest.class,
               TableTest.class, HandAnalyzerTest.class, EnvTest.class,
               CvUtilTest.class, BackgroundTest.class, 
               CalibrationExampleTest.class, JavaCVTest.class,
               HandTrackingEngineTest.class, 
               TableTest.class})
public class AllTests {
}
