/* Copied from https://llvm.org/svn/llvm-project/test-suite/tags/RELEASE_14/SingleSource/Benchmarks
 * License: LLVM Release License. See Notice file
 */

package benchmarks.stanford;

public class Perm {

  private static final int permrange = 10;

  private boolean error;

  /* Perm */
  private int[] permarray = new int [permrange + 1];
  /* converted pctr to unsigned int for 16 bit WR*/
  private int  pctr;

// CHECKSTYLE.OFF: .*
    /* Permutation program, heavily recursive, written by Denny Brown. */
void Swap ( int a[], int ai,  int b[], int bi ) {
	int t;
	t = a[ai];  a[ai] = b[bi];  b[bi] = t;
}

void Initialize () {
	int i;
	for ( i = 1; i <= 7; i++ ) {
	    permarray[i]=i-1;
	}
}

void Permute (int n) {   /* permute */
	int k;
	pctr = pctr + 1;
	if ( n!=1 )  {
	    Permute(n-1);
	    for ( k = n-1; k >= 1; k-- ) {
			Swap(permarray, n, permarray, k);
			Permute(n-1);
			Swap(permarray, n, permarray, k);
		}
    }
}     /* permute */

void Perm ()    {   /* Perm */
    int i;
    pctr = 0;
    for ( i = 1; i <= 5; i++ ) {
		Initialize();
		Permute(7);
	}
    if ( pctr != 43300 )
	error = true;
}     /* Perm */
  // CHECKSTYLE.ON: .*

  public void timePerm(int iters) {
    for (int i = 0; i < iters; i++) {
      Perm();
    }
  }

  public static boolean verify() {
    Perm obj = new Perm();
    obj.timePerm(1);
    return !obj.error;
  }

  public static void main(String[] args) {
    int rc = 0;
    Perm obj = new Perm();

    long before = System.currentTimeMillis();
    obj.timePerm(100);
    long after = System.currentTimeMillis();

    System.out.println("benchmarks/stanford/Perm: " + (after - before));

    if (!verify()) {
      rc++;
    }

    System.exit(rc);
  }
}
