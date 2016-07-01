#include <stdio.h>
#include <stdlib.h>


    /* Intmm, Mm */
#define rowsize 	 40

    /* global */
long    seed;  /* converted to long for 16 bit WR*/

    /* Intmm, Mm */

int   ima[rowsize+1][rowsize+1], imb[rowsize+1][rowsize+1], imr[rowsize+1][rowsize+1];

void Initrand () {
    seed = 74755L;   /* constant to long WR*/
}

int Rand () {
    seed = (seed * 1309L + 13849L) & 65535L;  /* constants to long WR*/
    return( (int)seed );     /* typecast back to int WR*/
}


    /* Multiplies two integer matrices. */

void Initmatrix (int m[rowsize+1][rowsize+1]) {
	int temp, i, j;
	for ( i = 1; i <= rowsize; i++ )
	    for ( j = 1; j <= rowsize; j++ ) {
	    temp = Rand();
		m[i][j] = temp - (temp/120)*120 - 60;
	}
}

void Innerproduct( int *result, int a[rowsize+1][rowsize+1], int b[rowsize+1][rowsize+1], int row, int column) {
	/* computes the inner product of A[row,*] and B[*,column] */
	int i;
	*result = 0;
	for(i = 1; i <= rowsize; i++ )*result = *result+a[row][i]*b[i][column];
}

void Intmm (int run) {
    int i, j;
    Initrand();
    Initmatrix (ima);
    Initmatrix (imb);
    for ( i = 1; i <= rowsize; i++ )
		for ( j = 1; j <= rowsize; j++ )
			Innerproduct(&imr[i][j],ima,imb,i,j);
	printf("%d\n", imr[run + 1][run + 1]);
}

int main()
{
	int i;
	for (i = 0; i < 10; i++) Intmm(i);
	return 0;
}
