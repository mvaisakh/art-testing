/*
 * Copyright (C) 2015 Linaro Limited. Ported to Java from:
 *  https://github.com/WebKit/webkit/blob/master/PerformanceTests/SunSpider/tests/sunspider-1.0.2/bitops-nsieve-bits.js
 *
 * Description:     Bitops prime number sieve, used for finding prime numbers.
 * Main Focus:      Bitwise operations.
 *
 */

// The Great Computer Language Shootout
//  http://shootout.alioth.debian.org/
//
//  Contributed by Ian Osgood

// http://benchmarksgame.alioth.debian.org/license.html  (BSD 3-clause license)
// See NOTICE file for the license

package benchmarks.algorithm;

import java.lang.Integer;
import java.lang.System;

public class BitopsNSieve {
  private static final int BITOPS_NSIEVE_EXPECTED = -1431657430;

  // As per the original, this function is not used.
  private String pad(int n, int width) {
    String s = Integer.toString(n);
    while (s.length() < width) {
      s = ' ' + s;
    }
    return s;
  }

  private void primes(int[] isPrime, int n) {
    // As per the original, this variable can be optimised out.
    int count = 0;
    final int m = 10000 << n;
    final int size = m + 31 >> 5;

    for (int i = 0; i < size; i++) {
      isPrime[i] = 0xffffffff;
    }

    for (int i = 2; i < m; i++) {
      if ((isPrime[i >> 5] & 1 << (i & 31)) != 0) {
        for (int j = i + 1; j < m; j += i) {
          isPrime[j >> 5] &= ~ (1 << (j & 31));
        }
        count++;
      }
    }
  }

  private int[] sieve() {
    int[] isPrime = null;
    // As per the original, this loop will have just one iteration.
    for (int i = 4; i <= 4; i++) {
      isPrime = new int[(10000 << i) + 31 >> 5];
      primes(isPrime, i);
    }
    return isPrime;
  }

  public void timeBitopsNSieve(int iters) {
    for (int i = 0; i < iters; i++) {
      sieve();
    }
  }

  public boolean verify() {
    int output = 0;
    int[] result = sieve();

    for (int i = 0; i < result.length; i++) {
      output += result[i];
    }
    return output == BITOPS_NSIEVE_EXPECTED;
  }

  public static void main(String[] argv) {
    BitopsNSieve obj = new BitopsNSieve();
    final long before = System.currentTimeMillis();
    obj.timeBitopsNSieve(189);
    final long after = System.currentTimeMillis();
    obj.verify();
    System.out.println("benchmarks/algorithm/BitopsNSieve: " + (after - before));
  }
}
