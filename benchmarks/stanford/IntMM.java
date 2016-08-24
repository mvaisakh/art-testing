/* Copied from https://llvm.org/svn/llvm-project/test-suite/tags/RELEASE_14/SingleSource/Benchmarks
 * License: LLVM Release License. See Notice file
 */

package benchmarks.stanford;

public class IntMM {

  private static final int rowsize = 40;

  // Number of (internal) iterations for IntMM runs as per original benchmark
  private static final int INTMM_ITERS = 10;
  private static final int EXPECTED = 300;

  private long seed;

  private int[][] ima = new int[rowsize + 1][rowsize + 1];
  private int[][] imb = new int[rowsize + 1][rowsize + 1];
  private int[][] imr = new int[rowsize + 1][rowsize + 1];

// CHECKSTYLE.OFF: .*
void Initrand () {
    seed = 74755L;   /* constant to long WR*/
}

int Rand () {
    seed = (seed * 1309L + 13849L) & 65535L;  /* constants to long WR*/
    return( (int)seed );     /* typecast back to int WR*/
}


    /* Multiplies two integer matrices. */

private void Initmatrix (int m[][]) {
	int temp, i, j;
	for ( i = 1; i <= rowsize; i++ )
	    for ( j = 1; j <= rowsize; j++ ) {
	    temp = Rand();
		m[i][j] = temp - (temp/120)*120 - 60;
	}
}

void Innerproduct( int result[][], int a[][], int b[][], int row, int column) {
	/* computes the inner product of A[row,*] and B[*,column] */
	int i;
	int tmp = 0;
	for(i = 1; i <= rowsize; i++ ) tmp = tmp+a[row][i]*b[i][column];
	result[row][column] = tmp;
  }

void Intmm (int run) {
    int i, j;
    Initrand();
    Initmatrix (ima);
    Initmatrix (imb);
    for ( i = 1; i <= rowsize; i++ )
		for ( j = 1; j <= rowsize; j++ )
			Innerproduct(imr,ima,imb,i,j);
}
  // CHECKSTYLE.ON: .*

  public void timeIntmm(int iters) {
    for (int i = 0; i < iters; i++) {
      for (int j = 0; j < INTMM_ITERS; j++) {
        Intmm(j);
      }
    }
  }

  public static boolean verify() {
    IntMM obj = new IntMM();
    obj.timeIntmm(1);
    return obj.imr[INTMM_ITERS][INTMM_ITERS] == EXPECTED;
  }

  public static void main(String[] args) {
    int rc = 0;
    IntMM obj = new IntMM();

    long before = System.currentTimeMillis();
    obj.timeIntmm(120);
    long after = System.currentTimeMillis();

    System.out.println("benchmarks/stanford/IntMM: " + (after - before));

    if (!verify()) {
      rc++;
    }

    System.exit(rc);
  }
}
