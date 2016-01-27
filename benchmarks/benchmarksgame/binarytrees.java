/*
 * This benchmark has been ported from "The Computer Language Benchmarks Game" suite and slightly
 * modified to fit the benchmarking framework.
 *
 * The original file is `binarytrees/binarytrees.java-2.java` from the archive
 * available at
 * http://benchmarksgame.alioth.debian.org/download/benchmarksgame-sourcecode.zip.
 * See LICENSE file in the same folder (BSD 3-clause)
 *
 * The Computer Language Benchmarks Game
 * http://benchmarksgame.alioth.debian.org/
 *
 * contributed by Jarkko Miettinen
 */

/*
 * Description:     Allocate and deallocate many many binary trees.
 * Main Focus:      TODO
 *
 */

package benchmarks.benchmarksgame;

// CHECKSTYLE.OFF: .*
public class binarytrees {

   private static final int PREDEFINED_DEPTH = 10;
   private final static int minDepth = 4;
   
   public int old_main(){
      int n = 0;
      int maxDepth = (minDepth + 2 > PREDEFINED_DEPTH) ? minDepth + 2 : PREDEFINED_DEPTH;
      int stretchDepth = maxDepth + 1;
      
      int check = (TreeNode.bottomUpTree(0,stretchDepth)).itemCheck();
      
      TreeNode longLivedTree = TreeNode.bottomUpTree(0,maxDepth);
      
      for (int depth=minDepth; depth<=maxDepth; depth+=2){
         int iterations = 1 << (maxDepth - depth + minDepth);
         check = 0;
         
         for (int i=1; i<=iterations; i++){
            check += (TreeNode.bottomUpTree(i,depth)).itemCheck();
            check += (TreeNode.bottomUpTree(-i,depth)).itemCheck();
         }
      }   
      return check;
   }
   
   
   private static class TreeNode
   {
      private TreeNode left, right;
      private int item;
      
      TreeNode(int item){
         this.item = item;
      }
      
      private static TreeNode bottomUpTree(int item, int depth){
         if (depth>0){
            return new TreeNode(
                  bottomUpTree(2*item-1, depth-1)
                  , bottomUpTree(2*item, depth-1)
                  , item
            );
         }
         else {
            return new TreeNode(item);
         }
      }
      
      TreeNode(TreeNode left, TreeNode right, int item){
         this.left = left;
         this.right = right;
         this.item = item;
      }
      
      private int itemCheck(){
         // if necessary deallocate here
         if (left==null) return item;
         else return item + left.itemCheck() - right.itemCheck();
      }
   }
  // CHECKSTYLE.ON: .*

  public void timeBinaryTrees(int iters) {
    for (int j = 0; j < iters; j++) {
      old_main();
    }
  }

  public boolean verifyBinaryTrees() {
    int expected = -32;
    int found = old_main();

    if (expected != found) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }

  public static void main(String[] args) {
    int rc = 0;
    binarytrees obj = new binarytrees();
    final long before = System.currentTimeMillis();
    obj.timeBinaryTrees(3);
    final long after = System.currentTimeMillis();

    if (!obj.verifyBinaryTrees()) {
      rc++;
    }
    System.out.println("benchmarks/benchmarksgame/binarytrees: " + (after - before));
    System.exit(rc);
  }
}
