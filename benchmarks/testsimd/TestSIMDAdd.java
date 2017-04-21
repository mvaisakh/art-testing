/*
 * Copyright (C) 2016 Linaro Limited. All rights received.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package benchmarks.testsimd;

// CHECKSTYLE.OFF: .*
public class TestSIMDAdd {
  static final int LENGTH = 1024 * 256;
  static int [] a = new int[LENGTH];
  static int [] b = new int[LENGTH];
  static int [] c = new int[LENGTH];
  static short [] sa = new short[LENGTH];
  static short [] sb = new short[LENGTH];
  static short [] sc = new short[LENGTH];
  static byte [] ba = new byte[LENGTH];
  static byte [] bb = new byte[LENGTH];
  static byte [] bc = new byte[LENGTH];

  public static void TestSIMDAddInit() {
    for (int i = 0; i < LENGTH; i++) {
       a[i] = i + 3;
       b[i] = i + 2;
       c[i] = i + 1;
       sa[i] = (short)(i + 3);
       sb[i] = (short)(i + 2);
       sc[i] = (short)(i + 1);
       ba[i] = (byte)(i + 3);
       bb[i] = (byte)(i + 2);
       bc[i] = (byte)(i + 1);
    }
  }

  public static void vectAddInt() {
    for (int i = 0; i < LENGTH; i++) {
      c[i] = a[i] + b[i];
    }
  }

  public static void vectAddShort() {
    for (int i = 0; i < LENGTH; i++) {
      sc[i] = (short)(sa[i] + sb[i]);
    }
  }

  public static void vectAddByte() {
    for (int i = 0; i < LENGTH; i++) {
      bc[i] = (byte)(ba[i] + bb[i]);
    }
  }

  public void timeVectAddByte(int iters) {
    TestSIMDAddInit();
    for (int i = 0; i < iters; i++) {
      vectAddByte();
    }
  }

  public void timeVectAddShort(int iters) {
    TestSIMDAddInit();
    for (int i = 0; i < iters; i++) {
      vectAddShort();
    }
  }

  public void timeVectAddInt(int iters) {
    TestSIMDAddInit();
    for (int i = 0; i < iters; i++) {
      vectAddInt();
    }
  }

  public boolean verifySIMDAdd() {
    TestSIMDAddInit();
    vectAddByte();
    vectAddShort();
    vectAddInt();

    int expected = 1572864;
    int found = 0;
    for (int i = 0; i < LENGTH; i++) {
      found += a[i] + b[i] + c[i] + sa[i] + sb[i] + sc[i] + ba[i] + bb[i] + bc[i];
    }

    if (found != expected) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }

    return true;
  }
  // CHECKSTYLE.ON: .*

  public static final int ITER_COUNT = 300;

  public static void main(String[] argv) {
    int rc = 0;
    TestSIMDAdd obj = new TestSIMDAdd();

    long before = System.currentTimeMillis();
    obj.timeVectAddByte(ITER_COUNT);
    long after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDAddByte: " + (after - before));

    before = System.currentTimeMillis();
    obj.timeVectAddShort(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDAddShort: " + (after - before));

    before = System.currentTimeMillis();
    obj.timeVectAddInt(ITER_COUNT);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestSIMDAddInt: " + (after - before));

    if (!obj.verifySIMDAdd()) {
      rc++;
    }
    System.exit(rc);
  }

}
