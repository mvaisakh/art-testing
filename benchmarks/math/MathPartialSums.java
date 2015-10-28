/*
 * Copyright (C) 2015 Linaro Limited. All rights received.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* Ported from:
 *  webkit/PerformanceTests/SunSpider/tests/sunspider-1.0.2/math-partial-sums.js
 *
 * The Computer Language Shootout
 * http://benchmarksgame.alioth.debian.org/
 *
 * contributed by Isaac Gouy
 */

/*
 * Description:     Partial sum calculation of a series using Math().pow(),
 *                  Math.sin() and Math.cos().
 * Main Focus:      Floating-Point operations, Math.[pow(), sin(), cos()].
 *
 */

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

  public static void main(String argv[]) {
    MathPartialSums obj = new MathPartialSums();
    final long before = System.currentTimeMillis();
    obj.timeMathPartialSums(96);
    final long after = System.currentTimeMillis();
    obj.verify();
    System.out.println("MathPartial: " + (after - before));
  }
}




