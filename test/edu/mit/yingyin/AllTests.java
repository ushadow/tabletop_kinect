package edu.mit.yingyin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import edu.mit.yingyin.calib.CalibModelTest;
import edu.mit.yingyin.tabletop.TabletopTests;
import edu.mit.yingyin.util.UtilTests;

@RunWith(Suite.class)
@SuiteClasses({UtilTests.class, TabletopTests.class, CalibModelTest.class})

public class AllTests {}
