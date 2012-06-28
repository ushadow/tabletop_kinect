package edu.mit.yingyin.util;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CommandLineOptions {
  private static Options options = new Options();
  private static CommandLineParser parser = new GnuParser();
  private static CommandLine line;
  
  public static void addOption(Option option) {
    options.addOption(option);
  }
  
  public static void parse(String[] args) {
    try {
      line = parser.parse(options, args);
    } catch (ParseException e) {
      System.err.println(e.getMessage());
    }
  }
  
  public static String getOptionValue(String option, String defaultValue) {
    if (line == null)
      return defaultValue;
    return line.getOptionValue(option, defaultValue);
  }
  
  public static boolean hasOption(String option) {
    if (line == null)
      return false;
    return line.hasOption(option);
  }
  
  private CommandLineOptions() {}
}
