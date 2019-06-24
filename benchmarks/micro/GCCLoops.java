/// This test contains some of the loops from the GCC vectrorizer example page [1].
/// Dorit Nuzman who developed the gcc vectorizer said that we can use them in our test suite.
///
/// [1] - http://gcc.gnu.org/projects/tree-ssa/vectorization.html

package benchmarks.micro;

import java.util.Arrays;

// CHECKSTYLE.OFF: .*
public class GCCLoops {
private static final int N = 1024;
private static final int M = 32;
private static final int K = 4;

char[] usa = new char[N];
short[] sb = new short[N];
short[] sc = new short[N];
float[] fa = new float[N];
float[] fb = new float[N];
float[] fc = new float[N];
float[] fd = new float[N];

public class StructAType {
  public int[] ca = new int[N];
}

StructAType s = new StructAType();

int[] a = new int[N*2];
int[] b = new int[N*2];
int[] c = new int[N*2];
int[] d = new int[N*2];

// NOTE: Be careful with the matrices sizes as they might not fit into caches
// and make micro-benchmarks unstable.
private static final int MAT_N = 128;
private static final int MAT_M = 32;
int[][] matrixA_m_n = new int[MAT_M*2][MAT_N*2];
int[][] matrixB_m_n = new int[MAT_M*2][MAT_N*2];
int[][] matrixA_n_m = new int[MAT_N*2][MAT_M*2];
int[][] matrixB_n_m = new int[MAT_N*2][MAT_M*2];
int[][] G = new int[MAT_M][MAT_N];

// Arrays used for output.
int[] out = new int[N*2];
int[] iout = out;
int[] out1 = out;
int[] out2 = new int[N*2];
short[] sout = new short[N];

public void setupArrays() {
  initArrayI(a);
  initArrayI(b);
  initArrayI(c);
  initArrayS(sb);
  initArrayS(sc);
  initArrayF(fa);
  initArrayF(fb);
  initArrayF(fc);
  initArrayF(fd);
  initArrayC(usa);
}

public void setupMatrices() {
  initMatrixI(matrixA_m_n);
  initMatrixI(matrixB_m_n);
  initMatrixI(matrixA_n_m);
  initMatrixI(matrixB_n_m);
}

void example1 (int[] b, int[] c, int[] out) {
  int i;

  for (i=0; i<N; i++){
    out[i] = b[i] + c[i];
  }
}

void example2a (int n, int x, int[] out) {
   int i;

   /* feature: support for unknown loop bound  */
   /* feature: support for loop invariants  */
   for (i=0; i<n; i++) {
      out[i] = x;
   }
}

void example2b (int[] b, int[] c, int n, int[] out) {
  int i = 0;
  int k = n;
   /* feature: general loop exit condition  */
   /* feature: support for bitwise operations  */
   while (k-- > 0){
      out[i] = b[i]&c[i]; i++;
   }
}


void example3 (int[] b, int n, int[] out) {
  int i = 0;
  int k = n;
   /* feature: support for (aligned) pointer accesses.  */
   while (k-- > 0){
      out[i] = b[i];
      i++;
   }
}

void example4a (int[] b, int n, int[] out) {
  int i = 0;
  int k = n;
   /* feature: support for (aligned) pointer accesses  */
   /* feature: support for constants  */
   while (k-- > 0){
      out[i] = b[i] + 5;
      i++;
   }
}

void example4b (int[] b, int[] c, int n, int[] out) {
   int i;

   /* feature: support for read accesses with a compile time known misalignment  */
   for (i=0; i<n; i++){
      out[i] = b[i+1] + c[i+3];
   }
}

void example4c (int[] a, int n, int[] out) {
   int i;
    final int MAX = 4;
   /* feature: support for if-conversion  */
   for (i=0; i<n; i++){
      int j = a[i];
      out[i] = (j > MAX ? MAX : 0);
   }
}

void  example5 (int n, StructAType s) {
  int i;
  for (i = 0; i < n; i++) {
    /* feature: support for alignable struct access  */
    s.ca[i] = 5;
  }
}

void  example7 (int[] b, int x, int[] out) {
   int i;

   /* feature: support for read accesses with an unknown misalignment  */
   for (i=0; i<N; i++){
      out[i] = b[i+x];
   }
}

void example8 (int[][] G, int x) {
   int i,j;

   /* feature: support for multidimensional arrays  */
   for (i=0; i<MAT_M; i++) {
     for (j=0; j<MAT_N; j++) {
       G[i][j] = x;
     }
   }
}


int example9 (int[] b, int[] c, int[] out) {
  int i;

  /* feature: support summation reduction.
     note: in case of floats use -funsafe-math-optimizations  */
  int diff = 0;
  for (i = 0; i < N; i++) {
    diff += (b[i] - c[i]);
  }

  // Inserted to avoid DCE removing the whole loop.
  out[0] = diff;
  return diff;
}


/* feature: support data-types of different sizes.
   Currently only a single vector-size per target is supported;
   it can accommodate n elements such that n = vector-size/element-size
   (e.g, 4 ints, 8 shorts, or 16 chars for a vector of size 16 bytes).
   A combination of data-types of different sizes in the same loop
   requires special handling. This support is now present in mainline,
   and also includes support for type conversions.  */
void example10a(short[] sb, short[] sc, int[] b, int[] c, int[] out1, short[] out2) {
  int i;
  for (i = 0; i < N; i++) {
    out1[i] = b[i] + c[i];
    out2[i] = (short)(sb[i] + sc[i]);
  }
}

void example10b(short[] sb, int[] out) {
  int i;
  for (i = 0; i < N; i++) {
    out[i] = (int) sb[i];
  }
}

/* feature: support strided accesses - the data elements
   that are to be operated upon in parallel are not consecutive - they
   are accessed with a stride > 1 (in the example, the stride is 2):  */
void example11(int[] b, int[] c, int[] out1, int[] out2) {
   int i;
  for (i = 0; i < N/2; i++){
    out1[i] = b[2*i+1] * c[2*i+1] - b[2*i] * c[2*i];
    out2[i] = b[2*i] * c[2*i+1] + b[2*i+1] * c[2*i];
  }
}


void example12(int[] out) {
  for (int i = 0; i < N; i++) {
    out[i] = i;
  }
}

void example13(int[][] A, int[][] B, int[] out) {
  int i,j;
  for (i = 0; i < MAT_M; i++) {
    int diff = 0;
    for (j = 0; j < MAT_N; j+=8) {
      diff += (A[i][j] - B[i][j]);
    }
    out[i] = diff;
  }
}

void example14(int[][] in, int[][] coeff, int[] out) {
  int k,j,i=0;
  for (k = 0; k < K; k++) {
    int sum = 0;
    for (j = 0; j < MAT_M; j++)
      for (i = 0; i < MAT_N; i++)
          sum += in[i+k][j] * coeff[i][j];

    out[k] = sum;
  }

}


void example21(int[] b, int n, int[] out) {
  int i, a = 0;

  for (i = n-1; i >= 0; i--)
    a += b[i];

  out[0] = a;
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


void example24 (float[] fa, float[] fb, short x, short y, int[] out)
{
  int i;
  for (i = 0; i < N; i++)
    out[i] = fa[i] < fb[i] ? x : y;
}


void example25 (float[] fa, float[] fb, float[] fc, float[] fd, int[] out)
{
  int i;
  char x, y;
  for (i = 0; i < N; i++)
    {
      x = (char)(fa[i] < fb[i] ? 1 : 0);
      y = (char)(fc[i] < fd[i] ? 1 : 0);
      out[i] = x & y;
    }
}
// CHECKSTYLE.ON: .*

  public void timeExample1(int iters) {
    for (int i = 0; i < iters; i++) {
      example1(b, c, out);
    }
  }

  public boolean verifyExample1() {
    Arrays.fill(out, 0);
    timeExample1(1);
    final int hashCode = Arrays.hashCode(out);
    final int expectedHashCode = -858163061;
    return hashCode == expectedHashCode;
  }

  public void timeExample2a(int iters) {
    for (int i = 0; i < iters; i++) {
      example2a(N, M, out);
    }
  }

  public boolean verifyExample2a() {
    Arrays.fill(out, 0);
    timeExample2a(1);
    final int hashCode = Arrays.hashCode(out);
    final int expectedHashCode = 620167169;
    return hashCode == expectedHashCode;
  }

  public void timeExample2b(int iters) {
    for (int i = 0; i < iters; i++) {
      example2b(b, c, N, out);
    }
  }

  public boolean verifyExample2b() {
    Arrays.fill(out, 0);
    timeExample2b(1);
    final int hashCode = Arrays.hashCode(out);
    final int expectedHashCode = -1067172794;
    return hashCode == expectedHashCode;
  }

  public void timeExample3(int iters) {
    for (int i = 0; i < iters; i++) {
      example3(b, N, out);
    }
  }

  public boolean verifyExample3() {
    Arrays.fill(out, 0);
    timeExample3(1);
    final int hashCode = Arrays.hashCode(out);
    final int expectedHashCode = -1067172794;
    return hashCode == expectedHashCode;
  }

  public void timeExample4a(int iters) {
    for (int i = 0; i < iters; i++) {
      example4a(b, N, out);
    }
  }

  public boolean verifyExample4a() {
    Arrays.fill(out, 0);
    timeExample4a(1);
    final int hashCode = Arrays.hashCode(out);
    final int expectedHashCode = -368214970;
    return hashCode == expectedHashCode;
  }

  public void timeExample4b(int iters) {
    for (int i = 0; i < iters; i++) {
      example4b(b, c, N, out);
    }
  }

  public boolean verifyExample4b() {
    Arrays.fill(out, 0);
    timeExample4b(1);
    final int hashCode = Arrays.hashCode(out);
    final int expectedHashCode = 2046858443;
    return hashCode == expectedHashCode;
  }

  public void timeExample4c(int iters) {
    for (int i = 0; i < iters; i++) {
      example4c(a, N, out);
    }
  }

  public boolean verifyExample4c() {
    Arrays.fill(out, 0);
    timeExample4c(1);
    final int hashCode = Arrays.hashCode(out);
    final int expectedHashCode = -1381105915;
    return hashCode == expectedHashCode;
  }

  public void timeExample5(int iters) {
    for (int i = 0; i < iters; i++) {
      example5(N, s);
    }
  }

  public boolean verifyExample5() {
    Arrays.fill(s.ca, 0);
    timeExample5(1);
    final int hashCode = Arrays.hashCode(s.ca);
    final int expectedHashCode = -2086617087;
    return hashCode == expectedHashCode;
  }

  public void timeExample7(int iters) {
    for (int i = 0; i < iters; i++) {
      example7(b, M, out);
    }
  }

  public boolean verifyExample7() {
    Arrays.fill(out, 0);
    timeExample7(1);
    final int hashCode = Arrays.hashCode(out);
    final int expectedHashCode = 1762596313;
    return hashCode == expectedHashCode;
  }

  public void timeExample8(int iters) {
    for (int i = 0; i < iters; i++) {
      example8(G, M);
    }
  }

  public boolean verifyExample8() {
    for (int i = 0; i < G.length; ++i) {
      Arrays.fill(G[i], 0);
    }

    timeExample8(1);

    int[] hashCodes = new int[G.length];
    for (int i = 0; i < G.length; ++i) {
      hashCodes[i] = Arrays.hashCode(out);
    }
    final int hashCode = Arrays.hashCode(hashCodes);
    final int expectedHashCode = -1269715455;
    return hashCode == expectedHashCode;
  }

  public void timeExample9(int iters) {
    for (int i = 0; i < iters; i++) {
      example9(b, c, out);
    }
  }

  public boolean verifyExample9() {
    Arrays.fill(out, 0);
    timeExample9(1);
    final int hashCode = Arrays.hashCode(out);
    final int expectedHashCode = -1276182527;
    return hashCode == expectedHashCode;
  }

  public void timeExample10a(int iters) {
    for (int i = 0; i < iters; i++) {
      example10a(sb, sc, b, c, iout, sout);
    }
  }

  public boolean verifyExample10a() {
    Arrays.fill(iout, 0);
    Arrays.fill(sout, (short)0);
    timeExample10a(1);
    final int hashCode1 = Arrays.hashCode(iout);
    final int hashCode2 = Arrays.hashCode(sout);
    final int expectedHashCode1 = -858163061;
    final int expectedHashCode2 = 259455115;
    return hashCode1 == expectedHashCode1 && hashCode2 == expectedHashCode2;
  }

  public void timeExample10b(int iters) {
    for (int i = 0; i < iters; i++) {
      example10b(sb, out);
    }
  }

  public boolean verifyExample10b() {
    Arrays.fill(out, 0);
    timeExample10b(1);
    final int hashCode = Arrays.hashCode(out);
    final int expectedHashCode = -1067172794;
    return hashCode == expectedHashCode;
  }

  public void timeExample11(int iters) {
    for (int i = 0; i < iters; i++) {
      example11(b, c, out1, out2);
    }
  }

  public boolean verifyExample11() {
    Arrays.fill(out1, 0);
    Arrays.fill(out2, 0);
    timeExample11(1);
    final int hashCode1 = Arrays.hashCode(out1);
    final int hashCode2 = Arrays.hashCode(out2);
    final int expectedHashCode1 = 1170079258;
    final int expectedHashCode2 = 1347691585;
    return hashCode1 == expectedHashCode1 && hashCode2 == expectedHashCode2;
  }

  public void timeExample12(int iters) {
    for (int i = 0; i < iters; i++) {
      example12(out);
    }
  }

  public boolean verifyExample12() {
    Arrays.fill(out, 0);
    timeExample12(1);
    final int hashCode = Arrays.hashCode(out);
    final int expectedHashCode = -1796537855;
    return hashCode == expectedHashCode;
  }

  public void timeExample13(int iters) {
    for (int i = 0; i < iters; i++) {
      example13(matrixA_m_n, matrixB_m_n, out);
    }
  }

  public boolean verifyExample13() {
    Arrays.fill(out, 0);
    timeExample13(1);
    final int hashCode = Arrays.hashCode(out);
    final int expectedHashCode = -1276182527;
    return hashCode == expectedHashCode;
  }

  public void timeExample14(int iters) {
    for (int i = 0; i < iters; i++) {
      example14(matrixA_n_m, matrixB_n_m, out);
    }
  }

  public boolean verifyExample14() {
    Arrays.fill(out, 0);
    timeExample14(1);
    final int hashCode = Arrays.hashCode(out);
    final int expectedHashCode = 248257147;
    return hashCode == expectedHashCode;
  }

  public void timeExample21(int iters) {
    for (int i = 0; i < iters; i++) {
      example21(b, N, out);
    }
  }

  public boolean verifyExample21() {
    Arrays.fill(out, 0);
    timeExample21(1);
    final int hashCode = Arrays.hashCode(out);
    final int expectedHashCode = 228894192;
    return hashCode == expectedHashCode;
  }

  public void timeExample23(int iters) {
    for (int i = 0; i < iters; i++) {
      example23(usa, out);
    }
  }

  public boolean verifyExample23() {
    Arrays.fill(out, 0);
    timeExample23(1);
    final int hashCode = Arrays.hashCode(out);
    final int expectedHashCode = 1545084673;
    return hashCode == expectedHashCode;
  }

  public void timeExample24(int iters) {
    for (int i = 0; i < iters; i++) {
      example24(fa, fb, (short)N, (short)M, out);
    }
  }

  public boolean verifyExample24() {
    Arrays.fill(out, 0);
    timeExample24(1);
    final int hashCode = Arrays.hashCode(out);
    final int expectedHashCode = 620167169;
    return hashCode == expectedHashCode;
  }

  public void timeExample25(int iters) {
    for (int i = 0; i < iters; i++) {
      example25(fa, fb, fc, fd, out);
    }
  }

  public boolean verifyExample25() {
    Arrays.fill(out, 0);
    timeExample25(1);
    final int hashCode = Arrays.hashCode(out);
    final int expectedHashCode = -1276182527;
    return hashCode == expectedHashCode;
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

  public static final int ITER_COUNT = 1000;

  public static void main(String[] args) {
    int rc = 0;
    GCCLoops obj = new GCCLoops();
    obj.setupArrays();
    obj.setupMatrices();

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

    if (!obj.verifyExample1()) {
      ++rc;
    }
    if (!obj.verifyExample2a()) {
      ++rc;
    }
    if (!obj.verifyExample2b()) {
      ++rc;
    }
    if (!obj.verifyExample3()) {
      ++rc;
    }
    if (!obj.verifyExample4a()) {
      ++rc;
    }
    if (!obj.verifyExample4b()) {
      ++rc;
    }
    if (!obj.verifyExample4c()) {
      ++rc;
    }
    if (!obj.verifyExample5()) {
      ++rc;
    }
    if (!obj.verifyExample7()) {
      ++rc;
    }
    if (!obj.verifyExample8()) {
      ++rc;
    }
    if (!obj.verifyExample9()) {
      ++rc;
    }
    if (!obj.verifyExample10a()) {
      ++rc;
    }
    if (!obj.verifyExample10b()) {
      ++rc;
    }
    if (!obj.verifyExample11()) {
      ++rc;
    }
    if (!obj.verifyExample12()) {
      ++rc;
    }
    if (!obj.verifyExample13()) {
      ++rc;
    }
    if (!obj.verifyExample14()) {
      ++rc;
    }
    if (!obj.verifyExample21()) {
      ++rc;
    }
    if (!obj.verifyExample23()) {
      ++rc;
    }
    if (!obj.verifyExample24()) {
      ++rc;
    }
    if (!obj.verifyExample25()) {
      ++rc;
    }

    System.out.println("benchmarks/micro/GCCLoops: " + (after - before));
  }
}
