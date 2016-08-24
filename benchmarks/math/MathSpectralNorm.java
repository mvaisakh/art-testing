/*
 * Copyright (C) 2015 Linaro Limited. Ported from:
 *  https://github.com/WebKit/webkit/blob/master/PerformanceTests/SunSpider/tests/sunspider-1.0.2/math-spectral-norm.js
 *
 * Description:     Martrix spectral norm calculation.
 * Main Focus:      Floating-Point operations.
 *
 */

// The Great Computer Language Shootout
// http://shootout.alioth.debian.org/
//
// contributed by Ian Osgood

// http://benchmarksgame.alioth.debian.org/license.html  (BSD 3-clause license)
// See NOTICE file for license.

package benchmarks.math;

import java.lang.Math;
import java.lang.System;

public class MathSpectralNorm {
  private static final double SPECTRAL_NORM_EXPECTED = 5.086694231303284d;

  private double aa(int i, int j) {
    return 1.0 / ((i + j) * (i + j + 1) / 2 + i + 1);
  }

  private void au(double[] u, double[] v) {
    for (int i = 0; i < u.length; ++i) {
      double t = 0.0;
      for (int j = 0; j < u.length; ++j) {
        t += aa(i, j) * u[j];
      }
      v[i] = t;
    }
  }

  private void atu(double[] u, double[] v) {
    for (int i = 0; i < u.length; ++i) {
      double t = 0.0;
      for (int j = 0; j < u.length; ++j) {
        t += aa(j, i) * u[j];
      }
      v[i] = t;
    }
  }

  private void atAu(double[] u, double[] v, double[] w) {
    au(u, w);
    atu(w, v);
  }

  double spectralNorm(int n) {
    double[] u = new double[n];
    double[] v = new double[n];
    double[] w = new double[n];
    double vv;
    double vBv;

    vv = vBv = 0.0;
    for (int i = 0; i < n; ++i) {
      u[i] = 1.0;
    }

    for (int i = 0; i < 10; ++i) {
      atAu(u, v, w);
      atAu(v, u, w);
    }

    for (int i = 0; i < n; ++i) {
      vBv += u[i] * v[i];
      vv += v[i] * v[i];
    }
    return Math.sqrt(vBv / vv);
  }

  public void timeMathSpectralNorm(int iters) {
    for (int i = 0; i < iters; i++) {
      for (int j = 6; j <= 48; j *= 2) {
        spectralNorm(j);
      }
    }
  }

  public boolean verify() {
    double spectralNormOutput = 0.0;
    for (int i = 6; i <= 48; i *= 2) {
      spectralNormOutput += spectralNorm(i);
    }
    return spectralNormOutput == SPECTRAL_NORM_EXPECTED;
  }

  public static void main(String[] argv) {
    MathSpectralNorm obj = new MathSpectralNorm();
    final long before = System.currentTimeMillis();
    obj.timeMathSpectralNorm(360);
    final long after = System.currentTimeMillis();
    obj.verify();
    System.out.println("benchmarks/math/MathSpectralNorm: " + (after - before));
  }
}
