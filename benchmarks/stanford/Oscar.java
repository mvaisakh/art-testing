/* Copied from https://llvm.org/svn/llvm-project/test-suite/tags/RELEASE_14/SingleSource/Benchmarks
 * License: LLVM Release License. See Notice file
 */

package benchmarks.stanford;

public class Oscar {

  private static final int fftsize = 256;
  private static final int fftsize2 = 129;

  class Complex {
    float rp;
    float ip;
  }

  private Complex[] z = new Complex [fftsize + 1];
  private Complex[] w = new Complex [fftsize + 1];
  private Complex[] e = new Complex [fftsize2 + 1];
  private float zr;
  private float zi;
  private float[] h = new float[26];

  private long seed;  /* converted to long for 16 bit WR*/

  public Oscar() {
    for (int i = 0; i < z.length; i++) z[i] = new Complex();
    for (int i = 0; i < e.length; i++) e[i] = new Complex();
    for (int i = 0; i < w.length; i++) w[i] = new Complex();
  }

// CHECKSTYLE.OFF: .*
float Cos (float x) {
/* computes cos of x (x in radians) by an expansion */
int i, factor;
float    result, power;

   result = 1.0f;
   factor = 1;
   power = x;
   for ( i = 2; i <= 10; i++ ) {
      factor = factor * i;  power = power*x;
      if ( (i & 1) == 0 )  {
        if ( (i & 3) == 0 ) result = result + power/factor;
		else result = result - power/factor;
      }
   }
   return (result);
}

int Min0( int arg1, int arg2) {
    if ( arg1 < arg2 ) 	return (arg1);
    else  return (arg2);
}

int Uniform11(int iy) {
    return (4855 * iy + 1731) & 8191;
} /* uniform */ 

void Exptab(int n, Complex e[]) { /* exptab */
    float theta, divisor;
    int i, j, k, l, m;

    theta = 3.1415926536f;
    divisor = 4.0f;
    for ( i=1; i <= 25; i++ ) {
		h[i] = 1/(2*Cos( theta/divisor ));
		divisor = divisor + divisor;
	}

    m = n / 2 ;
    l = m / 2 ;
    j = 1 ;
    e[1].rp = 1.0f;
    e[1].ip = 0.0f;
    e[l+1].rp = 0.0f;
    e[l+1].ip = 1.0f;
    e[m+1].rp = -1.0f;
    e[m+1].ip = 0.0f;

    do {
		i = l / 2 ;
		k = i ;
	
		do {
		    e[k+1].rp = h[j]*(e[k+i+1].rp+e[k-i+1].rp) ;
		    e[k+1].ip = h[j]*(e[k+i+1].ip+e[k-i+1].ip) ;
		    k = k+l ;
		} while ( k <= m );
	
		j = Min0( j+1, 25);
		l = i ;
	} while ( l > 1 );

} /* exptab */

void Fft( int n, Complex z[], Complex w[], Complex e[], float sqrinv) {
    int i, j, k, l, m, index;
    m = n / 2 ;
    l = 1 ;

    do {
		k = 0 ;
		j = l ;
		i = 1 ;
	
		do {
	
		    do {
			w[i+k].rp = z[i].rp+z[m+i].rp ;
			w[i+k].ip = z[i].ip+z[m+i].ip ;
			w[i+j].rp = e[k+1].rp*(z[i].rp-z[i+m].rp)
			-e[k+1].ip*(z[i].ip-z[i+m].ip) ;
			w[i+j].ip = e[k+1].rp*(z[i].ip-z[i+m].ip)
			+e[k+1].ip*(z[i].rp-z[i+m].rp) ;
			i = i+1 ;
		    } while ( i <= j );
	
		    k = j ;
		    j = k+l ;
		} while ( j <= m );
	
		/*z = w ;*/ index = 1;
		do {
		    z[index].rp = w[index].rp;
		    z[index].ip = w[index].ip;
		    index = index+1;
		} while ( index <= n );
		l = l+l ;
    } while ( l <= m );

    for ( i = 1; i <= n; i++ ){
		z[i].rp = sqrinv*z[i].rp ;
		z[i].ip = -sqrinv*z[i].ip;
	}

}

void Oscar() { /* oscar */
	int i;
	Exptab(fftsize,e) ;
	seed = 5767 ;
	for ( i = 1; i <= fftsize; i++ ) {
	    seed = Uniform11( (int)seed ); /* typecast seed for 16 bit WR*/
		zr = seed / 8192.0f;
	    seed = Uniform11( (int)seed ); /* typecast seed for 16 bit WR*/
		zi = seed / 8192.0f;
	    z[i].rp = 20.0f*zr - 10.0f;
	    z[i].ip = 20.0f*zi - 10.0f;
	}
	for ( i = 1; i <= 20; i++ ) {
	   Fft(fftsize,z,w,e,0.0625f) ;
	}
} /* oscar */
  // CHECKSTYLE.ON: .*

  public void timeOscar(int iters) {
    for (int i = 0; i < iters; i++) {
      Oscar();
    }
  }

  public static boolean verify() {
    Oscar obj = new Oscar();
    obj.timeOscar(1);
    // We know for a fact that z[256] = {-9.150383 -0.920413}
    // So its rp must be between -9.150f and -9.151f, otherwise it is an error.
    boolean error = obj.z[256].rp < -9.151f || -9.150f < obj.z[256].rp;
    // And its ip must be between -.920f and -921f, otherwise it is also an error.
    error = error || (obj.z[256].ip < -.921f || -.920f < obj.z[256].ip);
    // verify() returns 0 when there is no error.
    return !error;
  }

  public static void main(String[] args) {
    int rc = 0;
    Oscar obj = new Oscar();

    long before = System.currentTimeMillis();
    obj.timeOscar(10);
    long after = System.currentTimeMillis();

    System.out.println("benchmarks/stanford/Oscar: " + (after - before));

    if (!verify()) {
      rc++;
    }

    System.exit(rc);
  }
}
