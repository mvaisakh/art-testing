/* Copied from https://llvm.org/svn/llvm-project/test-suite/tags/RELEASE_14/SingleSource/Benchmarks
 * License: LLVM Release License. See Notice file
 */

package benchmarks.stanford;

public class Queens {

  /* Since the original benchmark uses these to calculate indices,
   * it is not appropriate to use pure booleans, and we use the constants below:
   */
  private static final int FALSE = 0;
  private static final int TRUE = 1;

  private boolean error;
  private int q = FALSE;

  private int[] a = new int [9];
  private int[] b = new int[17];
  private int[] c = new int [15];
  private int[] x = new int [9];

// CHECKSTYLE.OFF: .*
void Try(int i, int a[], int b[], int c[], int x[]) {
	int     j;
	j = 0;
	q = FALSE;
	while ( (q == FALSE) && (j != 8) ) {
		j = j + 1;
		q = FALSE;
		if ( b[j] != FALSE && a[i+j] != FALSE && c[i-j+7] != FALSE ) {
			x[i] = j;
		    b[j] = FALSE;
		    a[i+j] = FALSE;
		    c[i-j+7] = FALSE;
		    if ( i < 8 ) {
		    	Try(i+1,a,b,c,x);
				if ( q ==FALSE ) {
					b[j] = TRUE;
				    a[i+j] = TRUE;
				    c[i-j+7] = TRUE;
				}
			}
		    else q = TRUE;
	    }
	}
}

void Doit () {
	int i;
	i = 0 - 7;
	while ( i <= 16 ) {
		if ( (i >= 1) && (i <= 8) ) a[i] = TRUE;
	    if ( i >= 2 ) b[i] = TRUE;
	    if ( i <= 7 ) c[i+7] = TRUE;
	    i = i + 1;
	}

	Try(1, b, a, c, x);
	if ( q == FALSE ) error = true;
}

void Queens (int run) {
    int i;
    for ( i = 1; i <= 50; i++ ) Doit();
}
  // CHECKSTYLE.ON: .*

  public void timeQueens(int iters) {
    for (int i = 0; i < iters; i++) {
      Queens(i);
    }
  }

  public static boolean verify() {
    Queens obj = new Queens();
    obj.timeQueens(1);
    return !obj.error;
  }

  public static void main(String[] args) {
    int rc = 0;
    Queens obj = new Queens();

    long before = System.currentTimeMillis();
    obj.timeQueens(100);
    long after = System.currentTimeMillis();

    System.out.println("benchmarks/stanford/Queens: " + (after - before));

    if (!verify()) {
      rc++;
    }

    System.exit(rc);
  }
}
