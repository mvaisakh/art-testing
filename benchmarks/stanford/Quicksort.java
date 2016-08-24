/* Copied from https://llvm.org/svn/llvm-project/test-suite/tags/RELEASE_14/SingleSource/Benchmarks
 * License: LLVM Release License. See Notice file
 */

package benchmarks.stanford;

public class Quicksort {

  /* Bubble, Quick */
  private static final int sortelements = 5000;
  private static final int srtelements = 500;

  private boolean error;
  long seed;

  int[] sortlist = new int [sortelements + 1];
  int biggest;
  int littlest;
  int inttop;

// CHECKSTYLE.OFF: .*
void Initrand () {
    seed = 74755L;   /* constant to long WR*/
}

int Rand () {
    seed = (seed * 1309L + 13849L) & 65535L;  /* constants to long WR*/
    return( (int)seed );     /* typecast back to int WR*/
}

    /* Sorts an array using quicksort */
void Initarr() {
	int i; /* temp */
	long temp;  /* made temp a long for 16 bit WR*/
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

void Quicksort( int a[], int l, int r) {
	/* quicksort the array A from start to finish */
	int i,j,x,w;

	i=l; j=r;
	x=a[(l+r) / 2];
	do {
	    while ( a[i]<x ) i = i+1;
	    while ( x<a[j] ) j = j-1;
	    if ( i<=j ) {
			w = a[i];
			a[i] = a[j];
			a[j] = w;
			i = i+1;    j= j-1;
		}
	} while ( i<=j );
	if ( l <j ) Quicksort(a,l,j);
	if ( i<r ) Quicksort(a,i,r);
}


void Quick (int run) {
    Initarr();
    Quicksort(sortlist,1,sortelements);
    if ( (sortlist[1] != littlest) || (sortlist[sortelements] != biggest) )	error = true;
}
  // CHECKSTYLE.ON: .*

  public void timeQuicksort(int iters) {
    for (int i = 0; i < iters; i++) {
      Quick(i);
    }
  }

  public static boolean verify() {
    Quicksort obj = new Quicksort();
    obj.timeQuicksort(1);
    return !obj.error;
  }

  public static void main(String[] args) {
    int rc = 0;
    Quicksort obj = new Quicksort();

    long before = System.currentTimeMillis();
    obj.timeQuicksort(1200);
    long after = System.currentTimeMillis();

    System.out.println("benchmarks/stanford/Quicksort: " + (after - before));

    if (!verify()) {
      rc++;
    }

    System.exit(rc);
  }
}
