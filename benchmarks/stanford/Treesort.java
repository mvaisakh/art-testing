/* Copied from https://llvm.org/svn/llvm-project/test-suite/tags/RELEASE_14/SingleSource/Benchmarks
 * License: LLVM Release License. See Notice file
 */

package benchmarks.stanford;

public class Treesort {

  private static final int sortelements = 5000;
  private static final int srtelements = 500;

  private boolean error;

  private long seed;

  private int[] sortlist = new int [sortelements + 1];
  private int biggest;
  private int littlest;
  private int top;

  class Node {
    Node left;
    Node right;
    int val;
  }

  Node tree;

// CHECKSTYLE.OFF: .*
void Initrand () {
    seed = 74755L;   /* constant to long WR*/
}

int Rand () {
    seed = (seed * 1309L + 13849L) & 65535L;  /* constants to long WR*/
    return( (int)seed );     /* typecast back to int WR*/
}



    /* Sorts an array using treesort */

void tInitarr() {
	int i;
	long temp;  /* converted temp to long for 16 bit WR*/
	Initrand();
	biggest = 0; littlest = 0;
	for ( i = 1; i <= sortelements; i++ ) {
	    temp = Rand(); 
	    /* converted constants to long in next stmt, typecast back to int WR*/
	    sortlist[i] = (int)(temp - (temp/100000L)*100000L - 50000L);
	    if ( sortlist[i] > biggest ) biggest = sortlist[i];
	    else if ( sortlist[i] < littlest ) littlest = sortlist[i];
	}
}

void CreateNode (Node t, int n) {
		t = new Node();
		t.val = n;
}

void Insert(int n, Node t) {
	/* insert n into tree */
	if ( n > t.val ) 
		if ( t.left == null ) CreateNode(t.left,n);
		else Insert(n,t.left);
	else if ( n < t.val )
		if ( t.right == null ) CreateNode(t.right,n);
		else Insert(n,t.right);
}


boolean Checktree(Node p) {
    /* check by inorder traversal */
    boolean result;
    result = true;
	if ( p.left != null ) 
	   if ( p.left.val <= p.val ) result=false;
	   else result = Checktree(p.left) && result;
	if ( p.right != null )
	   if ( p.right.val >= p.val ) result = false;
	   else result = Checktree(p.right) && result;
	return( result);
} /* checktree */

void Trees(int run) {
    int i;
    tInitarr();
    tree = new Node();
    tree.left = null; tree.right=null; tree.val=sortlist[1];
    for ( i = 2; i <= sortelements; i++ )
		Insert(sortlist[i],tree);
    if ( ! Checktree(tree) ) error = true;
}
  // CHECKSTYLE.ON: .*

  public void timeTreesort(int iters) {
    for (int i = 0; i < iters; i++) {
      Trees(i);
    }
  }

  public static boolean verify() {
    Treesort obj = new Treesort();
    obj.timeTreesort(1);
    return !obj.error;
  }

  public static void main(String[] args) {
    int rc = 0;
    Treesort obj = new Treesort();

    long before = System.currentTimeMillis();
    obj.timeTreesort(2500);
    long after = System.currentTimeMillis();

    System.out.println("benchmarks/stanford/Treesort: " + (after - before));

    if (!obj.verify()) {
      rc++;
    }

    System.exit(rc);
  }
}
