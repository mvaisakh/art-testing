/// This test contains some of the loops from the GCC vectrorizer example page [1].
/// Dorit Nuzman who developed the gcc vectorizer said that we can use them in our test suite.
///
/// [1] - http://gcc.gnu.org/projects/tree-ssa/vectorization.html

package benchmarks.micro;

// CHECKSTYLE.OFF: .*
public class GCCLoops {
private static final int N = 1024;
private static final int M = 32;
private static final int K = 4;

char[] usa = new char[N];
short[] sa = new short[N];
short[] sb = new short[N];
short[] sc = new short[N];
int[]   ua = new int[N];
int[]   ia = new int[N];
int[]   ib = new int[N];
int[]   ic = new int[N];
int[] ub = new int[N];
int[] uc = new int[N];
float[] fa = new float[N];
float[] fb = new float[N];
float[] da = new float[N];
float[] db = new float[N];
float[] dc = new float[N];
float[] dd = new float[N];
int[] dj = new int[N];

public class StructAType {
  public int[] ca = new int[N];
}

StructAType s = new StructAType();

int[] a = new int[N*2];
int[] b = new int[N*2];
int[] c = new int[N*2];
int[] d = new int[N*2];

int[][] matrixA_m_n = new int[M*2][N*2];
int[][] matrixB_m_n = new int[M*2][N*2];
int[][] matrixA_n_m = new int[N*2][M*2];
int[][] matrixB_n_m = new int[N*2][M*2];
int[][] G = new int[M][N];

void example1 () {
  int i;

  for (i=0; i<N; i++){
    a[i] = b[i] + c[i];
  }
}

void example2a (int n, int x) {
   int i;

   /* feature: support for unknown loop bound  */
   /* feature: support for loop invariants  */
   for (i=0; i<n; i++) {
      b[i] = x;
   }
}

void example2b (int n) {
  int i = 0;
  int k = n;
   /* feature: general loop exit condition  */
   /* feature: support for bitwise operations  */
   while (k-- > 0){
      a[i] = b[i]&c[i]; i++;
   }
}


void example3 (int n) {
  int i = 0;
  int k = n;
   /* feature: support for (aligned) pointer accesses.  */
   while (k-- > 0){
      ia[i] = ib[i];
      i++;
   }
}

void example4a (int n) {
  int i = 0;
  int k = n;
   /* feature: support for (aligned) pointer accesses  */
   /* feature: support for constants  */
   while (k-- > 0){
      ia[i] = ib[i] + 5;
      i++;
   }
}

void example4b (int n) {
   int i;

   /* feature: support for read accesses with a compile time known misalignment  */
   for (i=0; i<n; i++){
      a[i] = b[i+1] + c[i+3];
   }
}

void example4c (int n) {
   int i;
    final int MAX = 4;
   /* feature: support for if-conversion  */
   for (i=0; i<n; i++){
      int j = a[i];
      b[i] = (j > MAX ? MAX : 0);
   }
}

void  example5 (int n, StructAType s) {
  int i;
  for (i = 0; i < n; i++) {
    /* feature: support for alignable struct access  */
    s.ca[i] = 5;
  }
}

void  example7 (int x) {
   int i;

   /* feature: support for read accesses with an unknown misalignment  */
   for (i=0; i<N; i++){
      a[i] = b[i+x];
   }
}

void example8 (int x) {
   int i,j;

   /* feature: support for multidimensional arrays  */
   for (i=0; i<M; i++) {
     for (j=0; j<N; j++) {
       G[i][j] = x;
     }
   }
}


int example9 () {
  int i;

  /* feature: support summation reduction.
     note: in case of floats use -funsafe-math-optimizations  */
  int diff = 0;
  for (i = 0; i < N; i++) {
    diff += (ub[i] - uc[i]);
  }

  // Inserted to avoid DCE removing the whole loop.
  ub[0] = diff;
  return diff;
}


/* feature: support data-types of different sizes.
   Currently only a single vector-size per target is supported; 
   it can accommodate n elements such that n = vector-size/element-size 
   (e.g, 4 ints, 8 shorts, or 16 chars for a vector of size 16 bytes). 
   A combination of data-types of different sizes in the same loop 
   requires special handling. This support is now present in mainline,
   and also includes support for type conversions.  */
void example10a(short[] sa, short[] sb, short[] sc, int[] ia, int[] ib, int[] ic) {
  int i;
  for (i = 0; i < N; i++) {
    ia[i] = ib[i] + ic[i];
    sa[i] = (short)(sb[i] + sc[i]);
  }
}

void example10b(short[] sb, int[] ia) {
  int i;
  for (i = 0; i < N; i++) {
    ia[i] = (int) sb[i];
  }
}

/* feature: support strided accesses - the data elements
   that are to be operated upon in parallel are not consecutive - they
   are accessed with a stride > 1 (in the example, the stride is 2):  */
void example11() {
   int i;
  for (i = 0; i < N/2; i++){
    a[i] = b[2*i+1] * c[2*i+1] - b[2*i] * c[2*i];
    d[i] = b[2*i] * c[2*i+1] + b[2*i+1] * c[2*i];
  }
}


void example12() {
  for (int i = 0; i < N; i++) {
    a[i] = i;
  }
}

void example13(int[][] A, int[][] B, int[] out) {
  int i,j;
  for (i = 0; i < M; i++) {
    int diff = 0;
    for (j = 0; j < N; j+=8) {
      diff += (A[i][j] - B[i][j]);
    }
    out[i] = diff;
  }
}

void example14(int[][] in, int[][] coeff, int[] out) {
  int k,j,i=0;
  for (k = 0; k < K; k++) {
    int sum = 0;
    for (j = 0; j < M; j++)
      for (i = 0; i < N; i++)
          sum += in[i+k][j] * coeff[i][j];

    out[k] = sum;
  }

}


void example21(int[] b, int n) {
  int i, a = 0;

  for (i = n-1; i >= 0; i--)
    a += b[i];

  b[0] = a;
}

void example23 (char[] src, int[] dst)
{
  int i;
  int k = 0;

  for (i = 0; i < 256; i++) {
    dst[k] = src[k] << 7;
    k++;
  }
}


void example24 (short x, short y)
{
  int i;
  for (i = 0; i < N; i++)
    ic[i] = fa[i] < fb[i] ? x : y;
}


void example25 ()
{
  int i;
  char x, y;
  for (i = 0; i < N; i++)
    {
      x = (char)(da[i] < db[i] ? 1 : 0);
      y = (char)(dc[i] < dd[i] ? 1 : 0);
      dj[i] = x & y;
    }
}
// CHECKSTYLE.ON: .*

