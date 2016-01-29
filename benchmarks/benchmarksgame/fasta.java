/*
 * This benchmark has been ported from "The Computer Language Benchmarks Game" suite and modified
 * to fit the benchmarking framework.
 * The original benchmarks printed long strings to the stdout. This print was removed to fit the
 * framework. These action can cause difference in behaviour of the original and changed benchmarks;
 * it hasn't been estimated yet.
 *
 * The original file is `fasta/fasta.java-2.java` from the archive available at
 * http://benchmarksgame.alioth.debian.org/download/benchmarksgame-sourcecode.zip.
 * See LICENSE file in the same folder (BSD 3-clause).
 *
 * The Computer Language Benchmarks Game
 * http://benchmarksgame.alioth.debian.org/
 *
 * modified by Mehmet D. AKIN
 *
 */

/*
 * Description:     Generate and write random DNA sequences.
 * Main Focus:      TODO
 *
 */

package benchmarks.benchmarksgame;

// CHECKSTYLE.OFF: .*
public class fasta {
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

    public static final frequency[] IUB = new frequency[] {
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
            new frequency('Y', 0.02) };

    public static final frequency[] HomoSapiens = new frequency[] {
            new frequency('a', 0.3029549426680d),
            new frequency('c', 0.1979883004921d),
            new frequency('g', 0.1975473066391d),
            new frequency('t', 0.3015094502008d)};

    public static void makeCumulative(frequency[] a) {
        double cp = 0.0;
        for (int i = 0; i < a.length; i++) {
            cp += a[i].p;
            a[i].p = cp;
        }
    }

    // naive
    public final static byte selectRandom(frequency[] a) {
        int len = a.length;
        double r = random(1.0);
        for (int i = 0; i < len; i++)
            if (r < a[i].p)
                return a[i].c;
        return a[len - 1].c;
    }

    static int BUFFER_SIZE = 1024;
    static int index = 0;
    static byte[] bbuffer = new byte[BUFFER_SIZE];
    final void makeRandomFasta(String id, String desc,frequency[] a, int n)
    {
        index = 0;
        int m = 0;
        String descStr = ">" + id + " " + desc + '\n'; 
        while (n > 0) {
            if (n < LINE_LENGTH) m = n;  else m = LINE_LENGTH;
            if(BUFFER_SIZE - index < m){
                index = 0;
            }
            for (int i = 0; i < m; i++) {
                bbuffer[index++] = selectRandom(a);
            }
            bbuffer[index++] = '\n';
            n -= LINE_LENGTH;
        }
    }    
    
    final void makeRepeatFasta(String id, String desc, String alu, int n)
    {
        index = 0;
        int m = 0;
        int k = 0;
        int kn = ALUB.length;
        String descStr = ">" + id + " " + desc + '\n'; 
        while (n > 0) {
            if (n < LINE_LENGTH) m = n; else m = LINE_LENGTH;
            if(BUFFER_SIZE - index < m){
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
    }

    public static class frequency {
        public byte c;
        public double p;

        public frequency(char c, double p) {
            this.c = (byte)c;
            this.p = p;
        }
    }

    public void old_main() {
        int n = 1000;
     
        makeRepeatFasta("ONE", "Homo sapiens alu", ALU, n * 2);
        makeRandomFasta("TWO", "IUB ambiguity codes", IUB, n * 3);
        makeRandomFasta("THREE", "Homo sapiens frequency", HomoSapiens, n * 5);
    }
    // CHECKSTYLE.ON: .*

  public void timeFasta(int iters) {
    makeCumulative(HomoSapiens);
    makeCumulative(IUB);

    for (int i = 0; i < iters; i++) {
      old_main();
    }
  }

  public boolean verifyFasta() {
    index = 0;
    int n = 25;
    
    makeRepeatFasta("ONE", "Homo sapiens alu", ALU, n * 2);
    int expected = 51;
    int found = index;

    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
  
    makeRandomFasta("THREE", "Homo sapiens frequency", HomoSapiens, n * 5);
    expected = 128;
    found = index;

    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }

    makeRandomFasta("TWO", "IUB ambiguity codes", IUB, n * 3);
    expected = 77;
    found = index;

    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }

    return true;
  }

  public static void main(String[] args) {
    int rc = 0;
    fasta obj = new fasta();

    final long before = System.currentTimeMillis();
    obj.timeFasta(5);
    final long after = System.currentTimeMillis();

    if (!obj.verifyFasta()) {
      rc++;
    }
    System.out.println("benchmarks/benchmarksgame/fasta: " + (after - before));
    System.exit(rc);
  }
}