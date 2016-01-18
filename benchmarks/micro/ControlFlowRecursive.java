/*
 * Copyright (c) 2015, Linaro Limited. Ported to Java from:
 *  https://github.com/WebKit/webkit/blob/master/PerformanceTests/SunSpider/tests/sunspider-1.0.2/controlflow-recursive.js
 *
 * Description:     A control flow recursive micro benchmark case.
 */


// The Computer Language Shootout
// http://shootout.alioth.debian.org/
// contributed by Isaac Gouy

// http://benchmarksgame.alioth.debian.org/license.html  (BSD 3-clause license)

package benchmarks.micro;

public class ControlFlowRecursive {
  private int result = 0;
  private final int expected = 57775;

  private int ack(int m, int n) {
    if (m == 0) {
      return n + 1;
    }
    if (n == 0) {
      return ack(m - 1, 1);
    }
    return ack(m - 1, ack(m, n - 1));
  }

  private int fib(int n) {
    if (n < 2) {
      return 1;
    }
    return fib(n - 2) + fib(n - 1);
  }

  private int tak(int x, int y, int z) {
    if (y >= x) {
      return z;
    }
    return tak(tak(x - 1, y, z), tak(y - 1, z, x), tak(z - 1, x, y));
  }

  public void timeControlFlowRecursive(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      result = 0;
      for (int i = 3; i <= 5; i++) {
        result += ack(3, i);
        result += fib(17 + i);
        result += tak(3 * i + 3, 2 * i + 2, i + 1);
      }
    }
  }
  /**
   * Verify
   **/

  /**
   * Called by the framework to assert the benchmarks have done the right thing.
   **/
  public boolean verify() {
    return result == expected;
  }

  /**
   * *NOT* called by the framework by default, provided for direct use only.
   **/
  public static void main(String[] args) {
    ControlFlowRecursive obj = new ControlFlowRecursive();
    long before = System.currentTimeMillis();
    obj.timeControlFlowRecursive(500);

    long after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/ControlFlowRecursive: " + (after - before));
  }
}
