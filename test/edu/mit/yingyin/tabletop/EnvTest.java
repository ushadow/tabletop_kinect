package edu.mit.yingyin.tabletop;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EnvTest {
  @Test
  public void testLibraryPath() {
    String libraryPath = System.getProperty("java.library.path");
    System.out.println("java.library.path = " + libraryPath);
    assertTrue(libraryPath.contains("/usr/lib"));
  }
}
