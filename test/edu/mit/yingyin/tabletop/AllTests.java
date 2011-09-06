package edu.mit.yingyin.tabletop;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({FullOpenNIDeviceTest.class, PartialOpenNIDeviceTest.class,
               TableTest.class, HandAnalyzerTest.class, EnvTest.class})
public class AllTests {
}
