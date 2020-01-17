/*
 * This benchmark has been ported from "The Computer Language Benchmarks Game" suite and modified
 * to fit the benchmarking framework.
 * The original benchmarks printed long strings to the stdout. This print was
 * replaced with prints to NullOutputStream. These action can cause difference
 * in behaviour of the original and changed benchmarks;
 * it hasn't been estimated yet.
 *
 * The original file:
 * https://benchmarksgame-team.pages.debian.net/benchmarksgame/program/fasta-java-2.html
 *
 * The Computer Language Benchmarks Game
 * https://salsa.debian.org/benchmarksgame-team/benchmarksgame/
 *
 * modified by Mehmet D. AKIN
 *
 * LICENSE: 3-Clause BSD
 * https://benchmarksgame-team.pages.debian.net/benchmarksgame/license.html
 */

/*
 * Description:     Generate and write random DNA sequences.
 * Main Focus:      TODO
 *
 */

package benchmarks.benchmarksgame;

import java.io.IOException;
import java.io.OutputStream;

public class fasta {
  private final static class NullOutputStream extends OutputStream {
    @Override
    public void write(int b) throws IOException {
      // Do nothing
    }
  }

  public static final int IM = 139968;
  public static final int IA = 3877;
  public static final int IC = 29573;
  public static int last = 42;

  public static final int LINE_LENGTH = 60;

  // pseudo-random number generator
  public static final double random(double max) {
    last = (last * IA + IC) % IM;
    return max * last / IM;
  }

  // Weighted selection from alphabet
  public static String ALU =
      "GGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGG"
          + "GAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGA"
          + "CCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAAT"
          + "ACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCA"
          + "GCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGG"
          + "AGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCC"
          + "AGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAA";
  public static byte[] ALUB = ALU.getBytes();

  public static final frequency[] IUB =
      new frequency[] {
        new frequency('a', 0.27),
        new frequency('c', 0.12),
        new frequency('g', 0.12),
        new frequency('t', 0.27),
        new frequency('B', 0.02),
        new frequency('D', 0.02),
        new frequency('H', 0.02),
        new frequency('K', 0.02),
        new frequency('M', 0.02),
        new frequency('N', 0.02),
        new frequency('R', 0.02),
        new frequency('S', 0.02),
        new frequency('V', 0.02),
        new frequency('W', 0.02),
        new frequency('Y', 0.02)
      };

  public static final frequency[] HomoSapiens =
      new frequency[] {
        new frequency('a', 0.3029549426680d),
        new frequency('c', 0.1979883004921d),
        new frequency('g', 0.1975473066391d),
        new frequency('t', 0.3015094502008d)
      };

  public static void makeCumulative(frequency[] a) {
    double cp = 0.0;
    for (int i = 0; i < a.length; i++) {
      cp += a[i].p;
      a[i].p = cp;
    }
  }

  // naive
  public static final byte selectRandom(frequency[] a) {
    int len = a.length;
    double r = random(1.0);
    for (int i = 0; i < len; i++) if (r < a[i].p) return a[i].c;
    return a[len - 1].c;
  }

  static int BUFFER_SIZE = 1024;
  static int index = 0;
  static byte[] bbuffer = new byte[BUFFER_SIZE];

  final void makeRandomFasta(String id, String desc, frequency[] a, int n, OutputStream writer)
      throws IOException {
    index = 0;
    int m = 0;
    String descStr = ">" + id + " " + desc + '\n';
    writer.write(descStr.getBytes());
    while (n > 0) {
      if (n < LINE_LENGTH) m = n;
      else m = LINE_LENGTH;
      if (BUFFER_SIZE - index < m) {
        writer.write(bbuffer, 0, index);
        index = 0;
      }
      for (int i = 0; i < m; i++) {
        bbuffer[index++] = selectRandom(a);
      }
      bbuffer[index++] = '\n';
      n -= LINE_LENGTH;
    }
    if (index != 0) writer.write(bbuffer, 0, index);
  }

  final void makeRepeatFasta(String id, String desc, String alu, int n, OutputStream writer)
      throws IOException {
    index = 0;
    int m = 0;
    int k = 0;
    int kn = ALUB.length;
    String descStr = ">" + id + " " + desc + '\n';
    writer.write(descStr.getBytes());
    while (n > 0) {
      if (n < LINE_LENGTH) m = n;
      else m = LINE_LENGTH;
      if (BUFFER_SIZE - index < m) {
        writer.write(bbuffer, 0, index);
        index = 0;
      }
      for (int i = 0; i < m; i++) {
        if (k == kn) k = 0;
        bbuffer[index++] = ALUB[k];
        k++;
      }
      bbuffer[index++] = '\n';
      n -= LINE_LENGTH;
    }
    if (index != 0) writer.write(bbuffer, 0, index);
  }

  public static class frequency {
    public byte c;
    public double p;

    public frequency(char c, double p) {
      this.c = (byte) c;
      this.p = p;
    }
  }

  public void old_main() throws IOException {
    int n = 1000;

    OutputStream out = new NullOutputStream();
    makeRepeatFasta("ONE", "Homo sapiens alu", ALU, n * 2, out);
    makeRandomFasta("TWO", "IUB ambiguity codes", IUB, n * 3, out);
    makeRandomFasta("THREE", "Homo sapiens frequency", HomoSapiens, n * 5, out);
  }

  public void timeFasta(int iters) {
    makeCumulative(HomoSapiens);
    makeCumulative(IUB);

    try {
      for (int i = 0; i < iters; i++) {
        old_main();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean verifyFasta() {
    index = 0;
    int n = 25;

    OutputStream out = new NullOutputStream();
    try {
      makeRepeatFasta("ONE", "Homo sapiens alu", ALU, n * 2, out);
      int expected = 51;
      int found = index;

      if (expected != found) {
        System.out.println("ERROR: Expected " + expected + " but found " + found);
        return false;
      }

      makeRandomFasta("THREE", "Homo sapiens frequency", HomoSapiens, n * 5, out);
      expected = 128;
      found = index;

      if (expected != found) {
        System.out.println("ERROR: Expected " + expected + " but found " + found);
        return false;
      }

      makeRandomFasta("TWO", "IUB ambiguity codes", IUB, n * 3, out);
      expected = 77;
      found = index;

      if (expected != found) {
        System.out.println("ERROR: Expected " + expected + " but found " + found);
        return false;
      }
    } catch (IOException e) {
      return false;
    }

    return true;
  }

  public static void main(String[] args) {
    int rc = 0;
    fasta obj = new fasta();

    final long before = System.currentTimeMillis();
    obj.timeFasta(1700);
    final long after = System.currentTimeMillis();

    if (!obj.verifyFasta()) {
      rc++;
    }
    System.out.println("benchmarks/benchmarksgame/fasta: " + (after - before));
    System.exit(rc);
  }
}
