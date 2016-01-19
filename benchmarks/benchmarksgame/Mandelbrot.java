/*
 * This benchmark has been ported from "The Computer Language Benchmarks Game" suite and slightly
 * modified to fit the benchmarking framework.
 *
 * The original file is `mandelbrot/mandelbrot.java` from the archive
 * available at
 * http://benchmarksgame.alioth.debian.org/download/benchmarksgame-sourcecode.zip.
 * See LICENSE file in the same folder (BSD 3-clause)
 *
 * The Computer Language Benchmarks Game
 * http://benchmarksgame.alioth.debian.org/
 *
 * contributed by Stefan Krause
 * slightly modified by Chad Whipkey
 */

/*
 * Description:     Generate Mandelbrot set portable bitmap file.
 * Main Focus:      TODO
 *
 */

package benchmarks.benchmarksgame;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.System;

public class Mandelbrot {
  private static final int PREDEFINED_SIZE = 400;

       // CHECKSTYLE.OFF: .*
       private static final int BUFFER_SIZE = 8192;

       public Mandelbrot() {
         this.size = PREDEFINED_SIZE;
         fac = 2.0 / size;
         shift = size % 8 == 0 ? 0 : (8- size % 8);
      }
      final int size;
      final byte [] buf = new byte[BUFFER_SIZE];
      int bufLen = 0;
      final double fac;
      final int shift;

      public void compute()
      {
         for (int y = 0; y<size; y++)
            computeRow(y);
      }

      private void computeRow(int y)
      {
         int bits = 0;

         final double Ci = (y*fac - 1.0);
          final byte[] bufLocal = buf;
          for (int x = 0; x<size;x++) {
            double Zr = 0.0;
            double Zi = 0.0;
            double Cr = (x*fac - 1.5);
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

            if (x%8 == 7) {
                bufLocal[bufLen++] = (byte) bits;
                if ( bufLen == BUFFER_SIZE) {
                    bufLen = 0;
                }
               bits = 0;
            }
         }
         if (shift!=0) {
            bits = bits << shift;
            bufLocal[bufLen++] = (byte) bits;
            if ( bufLen == BUFFER_SIZE) {
                bufLen = 0;
            }
         }
      }
      // CHECKSTYLE.OFF: .*

  public void timeMandelbrot(int iters) {
    for (int i = 0; i < iters; i++) {
      bufLen = 0;
      compute();
    }
  }

  public boolean verify() {
    bufLen = 0;
    compute();
    return 3616 == bufLen;
  }

  public static void main(String[] args) {
    Mandelbrot obj = new Mandelbrot();

    final long before = System.currentTimeMillis();
    obj.timeMandelbrot(75);
    final long after = System.currentTimeMillis();

    obj.verify();
    System.out.println("benchmarks/benchmarksgame/Mandelbrot: " + (after - before));
  }
}
