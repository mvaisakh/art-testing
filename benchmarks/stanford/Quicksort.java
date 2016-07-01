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
    if ( (sortlist[1] != littlest) || (sortlist[sortelements] != biggest) )	printf ( " Error in Quick.\n");
	  printf("%d\n", sortlist[run + 1]);
}

int main()
{
	int i;
	for (i = 0; i < 100; i++) Quick(i);
	return 0;
}

