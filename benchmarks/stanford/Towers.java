/* Copied from https://llvm.org/svn/llvm-project/test-suite/tags/RELEASE_14/SingleSource/Benchmarks
 * License: LLVM Release License. See Notice file
 */

package benchmarks.stanford;

public class Towers {

  private static final int maxcells = 18;

  private static final int stackrange = 3;

  private boolean error;

  class Element {
    int discsize;
    int next;
  }

  int[] stack = new int[stackrange + 1];
  Element[] cellspace = new Element[maxcells + 1];
  int freelist;
  int movesdone;

  public Towers() {
    for (int i = 0; i < cellspace.length; i++) {
      cellspace[i] = new Element();
    }
  }

// CHECKSTYLE.OFF: .*
void Error (String emsg) 	{
  error = true;
}


void Makenull (int s) {
	stack[s]=0;
}

int Getelement () {
	int temp = 0;  /* force init of temp WR*/
	if ( freelist>0 ) {
	    temp = freelist;
	    freelist = cellspace[freelist].next;
	}
	else 
	    Error("out of space   ");
	return (temp);
}

void Push(int i, int s)	{
	boolean errorfound=false;
	int localel;
	if ( stack[s] > 0 )
		if ( cellspace[stack[s]].discsize<=i ) {
			errorfound=true;
			Error("disc size error");
		}
	if ( ! errorfound )	{
		localel=Getelement();
		cellspace[localel].next=stack[s];
		stack[s]=localel;
		cellspace[localel].discsize=i;
	}
}

void Init (int s, int n) {
	int discctr;
	Makenull(s);
	for ( discctr = n; discctr >= 1; discctr-- )
	    Push(discctr,s);
}

int Pop (int s)	{
	int temp, temp1;
	if ( stack[s] > 0 ) {
		temp1 = cellspace[stack[s]].discsize;
		temp = cellspace[stack[s]].next;
		cellspace[stack[s]].next=freelist;
		freelist=stack[s];
		stack[s]=temp;
		return (temp1);
	}
	else
		Error("nothing to pop ");
	return 0;
}

void Move (int s1, int s2) {
	Push(Pop(s1),s2);
	movesdone=movesdone+1;
}

void tower(int i, int j, int k) {
	int other;
	if ( k==1 ) Move(i,j);
	else {
	    other=6-i-j;
	    tower(i,other,k-1);
	    Move(i,j);
	    tower(other,j,k-1);
	}
}

void Towers ()    { /* Towers */
    int i;
    for ( i=1; i <= maxcells; i++ ) cellspace[i].next=i-1;
    freelist=maxcells;
    Init(1,14);
    Makenull(2);
    Makenull(3);
    movesdone=0;
    tower(1,2,14);
    if ( movesdone != 16383 ) error = true;
} /* Towers */
  // CHECKSTYLE.ON: .*

  public void timeTowers(int iters) {
    for (int i = 0; i < iters; i++) {
      Towers();
    }
  }

  public static boolean verify() {
    Towers obj = new Towers();
    obj.timeTowers(1);
    return !obj.error;
  }

  public static void main(String[] args) {
    int rc = 0;
    Towers obj = new Towers();

    long before = System.currentTimeMillis();
    obj.timeTowers(100);
    long after = System.currentTimeMillis();

    System.out.println("benchmarks/stanford/Towers: " + (after - before));

    if (!verify()) {
      rc++;
    }

    System.exit(rc);
  }
}