  public void timeExample1(int iters) {
    initArrayI(a);
    initArrayI(b);
    initArrayI(c);
    for (int i = 0; i < iters; i++) {
      example1();
    }
  }

  public void timeExample2a(int iters) {
    initArrayI(b);
    for (int i = 0; i < iters; i++) {
      example2a(N, M);
    }
  }

  public void timeExample2b(int iters) {
    initArrayI(a);
    initArrayI(b);
    initArrayI(c);
    for (int i = 0; i < iters; i++) {
      example2b(N);
    }
  }

  public void timeExample3(int iters) {
    initArrayI(ia);
    initArrayI(ib);
    for (int i = 0; i < iters; i++) {
      example3(N);
    }
  }

  public void timeExample4a(int iters) {
    initArrayI(ia);
    initArrayI(ib);
    for (int i = 0; i < iters; i++) {
      example4a(N);
    }
  }

  public void timeExample4b(int iters) {
    initArrayI(a);
    initArrayI(b);
    initArrayI(c);
    for (int i = 0; i < iters; i++) {
      example4b(N);
    }
  }

  public void timeExample4c(int iters) {
    initArrayI(a);
    initArrayI(b);
    for (int i = 0; i < iters; i++) {
      example4c(N);
    }
  }

  public void timeExample5(int iters) {
    initArrayI(s.ca);
    for (int i = 0; i < iters; i++) {
      example5(N, s);
    }
  }

  public void timeExample7(int iters) {
    initArrayI(a);
    initArrayI(b);
    for (int i = 0; i < iters; i++) {
      example7(M);
    }
  }

  public void timeExample8(int iters) {
    initMatrixI(G);
    for (int i = 0; i < iters; i++) {
      example8(M);
    }
  }

  public void timeExample9(int iters) {
    initArrayI(ub);
    initArrayI(uc);
    for (int i = 0; i < iters; i++) {
      example9();
    }
  }

  public void timeExample10a(int iters) {
    initArrayS(sa);
    initArrayS(sb);
    initArrayS(sc);
    initArrayI(ia);
    initArrayI(ib);
    initArrayI(ic);
    for (int i = 0; i < iters; i++) {
      example10a(sa, sb, sc, ia, ib, ic);
    }
  }

  public void timeExample10b(int iters) {
    initArrayS(sb);
    initArrayI(ia);
    for (int i = 0; i < iters; i++) {
      example10b(sb, ia);
    }
  }

  public void timeExample11(int iters) {
    initArrayI(a);
    initArrayI(b);
    initArrayI(c);
    initArrayI(d);
    for (int i = 0; i < iters; i++) {
      example11();
    }
  }

  public void timeExample12(int iters) {
    initArrayI(a);
    for (int i = 0; i < iters; i++) {
      example12();
    }
  }

  public void timeExample13(int iters) {
    initMatrixI(matrixA_m_n);
    initMatrixI(matrixA_m_n);
    initArrayI(a);
    for (int i = 0; i < iters; i++) {
      example13(matrixA_m_n, matrixB_m_n, a);
    }
  }

