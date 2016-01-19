/*
 * This benchmark has been ported from "The Computer Language Benchmarks Game" suite and slightly
 * modified to fit the benchmarking framework.
 *
 * The original file is `fannkuchredux/fannkuchredux.java-2.java` from the
 * archive available at
 * http://benchmarksgame.alioth.debian.org/download/benchmarksgame-sourcecode.zip.
 * See LICENSE file in the same folder (BSD 3-clause).
 *
 * The Computer Language Benchmarks Game
 * http://benchmarksgame.alioth.debian.org/
 *
 * contributed by Isaac Gouy
 * converted to Java by Oleg Mazurov
 */

/*
 * Description:     Indexed-access to tiny integer-sequence.
 * Main Focus:      TODO
 *
 */

package benchmarks.benchmarksgame;

public class FannkuchRedux {
   // CHECKSTYLE.OFF: .*
   public int fannkuch(int n) {
      int[] perm = new int[n];
      int[] perm1 = new int[n];
      int[] count = new int[n];
      int maxFlipsCount = 0;
      int permCount = 0;
      int checksum = 0;

      for(int i=0; i<n; i++) perm1[i] = i;
      int r = n;

      while (true) {

         while (r != 1){ count[r-1] = r; r--; }

         for(int i=0; i<n; i++) perm[i] = perm1[i];
         int flipsCount = 0;
         int k;

         while ( !((k=perm[0]) == 0) ) {
            int k2 = (k+1) >> 1;
            for(int i=0; i<k2; i++) {
               int temp = perm[i]; perm[i] = perm[k-i]; perm[k-i] = temp;
            }
            flipsCount++;
         }

         maxFlipsCount = Math.max(maxFlipsCount, flipsCount);
         checksum += permCount%2 == 0 ? flipsCount : -flipsCount;

         // Use incremental change to generate another permutation
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
            if (count[r] > 0) break;
            r++;
         }

         permCount++;
      }
   }
   // CHECKSTYLE.ON: .*

  private static final int PREDEFINED_N_PANCAKES = 9;

  public void timeFannkuchRedux(int iters) {
    for (int i = 0; i < iters; i++) {
      fannkuch(PREDEFINED_N_PANCAKES);
    }
  }

  public boolean verify() {
    return fannkuch(PREDEFINED_N_PANCAKES) == 30;
  }

  public static void main(String[] args) {

    FannkuchRedux obj = new FannkuchRedux();
    final long before = System.currentTimeMillis();
    obj.timeFannkuchRedux(4);
    final long after = System.currentTimeMillis();

    obj.verify();
    System.out.println("benchmarks/benchmarksgame/FannkuchRedux: " + (after - before));
  }
}
