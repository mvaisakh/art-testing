/*
 * This benchmark has been ported from "The Computer Language Benchmarks Game"
 * suite and slightly modified to fit the benchmarking framework.
 *
 * The original file:
 * https://benchmarksgame-team.pages.debian.net/benchmarksgame/program/binarytrees-java-6.html
 *
 * The Computer Language Benchmarks Game
 * https://salsa.debian.org/benchmarksgame-team/benchmarksgame/
 *
 * contributed by Jarkko Miettinen
 * modified by Chandra Sekar
 * modified by Mike Kruger
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

// CHECKSTYLE.OFF: TypeName
public class binarytrees_6 {
// CHECKSTYLE.ON: TypeName
  private static final int PREDEFINED_DEPTH = 7;
  private static final int minDepth = 4;

  public int bench() {
    int maxDepth = Math.max(minDepth + 2, PREDEFINED_DEPTH);
    int stretchDepth = maxDepth + 1;

    int totalChecks = (TreeNode.create(stretchDepth)).check();

    TreeNode longLivedTree = TreeNode.create(maxDepth);

    for (int depth = minDepth; depth <= maxDepth; depth += 2) {
      int iterations = 1 << (maxDepth - depth + minDepth);
      int check = 0;

      for (int i = 1; i <= iterations; i++) {
        check += (TreeNode.create(depth)).check();
      }
      totalChecks += check;
    }

    totalChecks += longLivedTree.check();

    return totalChecks;
  }

  static class TreeNode {
    TreeNode left;
    TreeNode right;

    static TreeNode create(int depth) {
      return ChildTreeNodes(depth);
    }

    // CHECKSTYLE.OFF: MethodName
    static TreeNode ChildTreeNodes(int depth) {
    // CHECKSTYLE.ON: MethodName
      TreeNode node = new TreeNode();
      if (depth > 0) {
        node.left = ChildTreeNodes(depth - 1);
        node.right = ChildTreeNodes(depth - 1);
      }
      return node;
    }

    int check() {
      return left == null ? 1 : left.check() + right.check() + 1;
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
    binarytrees_6 obj = new binarytrees_6();
    final long before = System.currentTimeMillis();
    obj.timeBinaryTrees(30);
    final long after = System.currentTimeMillis();

    if (!obj.verifyBinaryTrees()) {
      rc++;
    }
    System.out.println("benchmarks/benchmarksgame/binarytrees_6: " + (after - before));
    System.exit(rc);
  }
}
