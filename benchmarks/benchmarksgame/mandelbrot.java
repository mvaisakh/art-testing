/*
 * This benchmark has been ported from "The Computer Language Benchmarks Game" suite and slightly
 * modified to fit the benchmarking framework.
 *
 * The original file:
 * https://benchmarksgame-team.pages.debian.net/benchmarksgame/program/mandelbrot-java-1.html
 *
 * The Computer Language Benchmarks Game
 * https://salsa.debian.org/benchmarksgame-team/benchmarksgame/
 *
 * contributed by Stefan Krause
 * slightly modified by Chad Whipkey
 *
 * LICENSE: 3-Clause BSD
 * https://benchmarksgame-team.pages.debian.net/benchmarksgame/license.html
 */

/*
 * Description:     Generate Mandelbrot set portable bitmap file.
 * Main Focus:      TODO
 *
 */

package benchmarks.benchmarksgame;


public class mandelbrot {
  private static final int PREDEFINED_SIZE = 200;

  private static final int BUFFER_SIZE = 8192;

  public mandelbrot() {
    this.size = PREDEFINED_SIZE;
    fac = 2.0 / size;
    shift = size % 8 == 0 ? 0 : (8 - size % 8);
  }

  final int size;
  final byte[] buf = new byte[BUFFER_SIZE];
  int bufLen = 0;
  final double fac;
  final int shift;

  public void compute() {
    for (int y = 0; y < size; y++) computeRow(y);
  }

  private void computeRow(int y) {
    int bits = 0;

    final double Ci = (y * fac - 1.0);
    final byte[] bufLocal = buf;
    for (int x = 0; x < size; x++) {
      double Zr = 0.0;
      double Zi = 0.0;
      double Cr = (x * fac - 1.5);
      int i = 50;
      double ZrN = 0;
      double ZiN = 0;
      do {
        Zi = 2.0 * Zr * Zi + Ci;
        Zr = ZrN - ZiN + Cr;
        ZiN = Zi * Zi;
        ZrN = Zr * Zr;
      } while (!(ZiN + ZrN > 4.0) && --i > 0);

      bits = bits << 1;
      if (i == 0) bits++;

      if (x % 8 == 7) {
        bufLocal[bufLen++] = (byte) bits;
        if (bufLen == BUFFER_SIZE) {
          bufLen = 0;
        }
        bits = 0;
      }
    }
    if (shift != 0) {
      bits = bits << shift;
      bufLocal[bufLen++] = (byte) bits;
      if (bufLen == BUFFER_SIZE) {
        bufLen = 0;
      }
    }
  }

  public void timeMandelbrot(int iters) {
    for (int i = 0; i < iters; i++) {
      bufLen = 0;
      compute();
    }
  }

  public boolean verifyMandelbrot() {
    bufLen = 0;
    compute();

    int expected = 5000;
    int found = bufLen;
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }

    return true;
  }

  public static void main(String[] args) {
    int rc = 0;
    mandelbrot obj = new mandelbrot();

    final long before = System.currentTimeMillis();
    obj.timeMandelbrot(65);
    final long after = System.currentTimeMillis();

    if (!obj.verifyMandelbrot()) {
      rc++;
    }
    System.out.println("benchmarks/benchmarksgame/mandelbrot: " + (after - before));
    System.exit(rc);
  }
}
