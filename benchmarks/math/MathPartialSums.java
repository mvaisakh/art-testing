/*
 * Copyright (C) 2015 Linaro Limited. Ported to Java from:
 *  https://github.com/WebKit/webkit/blob/master/PerformanceTests/SunSpider/tests/sunspider-1.0.2/math-partial-sums.js
 *
 * Description:     Partial sum calculation of a series using Math().pow(),
 *                  Math.sin() and Math.cos().
 * Main Focus:      Floating-Point operations, Math.[pow(), sin(), cos()].
 *
 */

// The Computer Language Shootout
// http://shootout.alioth.debian.org/
// contributed by Isaac Gouy

// http://benchmarksgame.alioth.debian.org/license.html  (BSD 3-clause license)
// See NOTICE file for license.

package benchmarks.math;

import java.lang.Exception;
import java.lang.Math;
import java.lang.System;

public class MathPartialSums {
  private static final double PARTIAL_SUMS_EXPECTED = 33.97380678948515;

  private static double partialSums(int n) {
    double a1;
    double a2;
    double a3;
    double a4;
    double a5;
    double a6;
    double a7;
    double a8;
    double a9;
    double k2;
    double k3;
    double sk;
    double ck;
    double twothirds = 2.0 / 3.0;
    double alt = -1.0;

    a1 = a2 = a3 = a4 = a5 = a6 = a7 = a8 = a9 = 0.0;
    for (int k = 1; k <= n; k++) {
      k2 = k * k;
      k3 = k2 * k;
      sk = Math.sin(k);
      ck = Math.cos(k);
      alt = -alt;

      a1 += Math.pow(twothirds, k - 1);
      a2 += Math.pow(k, -0.5);
      a3 += 1.0 / (k * (k + 1.0));
      a4 += 1.0 / (k3 * sk * sk);
      a5 += 1.0 / (k3 * ck * ck);
      a6 += 1.0 / k;
      a7 += 1.0 / k2;
      a8 += alt / k;
      a9 += alt / (2 * k - 1);
    }
    return a6 + a7 + a8 + a9;
  }

  public void timeMathPartialSums(int iters) {
    for (int i = 0; i < iters; i++) {
      for (int j = 1024; j <= 5000; j *= 2) {
        partialSums(j);
      }
    }
  }

  public boolean verify() {
    double partialSumsOutput = 0.0;

    for (int i = 1024; i <= 5000; i *= 2) {
      partialSumsOutput += partialSums(i);
    }
    return partialSumsOutput == PARTIAL_SUMS_EXPECTED;
  }

  public static void main(String[] argv) {
    MathPartialSums obj = new MathPartialSums();
    final long before = System.currentTimeMillis();
    obj.timeMathPartialSums(140);
    final long after = System.currentTimeMillis();
    obj.verify();
    System.out.println("benchmarks/math/MathPartial: " + (after - before));
  }
}




