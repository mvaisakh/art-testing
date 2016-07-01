#include <stdio.h>
#include <stdlib.h>

    /* Intmm, Mm */
#define rowsize 	 40

double rma[rowsize+1][rowsize+1], rmb[rowsize+1][rowsize+1], rmr[rowsize+1][rowsize+1];

long seed;

void Initrand () {
    seed = 74755L;   /* constant to long WR*/
}

int Rand () {
    seed = (seed * 1309L + 13849L) & 65535L;  /* constants to long WR*/
    return( (int)seed );     /* typecast back to int WR*/
}


    /* Multiplies two real matrices. */

void rInitmatrix ( double m[rowsize+1][rowsize+1] ) {
	int temp, i, j;
	for ( i = 1; i <= rowsize; i++ )
	    for ( j = 1; j <= rowsize; j++ ) {
	    	temp = Rand();
			m[i][j] = (double)(temp - (temp/120)*120 - 60)/3;
        }
}

void rInnerproduct(double *result, double a[rowsize+1][rowsize+1], double b[rowsize+1][rowsize+1], int row, int column) {
	/* computes the inner product of A[row,*] and B[*,column] */
	int i;
	*result = 0.0f;
	for (i = 1; i<=rowsize; i++) *result = *result+a[row][i]*b[i][column];
}

void Mm (int run)    {
    int i, j;
    Initrand();
    rInitmatrix (rma);
    rInitmatrix (rmb);
    for ( i = 1; i <= rowsize; i++ )
		for ( j = 1; j <= rowsize; j++ ) 
			rInnerproduct(&rmr[i][j],rma,rmb,i,j);
	printf("%f\n", rmr[run + 1][run + 1]);
}

int main()
{
	int i;
	for (i = 0; i < 10; i++) Mm(i);
	return 0;
}
