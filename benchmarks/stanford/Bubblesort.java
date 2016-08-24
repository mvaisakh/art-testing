/* Copied from https://llvm.org/svn/llvm-project/test-suite/tags/RELEASE_14/SingleSource/Benchmarks
 * License: LLVM Release License. See Notice file
 */

package benchmarks.stanford;

public class Bubblesort {

  private static final int sortelements = 5000;
  private static final int srtelements = 500;

  private boolean error; // Used as flag for the verify() function.
  private long seed;
  private int[] sortlist = new int [sortelements + 1];
  private int biggest;
  private int littlest;
  private int top;

// CHECKSTYLE.OFF: .*
void Initrand () {
    seed = 74755L;   /* constant to long WR*/
}

int Rand () {
    seed = (seed * 1309L + 13849L) & 65535L;  /* constants to long WR*/
    return( (int)seed );     /* typecast back to int WR*/
}


    /* Sorts an array using bubblesort */

void bInitarr()	{
	int i;
	long temp; /* converted temp to long for 16 bit WR*/
	Initrand();
	biggest = 0; littlest = 0;
	for ( i = 1; i <= srtelements; i++ ) {
	    temp = Rand();
	    /* converted constants to long in next stmt, typecast back to int WR*/
	    sortlist[i] = (int)(temp - (temp/100000L)*100000L - 50000L);
	    if ( sortlist[i] > biggest ) biggest = sortlist[i];
	    else if ( sortlist[i] < littlest ) littlest = sortlist[i];
	}
}

void Bubble(int run) {
	int i, j;
	bInitarr();
	top=srtelements;
	
	while ( top>1 ) {
		
		i=1;
		while ( i<top ) {
			
			if ( sortlist[i] > sortlist[i+1] ) {
				j = sortlist[i];
				sortlist[i] = sortlist[i+1];
				sortlist[i+1] = j;
			}
			i=i+1;
		}
		
		top=top-1;
	}
	if ( (sortlist[1] != littlest) || (sortlist[srtelements] != biggest) )
	error = true;
}
  // CHECKSTYLE.ON: .*

  public static boolean verify() {
    Bubblesort obj = new Bubblesort();
    obj.timeBubble(1);
    return !obj.error;
  }

  public void timeBubble(int iters) {
    for (int i = 0; i < iters; i++) {
      Bubble(i);
    }
  }

  public static void main(String[] args) {
    int rc = 0;
    Bubblesort obj = new Bubblesort();

    long before = System.currentTimeMillis();
    obj.timeBubble(1000);
    long after = System.currentTimeMillis();

    System.out.println("benchmarks/stanford/Bubblesort: " + (after - before));

    if (!verify()) {
      rc++;
    }

    System.exit(rc);
  }
}
