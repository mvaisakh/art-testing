/*
 * This benchmark has been ported from "The Computer Language Benchmarks Game"
 * suite and slightly modified to fit the benchmarking framework.
 *
 * The original file:
 * https://benchmarksgame-team.pages.debian.net/benchmarksgame/program/fannkuchredux-java-2.html
 *
 * The Computer Language Benchmarks Game
 * https://salsa.debian.org/benchmarksgame-team/benchmarksgame/
 *
 * contributed by Isaac Gouy
 * converted to Java by Oleg Mazurov
 *
 * LICENSE: 3-Clause BSD
 * https://benchmarksgame-team.pages.debian.net/benchmarksgame/license.html
 */

/*
 * Description:     Indexed-access to tiny integer-sequence.
 * Main Focus:      TODO
 *
 */

package benchmarks.benchmarksgame;

// CHECKSTYLE.OFF: TypeName
public class fannkuchredux {
// CHECKSTYLE.ON: TypeName
  public int fannkuch(int n) {
    int[] perm = new int[n];
    int[] perm1 = new int[n];
    int[] count = new int[n];
    int maxFlipsCount = 0;
    int permCount = 0;
    int checksum = 0;

    for (int i = 0; i < n; i++) perm1[i] = i;
    int r = n;

    while (true) {

      while (r != 1) {
        count[r - 1] = r;
        r--;
      }

      for (int i = 0; i < n; i++) perm[i] = perm1[i];
      int flipsCount = 0;
      int k;

      while (!((k = perm[0]) == 0)) {
        int k2 = (k + 1) >> 1;
        for (int i = 0; i < k2; i++) {
          int temp = perm[i];
          perm[i] = perm[k - i];
          perm[k - i] = temp;
        }
        flipsCount++;
      }

      maxFlipsCount = Math.max(maxFlipsCount, flipsCount);
      checksum += permCount % 2 == 0 ? flipsCount : -flipsCount;

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

  private static final int PREDEFINED_N_PANCAKES = 7;

  public void timeFannkuchRedux(int iters) {
    for (int i = 0; i < iters; i++) {
      fannkuch(PREDEFINED_N_PANCAKES);
    }
  }

  public boolean verifyFannkuchRedux() {
    int expected = 16;
    int found = fannkuch(PREDEFINED_N_PANCAKES);

    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static void main(String[] args) {
    int rc = 0;
    fannkuchredux obj = new fannkuchredux();
    final long before = System.currentTimeMillis();
    obj.timeFannkuchRedux(1100);
    final long after = System.currentTimeMillis();

    if (!obj.verifyFannkuchRedux()) {
      rc++;
    }
    System.out.println("benchmarks/benchmarksgame/fannkuchredux: " + (after - before));
    System.exit(rc);
  }
}
