package edu.mit.yingyin.util;

/**
 * Value confidence pair for probabilistic reasoning.
 * @author yingyin
 *
 * @param <T>
 */
public class ValConfidencePair<T> {
  public T value;
  public float confidence;

  public ValConfidencePair(T v, float c) {
    value = v;
    confidence = c;
  }
}
