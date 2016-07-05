/* Copied from https://llvm.org/svn/llvm-project/test-suite/tags/RELEASE_14/SingleSource/Benchmarks
 * License: LLVM Release License. See Notice file
 */

package benchmarks.stanford;

public class RealMM {

  private static final int rowsize = 40;

  private double[][] rma = new double [rowsize + 1][rowsize + 1];
  private double[][] rmb = new double [rowsize + 1][rowsize + 1];
  private double[][] rmr = new double [rowsize + 1][rowsize + 1];

  long seed;

// CHECKSTYLE.OFF: .*
void Initrand () {
    seed = 74755L;   /* constant to long WR*/
}

int Rand () {
    seed = (seed * 1309L + 13849L) & 65535L;  /* constants to long WR*/
    return( (int)seed );     /* typecast back to int WR*/
}


    /* Multiplies two real matrices. */

void rInitmatrix ( double m[][] ) {
	int temp, i, j;
	for ( i = 1; i <= rowsize; i++ )
	    for ( j = 1; j <= rowsize; j++ ) {
	    	temp = Rand();
			m[i][j] = (double)(temp - (temp/120)*120 - 60)/3;
        }
}

void rInnerproduct(double result[][], double a[][], double b[][], int row, int column) {
	/* computes the inner product of A[row,*] and B[*,column] */
	int i;
	result[row][column] = 0.0f;
	for (i = 1; i<=rowsize; i++) result[row][column] = result[row][column]+a[row][i]*b[i][column];
}

void Mm (int run)    {
    int i, j;
    Initrand();
    rInitmatrix (rma);
    rInitmatrix (rmb);
    for ( i = 1; i <= rowsize; i++ )
		for ( j = 1; j <= rowsize; j++ ) 
			rInnerproduct(rmr,rma,rmb,i,j);
}
  // CHECKSTYLE.ON: .*

  public void timeRealMM(int iters) {
    for (int i = 0; i < iters; i++) {
      Mm(i);
    }
  }

  public static boolean verify() {
    RealMM obj = new RealMM();
    obj.timeRealMM(1);
    // Expected obj.rmr[1][1] value: -775.9999999999999
    boolean error = obj.rmr[1][1] < -776.000f || -775.999f < obj.rmr[1][1];
    return !error;
  }

  public static void main(String[] args) {
    int rc = 0;
    RealMM obj = new RealMM();

    long before = System.currentTimeMillis();
    obj.timeRealMM(10);
    long after = System.currentTimeMillis();

    System.out.println("benchmarks/stanford/RealMM: " + (after - before));

    if (!verify()) {
      rc++;
    }

    System.exit(rc);
  }
}