  public void timeExample14(int iters) {
    initMatrixI(matrixA_n_m);
    initMatrixI(matrixA_n_m);
    initArrayI(a);
    for (int i = 0; i < iters; i++) {
      example14(matrixA_n_m, matrixB_n_m, a);
    }
  }

  public void timeExample21(int iters) {
    initArrayI(b);
    for (int i = 0; i < iters; i++) {
      example21(b, N);
    }
  }

  public void timeExample23(int iters) {
    initArrayC(usa);
    initArrayI(b);
    for (int i = 0; i < iters; i++) {
      example23(usa, b);
    }
  }

  public void timeExample24(int iters) {
    initArrayI(ic);
    initArrayF(fa);
    initArrayF(fb);
    for (int i = 0; i < iters; i++) {
      example24((short)N, (short)M);
    }
  }

  public void timeExample25(int iters) {
    initArrayF(da);
    initArrayF(db);
    initArrayF(dc);
    initArrayF(dd);
    initArrayI(dj);
    for (int i = 0; i < iters; i++) {
      example25();
    }
  }

  public static final int INIT_CONSTANT = 13;

  public static void initArrayI(int[] array) {
    for (int i = 0; i < array.length; i++) {
      array[i] = (i % INIT_CONSTANT);
    }
  }

  public static void initArrayF(float[] array) {
    for (int i = 0; i < array.length; i++) {
      array[i] = (float)(i % INIT_CONSTANT);
    }
  }

  public static void initArrayS(short[] array) {
    for (int i = 0; i < array.length; i++) {
      array[i] = (short)(i % INIT_CONSTANT);
    }
  }

  public static void initArrayC(char[] array) {
    for (int i = 0; i < array.length; i++) {
      array[i] = (char)(i % INIT_CONSTANT);
    }
  }

  public static void initMatrixI(int[][] matrix) {
    for (int i = 0; i < matrix.length; i++) {
      for (int j = 0; j < matrix[i].length; j++) {
        matrix[i][j] = ((i + j) % INIT_CONSTANT);
      }
    }
  }

// CHECKSTYLE.OFF: .*
  public boolean verifyGCCLoops() {
    int expected = 1825677;

    timeExample1(1);
    timeExample2a(1);
    timeExample2b(1);
    timeExample3(1);
    timeExample4a(1);
    timeExample4b(1);
    timeExample4c(1);
    timeExample5(1);
    timeExample7(1);
    timeExample8(1);
    timeExample9(1);
    timeExample10a(1);
    timeExample10b(1);
    timeExample11(1);
    timeExample12(1);
    timeExample13(1);
    timeExample14(1);
    timeExample21(1);
    timeExample23(1);
    timeExample24(1);
    timeExample25(1);
    int found = 0;

    for (int i = 0; i < N; i++) {
      found += usa[i] + sa[i] + sb[i] + sc[i] + ua[i] + ia[i] + ib[i] + ic[i] + ub[i] + uc[i]
            + (int)fa[i] + (int)fb[i] + (int)da[i] + (int)db[i] + (int)dc[i] + (int)dd[i]
            + (int)dj[i] + a[i] + b[i] + c[i] + d[i];
    }

    for (int i = 0; i < N; i++) {
      for (int j = 0; j < M; j++) {
        found += G[j][i] + matrixA_m_n[j][i] + matrixA_n_m[i][j];
      }
    }

    if (found != expected) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }
// CHECKSTYLE.ON: .*

  public static final int ITER_COUNT = 1000;

  public static void main(String[] args) {
    int rc = 0;
    GCCLoops obj = new GCCLoops();

    long before = System.currentTimeMillis();
    obj.timeExample1(ITER_COUNT);
    obj.timeExample2a(ITER_COUNT);
    obj.timeExample2b(ITER_COUNT);
    obj.timeExample3(ITER_COUNT);
    obj.timeExample4a(ITER_COUNT);
    obj.timeExample4b(ITER_COUNT);
    obj.timeExample4c(ITER_COUNT);
    obj.timeExample5(ITER_COUNT);
    obj.timeExample7(ITER_COUNT);
    obj.timeExample8(ITER_COUNT);
    obj.timeExample9(ITER_COUNT);
    obj.timeExample10a(ITER_COUNT);
    obj.timeExample10b(ITER_COUNT);
    obj.timeExample11(ITER_COUNT);
    obj.timeExample12(ITER_COUNT);
    obj.timeExample13(ITER_COUNT);
    obj.timeExample14(ITER_COUNT);
    obj.timeExample21(ITER_COUNT);
    obj.timeExample23(ITER_COUNT);
    obj.timeExample24(ITER_COUNT);
    obj.timeExample25(ITER_COUNT);
    long after = System.currentTimeMillis();

    if (!obj.verifyGCCLoops()) {
      rc++;
    }

    System.out.println("benchmarks/micro/GCCLoops: " + (after - before));
  }
}
