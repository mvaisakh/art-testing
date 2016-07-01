#include <stdio.h>
#include <stdlib.h>

    /* Bubble, Quick */
#define sortelements 5000
#define srtelements  500


    /* global */
long    seed;  /* converted to long for 16 bit WR*/

    /* Bubble, Quick */
int sortlist[sortelements+1], biggest, littlest, top;

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
	printf ( "Error3 in Bubble.\n");
	printf("%d\n", sortlist[run + 1]);
}

int main()
{
	int i;
	for (i = 0; i < 100; i++) Bubble(i);
	return 0;
}
