/*
 * Created on Sep 22, 2006
 */
package edu.mit.yingyin.util;

/**
 * A simple timer class: tic toc tocln ...
 */
public class Timer {

  protected long t_tic, t_toc;
  protected long div = 1000000L;
  protected String unit = "ms"; 
  
  /**
   * Constructor. Invokes tic.
   */
  public Timer() {
    tic();
  }
  
  public Timer(boolean useMicro) {
    if (useMicro) {
      div = 1000L;
      unit = "us";
    }
    tic();
  }

  /**
   * Sets the start time.
   */
  public void tic() {
    t_tic = currentTimeMillis();
  }

  /**
   * Same as "tocln(what); tic();". 
   * Useful for profiling sections of code.
   * @return printed result
   */
  public String ticln(String what) {
    String result = tocln(what);
    tic();
    return result;
  }

  /**
   * Returns the time elapsed (in ms) since tic() (or Timer()) 
   * was last invoked.
   */
  public String toc() {
    t_toc = currentTimeMillis();
    String result = ("" + (t_toc - t_tic));
    return result;
  }

  /** 
   * Returns the time elapsed (in ms) since tic() (or Timer()) was
   * last invoked. Example: tic(), getToc(), getToc(), ...
   */
  public long getToc() {
    t_toc = currentTimeMillis();
    return (t_toc - t_tic);
  }

  /**
   * Like toc except that the time elapsed is printed to standard out
   * along with the provided message "what" using println.
   * @return Printed result.
   */
  public String tocln(String what) {
    String s = getTocln(what);
    System.out.println(s);
    return s;
  }
  
  public String toclntic(String what) {
    String s = getTocln(what);
    System.out.println(s);
    tic();
    return s;
  }

  public String getTocln(String what) {
    String s = toc() + " " + unit + " : " + what;
    return s;
  }

  /** Time in millis as provided by java or best loaded clock */
  protected long currentTimeMillis() {
    return System.nanoTime() / div;
  }
}
