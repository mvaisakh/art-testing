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
public class TestRGBToCmyk
{
  static final int LENGTH = 256 * 1024;
  static byte input[] = new byte[LENGTH*3];
  static byte output[] = new byte[LENGTH*4];

  public static void TestRGBToCmykInit()
  {
    for (int i = 0; i < LENGTH*3; i++) {
      input[i] = (byte)i;
    }
  }

  public static void RGBToCmyk(byte[] rgb, byte[] cmyk, int cnt)
  {
    int i;
    int c, m, y, k;
    for (i = 0; i < cnt; i++) {
      /* calculate complementary colors */
      c = 255 - rgb[i*3];
      m = 255 - rgb[i*3+1];
      y = 255 - rgb[i*3+2];
      /* find the black level k */
      k = Math.min(Math.min(c, m), y);
      /* correct complementary color lever based on k */
      cmyk[i*4] = (byte)(c - k);
      cmyk[i*4+1] = (byte)(m - k);
      cmyk[i*4+2] = (byte)(y - k);
      cmyk[i*4+3] = (byte)(k);
    }
  }

  public void timeRGBToCmyk(int iters) {
    TestRGBToCmykInit();
    for (int i = 0; i < iters; i++) {
      RGBToCmyk(input, output, LENGTH);
    }
  }

  public boolean verifyRGBToCmyk() {
    TestRGBToCmykInit();
    RGBToCmyk(input, output, LENGTH);

    int expected = -268288;
    int found = 0;
    for (int i = 0; i < LENGTH * 3; i++) {
      found += input[i];
    }
    for (int i = 0; i < LENGTH * 4; i++) {
      found += output[i];
    }

    if (found != expected) {
      System.out.println("ERROR: Expected " + expected + " but found " + found);
      return false;
    }
    return true;
  }
  // CHECKSTYLE.ON: .*

  public static final int ITER_COUNT = 150;

  public static void main(String[] argv) {
    int rc = 0;
    TestRGBToCmyk obj = new TestRGBToCmyk();

    long before = System.currentTimeMillis();
    obj.timeRGBToCmyk(ITER_COUNT);
    long after = System.currentTimeMillis();
    System.out.println("benchmarks/testsimd/TestRGBToCmyk: " + (after - before));

    if (!obj.verifyRGBToCmyk()) {
      rc++;
    }
    System.exit(rc);
  }

}
