/*
 * Copyright (C) 2015 Linaro Limited. Ported to Java from:
 *  https://github.com/WebKit/webkit/blob/master/PerformanceTests/SunSpider/tests/sunspider-1.0.2/access-fannkuch.js
 *
 * Description:     Algorithm that performs array permutations.
 * Main Focus:      Array operations.
 *
 */

/* The Great Computer Language Shootout
   http://shootout.alioth.debian.org/
   contributed by Isaac Gouy */

// http://benchmarksgame.alioth.debian.org/license.html  (BSD 3-clause license)
// See NOTICE file for the license

package benchmarks.algorithm;

import java.lang.Integer;
import java.lang.System;

public class AccessFannkuch {
  private static final int ACCESS_FANNKUCH_EXPECTED = 22;

  private int fannkuch(int n) {
    int check = 0;
    int[] perm = new int[n];
    int[] perm1 = new int[n];
    int[] count = new int[n];
    int[] maxPerm = new int[n];
    int maxFlipsCount = 0;
    final int m = n - 1;

    for (int i = 0; i < n; i++) {
      perm1[i] = i;
    }
    int r = n;

    while (true) {
      // Write-out the first 30 permutations.
      if (check < 30) {
        String s = "";
        for (int i = 0; i < n; i++) {
          s += Integer.toString((perm1[i] + 1));
        }
        check++;
      }

      while (r != 1) {
        count[r - 1] = r;
        r--;
      }

      if (!(perm1[0] == 0 || perm1[m] == m)) {
        for (int i = 0; i < n; i++) {
          perm[i] = perm1[i];
        }

        int flipsCount = 0;
        int k;

        while (!((k = perm[0]) == 0)) {
          int k2 = ( k + 1) >> 1;
          for (int i = 0; i < k2; i++) {
            int temp = perm[i];
            perm[i] = perm[k - i];
            perm[k - i] = temp;
          }
          flipsCount++;
        }

        if (flipsCount > maxFlipsCount) {
          maxFlipsCount = flipsCount;
          for (int i = 0; i < n; i++) {
            maxPerm[i] = perm1[i];
          }
        }
      }

      while (true) {
        if (r == n) {
          return maxFlipsCount;
        }
        int perm0 = perm1[0];
        int i = 0;
        while (i < r) {
          int j = i + 1;
          perm1[i] = perm1[j];
          i = j;
        }
        perm1[r] = perm0;

        count[r] = count[r] - 1;
        if (count[r] > 0) {
          break;
        }
        r++;
      }
    }
  }

  public void timeAccessFannkuch(int iters) {
    for (int i = 0; i < iters; i++) {
      fannkuch(8);
    }
  }

  public boolean verify() {
    return fannkuch(8) == ACCESS_FANNKUCH_EXPECTED;
  }

  public static void main(String[] args) {
    AccessFannkuch obj = new AccessFannkuch();
    final long before = System.currentTimeMillis();
    obj.timeAccessFannkuch(109);
    final long after = System.currentTimeMillis();
    obj.verify();
    System.out.println("benchmarks/algorithm/AccessFankuch: " + (after - before));
  }
}

