package edu.mit.yingyin.util;

public class Matrix {
  /**
   * Rotates a n x n matrix anticlockwise by 90 degrees.
   *   
   * @param matrix the matrix in 1-dimentional array to be rotated.
   * @param n size of the square matrix.
   */
  public static void rot90(int[] matrix, int n) {
    for (int i = 0; i < n / 2; i++)
      for (int j = i; j < n - i - 1; j++) 
        cyclicRoll(matrix, i * n + j, j * n + n - 1 - i, 
                   (n - 1 - i) * n + n - 1 - j, (n - 1 - j) * n + i);
  }
  
  private static void cyclicRoll(int[] array, int a, int b, int c, int d) {
    int temp = array[a];
    array[a] = array[b];
    array[b] = array[c];
    array[c] = array[d];
    array[d] = temp;
  }
}
