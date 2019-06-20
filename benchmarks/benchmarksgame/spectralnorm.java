/*
 * This benchmark has been ported from "The Computer Language Benchmarks Game" suite and slightly
 * modified to fit the benchmarking framework.
 *
 * The original file:
 * https://benchmarksgame-team.pages.debian.net/benchmarksgame/program/spectralnorm-java-1.html
 *
 * The Computer Language Benchmarks Game
 * https://salsa.debian.org/benchmarksgame-team/benchmarksgame/
 *
 * contributed by Java novice Jarkko Miettinen
 * modified ~3 lines of the original C#-version
 * by Isaac Gouy
 *
 * LICENSE: 3-Clause BSD
 * https://benchmarksgame-team.pages.debian.net/benchmarksgame/license.html
 */

/*
 * Description:     Eigenvalue using the power method.
 * Main Focus:      TODO
 *
 */

package benchmarks.benchmarksgame;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class spectralnorm {

  private static final NumberFormat formatter = new DecimalFormat("#.000000000");

  private final double Approximate(int n) {
    // create unit vector
    double[] u = new double[n];
    for (int i = 0; i < n; i++) u[i] = 1;

    // 20 steps of the power method
    double[] v = new double[n];
    for (int i = 0; i < n; i++) v[i] = 0;

    for (int i = 0; i < 10; i++) {
      MultiplyAtAv(n, u, v);
      MultiplyAtAv(n, v, u);
    }

    // B=AtA         A multiplied by A transposed
    // v.Bv /(v.v)   eigenvalue of v
    double vBv = 0, vv = 0;
    for (int i = 0; i < n; i++) {
      vBv += u[i] * v[i];
      vv += v[i] * v[i];
    }

    return Math.sqrt(vBv / vv);
  }

  /* return element i,j of infinite matrix A */
  private final double A(int i, int j) {
    return 1.0 / ((i + j) * (i + j + 1) / 2 + i + 1);
  }

  /* multiply vector v by matrix A */
  private final void MultiplyAv(int n, double[] v, double[] Av) {
    for (int i = 0; i < n; i++) {
      Av[i] = 0;
      for (int j = 0; j < n; j++) Av[i] += A(i, j) * v[j];
    }
  }

  /* multiply vector v by matrix A transposed */
  private final void MultiplyAtv(int n, double[] v, double[] Atv) {
    for (int i = 0; i < n; i++) {
      Atv[i] = 0;
      for (int j = 0; j < n; j++) Atv[i] += A(j, i) * v[j];
    }
  }

  /* multiply vector v by matrix A and then by matrix A transposed */
  private final void MultiplyAtAv(int n, double[] v, double[] AtAv) {
    double[] u = new double[n];
    MultiplyAv(n, v, u);
    MultiplyAtv(n, u, AtAv);
  }

  private static final int APPROXIMATE_N = 100;

  public boolean verifySpectralNorm() {
    double expected = 1.2742199912349306;
    double found = Approximate(APPROXIMATE_N);

    if (Math.abs(expected - found) > 0.000000001) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public void timeSpectralNorm(int iters) {
    for (int j = 0; j < iters; j++) {
      Approximate(APPROXIMATE_N);
    }
  }

  public static void main(String[] args) {
    int rc = 0;
    spectralnorm obj = new spectralnorm();

    final long before = System.currentTimeMillis();
    obj.timeSpectralNorm(100);
    final long after = System.currentTimeMillis();

    if (!obj.verifySpectralNorm()) {
      rc++;
    }
    System.out.println("benchmarks/benchmarksgame/spectralnorm: " + (after - before));
    System.exit(rc);
  }
}
