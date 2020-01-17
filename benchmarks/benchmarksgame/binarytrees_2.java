/*
 * This benchmark has been ported from "The Computer Language Benchmarks Game"
 * suite and slightly modified to fit the benchmarking framework.
 *
 * The original file:
 * https://benchmarksgame-team.pages.debian.net/benchmarksgame/program/binarytrees-java-2.html
 *
 * The Computer Language Benchmarks Game
 * https://salsa.debian.org/benchmarksgame-team/benchmarksgame/
 *
 * contributed by Jarkko Miettinen
 *
 * LICENSE: 3-Clause BSD
 * https://benchmarksgame-team.pages.debian.net/benchmarksgame/license.html
 */

/*
 * Description:     Allocate and deallocate many many binary trees.
 * Main Focus:      TODO
 *
 */

package benchmarks.benchmarksgame;

public class binarytrees_2 {
  private static final int PREDEFINED_DEPTH = 7;
  private static final int minDepth = 4;

  public int bench() {
    int maxDepth = (minDepth + 2 > PREDEFINED_DEPTH) ? minDepth + 2 : PREDEFINED_DEPTH;
    int stretchDepth = maxDepth + 1;

    int totalChecks = (TreeNode.bottomUpTree(stretchDepth)).itemCheck();

    TreeNode longLivedTree = TreeNode.bottomUpTree(maxDepth);

    for (int depth = minDepth; depth <= maxDepth; depth += 2) {
      int iterations = 1 << (maxDepth - depth + minDepth);
      int check = 0;

      for (int i = 1; i <= iterations; i++) {
        check += (TreeNode.bottomUpTree(depth)).itemCheck();
      }
      totalChecks += check;
    }
    totalChecks += longLivedTree.itemCheck();
    return totalChecks;
  }

  private static class TreeNode {
    private TreeNode left, right;

    private static TreeNode bottomUpTree(int depth) {
      if (depth > 0) {
        return new TreeNode(bottomUpTree(depth - 1), bottomUpTree(depth - 1));
      } else {
        return new TreeNode(null, null);
      }
    }

    TreeNode(TreeNode left, TreeNode right) {
      this.left = left;
      this.right = right;
    }

    private int itemCheck() {
      // if necessary deallocate here
      if (left == null) return 1;
      else return 1 + left.itemCheck() + right.itemCheck();
    }
  }

  static long checksum;

  public void timeBinaryTrees(int iters) {
    long sum = 0;
    for (int j = 0; j < iters; j++) {
      sum += bench();
    }
    checksum = sum;
  }

  public boolean verifyBinaryTrees() {
    int expected = 8798;
    int found = bench();

    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static void main(String[] args) {
    int rc = 0;
    binarytrees_2 obj = new binarytrees_2();
    final long before = System.currentTimeMillis();
    obj.timeBinaryTrees(30);
    final long after = System.currentTimeMillis();

    if (!obj.verifyBinaryTrees()) {
      rc++;
    }
    System.out.println("benchmarks/benchmarksgame/binarytrees_2: " + (after - before));
    System.exit(rc);
  }
}
