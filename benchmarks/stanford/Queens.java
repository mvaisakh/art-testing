#include <stdio.h>
#include <stdlib.h>

#define	 FALSE		0
#define  TRUE		1

void Try(int i, int *q, int a[], int b[], int c[], int x[]) {
	int     j;
	j = 0;
	*q = FALSE;
	while ( (! *q) && (j != 8) ) {
		j = j + 1;
		*q = FALSE;
		if ( b[j] && a[i+j] && c[i-j+7] ) {
			x[i] = j;
		    b[j] = FALSE;
		    a[i+j] = FALSE;
		    c[i-j+7] = FALSE;
		    if ( i < 8 ) {
		    	Try(i+1,q,a,b,c,x);
				if ( ! *q ) {
					b[j] = TRUE;
				    a[i+j] = TRUE;
				    c[i-j+7] = TRUE;
				}
			}
		    else *q = TRUE;
	    }
	}
}
	
void Doit () {
	int i,q;
	int a[9], b[17], c[15], x[9];
	i = 0 - 7;
	while ( i <= 16 ) {
		if ( (i >= 1) && (i <= 8) ) a[i] = TRUE;
	    if ( i >= 2 ) b[i] = TRUE;
	    if ( i <= 7 ) c[i+7] = TRUE;
	    i = i + 1;
	}

	Try(1, &q, b, a, c, x);
	if ( !q ) printf (" Error in Queens.\n");
}

void Queens (int run) {
    int i;
    for ( i = 1; i <= 50; i++ ) Doit();
	 printf("%d\n", run + 1);
}

int main()
{
	int i;
	for (i = 0; i < 100; i++) Queens(i);
	return 0;
}
