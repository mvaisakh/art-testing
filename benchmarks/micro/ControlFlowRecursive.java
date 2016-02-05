/*
 * Copyright (c) 2015, Linaro Limited. Ported to Java from:
 *  https://github.com/WebKit/webkit/blob/master/PerformanceTests/SunSpider/tests/sunspider-1.0.2/controlflow-recursive.js
 * and added Tarai.
 *
 * Description:     A control flow recursive micro benchmark case.
 */


// The Computer Language Shootout
// http://shootout.alioth.debian.org/
// contributed by Isaac Gouy

// http://benchmarksgame.alioth.debian.org/license.html  (BSD 3-clause license)
// See NOTICE file for license.

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

  private int tarai(int x, int y, int z) {
    if (y >= x) {
      return y;
    }
    return tarai(tarai(x - 1, y, z), tarai(y - 1, z, x), tarai(z - 1, x, y));
  }

  public void timeTak(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 3; i <= 5; i++) {
        tak(3 * i + 3, 2 * i + 2, i + 1);
      }
    }
  }

  public void timeTarai(int iterations) {
    for (int iter = 0; iter < iterations; ++iter) {
      for (int i = 3; i <= 5; i++) {
        tarai(3 * i + 3, 2 * i + 2, i + 1);
      }
    }
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

  public boolean verifyTakTarai() {
    int i = 5;
    int expected = 25;
    int found = tak(3 * i + 3, 2 * i + 2, i + 1) + tarai(3 * i + 3, 2 * i + 2, i + 1);

    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  /**
   * Called by the framework to assert the benchmarks have done the right thing.
   **/
  public boolean verifyControlFlowRecursive() {
    return result == expected;
  }

  /**
   * *NOT* called by the framework by default, provided for direct use only.
   **/
  public static void main(String[] args) {
    int rc = 0;
    long start;
    long end;
    ControlFlowRecursive obj = new ControlFlowRecursive();

    start = System.currentTimeMillis();
    obj.timeControlFlowRecursive(1000);
    end = System.currentTimeMillis();
    System.out.println(
        "benchmarks/micro/ControlFlowRecursive.ControlFlowRecursive: " + (end - start));

    start = System.currentTimeMillis();
    obj.timeTak(2000);
    end = System.currentTimeMillis();
    System.out.println("benchmarks/micro/ControlFlowRecursive.Tak: " + (end - start));

    start = System.currentTimeMillis();
    obj.timeTarai(20);
    end = System.currentTimeMillis();
    System.out.println("benchmarks/micro/ControlFlowRecursive.Tarai: " + (end - start));

    if (!obj.verifyTakTarai() || !obj.verifyControlFlowRecursive()) {
      rc++;
    }
    System.exit(rc);
  }
}
