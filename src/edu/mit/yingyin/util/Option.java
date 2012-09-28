package edu.mit.yingyin.util;

import java.lang.UnsupportedOperationException;

/**
 * Option type similar to that in Scala. Used as return type from method.
 * 
 * @author yingyin
 *
 * @param <T> type of the optional value.
 */
public interface Option<T> {
  
  public static final class Some<T> implements Option<T> {
    private final T value;
    
    public Some(T value) {
      // Setting Some to null, nullifies the purpose of Some/None
      if (value == null) {
        throw new NullPointerException("Some value was null, use None instead");
      }
      
      this.value = value;
    }
    
    @Override
    public T value() { return value; }
    
    @Override
    public boolean isSome() { return true; }
    
    @Override
    public boolean isNone() { return false; }
  }
  
  public static final class None<T> implements Option<T> {
    @Override
    public T value() {
        throw new UnsupportedOperationException("There is no value");
    }

    @Override
    public boolean isSome() { return false; }

    @Override
    public boolean isNone() { return true; }
  }

  /**
   * Could contain the value if Some, but not if None.
   * @return the value is Some, otherwise throws an <code>
   *    UnsupportedOperationException</code>.
   */
  public T value();
  public boolean isSome();
  public boolean isNone();
}
