/*
 * This benchmark has been ported from "The Computer Language Benchmarks Game" suite and modified
 * to fit the benchmarking framework.
 * The original benchmarks printed long strings to the stdout. This print was overrided to do
 * nothing to fit the framework. These action can cause difference in behaviour of the original and
 * changed benchmarks; it hasn't been estimated yet.
 *
 * The original file:
 * https://salsa.debian.org/benchmarksgame-team/archive-alioth-benchmarksgame/blob/master/contributed-source-code/benchmarksgame/fastaredux/fastaredux.java-3.java
 *
 * modified by Enotus
 *
 * LICENSE: 3-Clause BSD
 * https://salsa.debian.org/benchmarksgame-team/archive-alioth-benchmarksgame/blob/master/contributed-source-code/benchmarksgame/fastaredux/LICENSE
 *
 */

/*
 * Description:     Generate and write random DNA sequences.
 * Main Focus:      TODO
 *
 */

package benchmarks.benchmarksgame;

import java.io.*;

// CHECKSTYLE.OFF: TypeName
public class fastaredux {
// CHECKSTYLE.ON: TypeName

  static final int LINE_LENGTH = 60;
  static final int OUT_BUFFER_SIZE = 256 * 1024;
  static final int LOOKUP_SIZE = 4 * 1024;
  static final double LOOKUP_SCALE = LOOKUP_SIZE - 1;

  static final class Freq {
    byte c;
    double p;

    Freq(char cc, double pp) {
      c = (byte) cc;
      p = pp;
    }
  }

  static final String ALU =
      "GGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGG"
          + "GAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGA"
          + "CCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAAT"
          + "ACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCA"
          + "GCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGG"
          + "AGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCC"
          + "AGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAA";
  static final Freq[] IUB = {
    new Freq('a', 0.27),
    new Freq('c', 0.12),
    new Freq('g', 0.12),
    new Freq('t', 0.27),
    new Freq('B', 0.02),
    new Freq('D', 0.02),
    new Freq('H', 0.02),
    new Freq('K', 0.02),
    new Freq('M', 0.02),
    new Freq('N', 0.02),
    new Freq('R', 0.02),
    new Freq('S', 0.02),
    new Freq('V', 0.02),
    new Freq('W', 0.02),
    new Freq('Y', 0.02)
  };
  static final Freq[] HomoSapiens = {
    new Freq('a', 0.3029549426680),
    new Freq('c', 0.1979883004921),
    new Freq('g', 0.1975473066391),
    new Freq('t', 0.3015094502008)
  };

  static void sumAndScale(Freq[] a) {
    double p = 0;
    for (int i = 0; i < a.length; i++) a[i].p = (p += a[i].p) * LOOKUP_SCALE;
    a[a.length - 1].p = LOOKUP_SCALE;
  }

  static final class Random {

    static final int IM = 139968;
    static final int IA = 3877;
    static final int IC = 29573;
    static final double SCALE = LOOKUP_SCALE / IM;
    static int last = 42;

    static double next() {
      return SCALE * (last = (last * IA + IC) % IM);
    }
  }

  static final class Out {

    static byte[] buf = new byte[OUT_BUFFER_SIZE];
    static final int lim = OUT_BUFFER_SIZE - 2 * LINE_LENGTH - 1;
    static int ct = 0;
    static OutputStream stream;

    static void checkFlush() throws IOException {
      if (ct >= lim) {
        stream.write(buf, 0, ct);
        ct = 0;
      }
    }

    static void close() throws IOException {
      stream.write(buf, 0, ct);
      ct = 0;
      stream.close();
    }
  }

  static final class RandomFasta {

    static final Freq[] lookup = new Freq[LOOKUP_SIZE];

    static void makeLookup(Freq[] a) {
      for (int i = 0, j = 0; i < LOOKUP_SIZE; i++) {
        while (a[j].p < i) j++;
        lookup[i] = a[j];
      }
    }

    static void addLine(int bytes) throws IOException {
      Out.checkFlush();
      int lct = Out.ct;
      while (lct < Out.ct + bytes) {
        double r = Random.next(); // Problematic for AOT! See fastaredux_revised.java.
        int ai = (int) r;
        while (lookup[ai].p < r) ai++;
        Out.buf[lct++] = lookup[ai].c;
      }
      Out.buf[lct++] = (byte) '\n';
      Out.ct = lct;
    }

    static void make(String desc, Freq[] a, int n) throws IOException {
      makeLookup(a);

      System.arraycopy(desc.getBytes(), 0, Out.buf, Out.ct, desc.length());
      Out.ct += desc.length();

      while (n > 0) {
        int bytes = Math.min(LINE_LENGTH, n);
        addLine(bytes);
        n -= bytes;
      }
    }
  }

  static final class RepeatFasta {

    static void make(String desc, byte[] alu, int n) throws IOException {
      System.arraycopy(desc.getBytes(), 0, Out.buf, Out.ct, desc.length());
      Out.ct += desc.length();

      byte[] buf = new byte[alu.length + LINE_LENGTH];
      for (int i = 0; i < buf.length; i += alu.length) {
        System.arraycopy(alu, 0, buf, i, Math.min(alu.length, buf.length - i));
      }

      int pos = 0;
      while (n > 0) {
        int bytes = Math.min(LINE_LENGTH, n);
        Out.checkFlush();
        System.arraycopy(buf, pos, Out.buf, Out.ct, bytes);
        Out.ct += bytes;
        Out.buf[Out.ct++] = (byte) '\n';
        pos = (pos + bytes) % alu.length;
        n -= bytes;
      }
    }
  }

  private void old_main() throws IOException {
    int n = 1000;

    RepeatFasta.make(">ONE Homo sapiens alu\n", ALU.getBytes(), n * 2);
    RandomFasta.make(">TWO IUB ambiguity codes\n", IUB, n * 3);
    RandomFasta.make(">THREE Homo sapiens frequency\n", HomoSapiens, n * 5);
  }

  /** Writes to nowhere */
  public class NullOutputStream extends OutputStream {
    @Override
    public void write(int b) throws IOException {}
  }

  public void timeFastaRedux(int iters) throws IOException {
    sumAndScale(IUB);
    sumAndScale(HomoSapiens);

    Out.stream = new NullOutputStream();

    for (int i = 0; i < iters; i++) {
      old_main();
    }
  }

  static final int VERIFY_MAGIC_NUMBER = 25;

  public boolean verifyFastaRedux() throws IOException {
    int n = VERIFY_MAGIC_NUMBER;
    Out.buf = new byte[OUT_BUFFER_SIZE];
    sumAndScale(IUB);
    sumAndScale(HomoSapiens);

    RepeatFasta.make(">ONE Homo sapiens alu\n", ALU.getBytes(), n * 2);
    RandomFasta.make(">TWO IUB ambiguity codes\n", IUB, n * 3);
    RandomFasta.make(">THREE Homo sapiens frequency\n", HomoSapiens, n * 5);

    int expected = 0;
    int found = Out.buf[VERIFY_MAGIC_NUMBER - 1];
    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }

    return true;
  }

  public static void main(String[] args) throws IOException {
    int rc = 0;
    fastaredux obj = new fastaredux();

    final long before = System.currentTimeMillis();
    obj.timeFastaRedux(1700);
    final long after = System.currentTimeMillis();

    if (!obj.verifyFastaRedux()) {
      rc++;
    }
    System.out.println("benchmarks/benchmarksgame/fastaredux: " + (after - before));
    System.exit(rc);
  }
}
