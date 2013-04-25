package edu.mit.yingyin.util;

import static org.junit.Assert.*;

import java.io.File;
import java.util.logging.Logger;

import org.junit.Test;

public class FileUtilTest {
  private static final Logger logger = Logger.getLogger(
      FileUtilTest.class.getName());
  
  @Test
  public void testJoint() {
    String expected = "user" + File.separator + "mail";
    String actual = FileUtil.join("user", "mail");
    assertEquals(expected, FileUtil.join("user", "mail"));
    logger.info(actual);
  }

  @Test
  public void testBasename() {
    String fileName = "/path/file.ext";
    String actual = FileUtil.basename(fileName);
    assertEquals("file.ext", actual);
    
    fileName = "file.ext";
    assertEquals("file.ext", FileUtil.basename(fileName));
  }
  
  @Test
  public void testBasenameWithoutSuffix() {
    String fileName = "/path/file.ext";
    String actual = FileUtil.basename(fileName, ".ext");
    assertEquals("file", actual);
    
    fileName = "file.ext";
    assertEquals("file", FileUtil.basename(fileName, ".ext"));
  }
}
