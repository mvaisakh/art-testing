/*
 * Copyright (C) 2015 Linaro Limited. Ported to Java from:
 *  https://github.com/WebKit/webkit/blob/master/PerformanceTests/SunSpider/tests/sunspider-1.0.2/access-nsieve.js
 *
 */

// The Great Computer Language Shootout
//  http://shootout.alioth.debian.org/
//
//  modified by Isaac Gouy

// http://benchmarksgame.alioth.debian.org/license.html  (BSD 3-clause license)

package benchmarks.algorithm;

import java.lang.System;


public class NSieve {
  /* Expected result for the standard benchmark setup */
  private static final int EXPECTED = 14302;
  /* Number of repeats (internal, not benchmark iterations) */
  private static final int NUM_SIEVES = 4;
  /* Array of flags - big enough for all standard test scenarios */
  private static boolean[] flags = new boolean[80001];
  private static int[] results = new int[NUM_SIEVES];

  private int nsieve(int m) {
    for (int i = 2; i <= m; i++) {
      flags[i] = true;
    }

    int count = 0;
    for (int i = 2; i <= m; i++) {
      if (flags[i]) {
        for (int k = i + i; k <= m; k += i) {
          flags[k] = false;
        }
        count++;
      }
    }
    return count;
  }

  /**
   * Find prime numbers in three sizes of pool, four times over
   * Repeat over number of iterations set by framework
   **/
  public void timeNSieveAccess(int iterations) {
    for (int iter = 0; iter < iterations; iter++) {
      for (int i = 0; i < NUM_SIEVES; i++) {
        int sum = 0;
        for (int o = 1; o <= 3; o++) {
          int m = (1 << o) * 10000;
          sum += nsieve(m);
        }
        results[i] = sum;
      }
    }
  }

  /**
   * Called by the framework to assert the benchmarks have done the right thing.
   **/
  public boolean verify() {
    for (int i = 0; i < NUM_SIEVES; i++) {
      if (results[i] != EXPECTED) {
        return false;
      }
    }
    return true;
  }

  /**
   * *NOT* called by the framework by default, provided for direct use only.
   **/
  public static void main(String[] args) {
    NSieve obj = new NSieve();
    long before = System.currentTimeMillis();

    obj.timeNSieveAccess(100);
    long after = System.currentTimeMillis();
    System.out.println("benchmarks/algorithm/NSieve.NSieveAccess: " + (after - before));
  }
}
