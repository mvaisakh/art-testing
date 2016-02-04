/*
 * Copyright (c) 2000-2015 The Legion of the Bouncy Castle Inc. (http://www.bouncycastle.org)
 *
 * Modifications copyright (c) 2015 Linaro Limited.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Description:     Check for regressions effecting Bouncy Castle SHA1Digest processing.
 * Main Focus:      Bitfield rotations.
 * Secondary Focus: Loop optimizations.
 */

package benchmarks.micro;

import java.nio.ByteBuffer;

public class BitfieldRotate {
  private static final String SOURCE_Text = "That though I loved them for their faults\n" +
                                            "As much as for their good,\n" +
                                            "My friends were enemies on stilts\n" +
                                            "With their heads in a cunning cloud.\n";

  private int h1;
  private int h2;
  private int h3;
  private int h4;
  private int h5;
  private int[] x = new int[80];
  private int xOff;

  //
  // Additive constants
  //
  private static final int    y1 = 0x5a827999;
  private static final int    y2 = 0x6ed9eba1;
  private static final int    y3 = 0x8f1bbcdc;
  private static final int    y4 = 0xca62c1d6;

  private int ffunc(
      int    u,
      int    v,
      int    w) {
    return ((u & v) | ((~u) & w));
  }

  private int hfunc(
      int    u,
      int    v,
      int    w) {
    return (u ^ v ^ w);
  }

  private int gfunc(
      int    u,
      int    v,
      int    w) {
    return ((u & v) | (u & w) | (v & w));
  }

  protected void processBlock() {
    //
    // expand 16 word block into 80 word block.
    //
    for (int i = 16; i < 80; i++) {
      int t = x[i - 3] ^ x[i - 8] ^ x[i - 14] ^ x[i - 16];
      x[i] = t << 1 | t >>> 31;
    }

    //
    // set up working variables.
    //
    int     a = h1;
    int     b = h2;
    int     c = h3;
    int     d = h4;
    int     e = h5;

    //
    // round 1
    //
    int idx = 0;

    for (int j = 0; j < 4; j++) {
      // E = rotateLeft(a, 5) + ffunc(b, c, d) + E + x[idx++] + y1
      // B = rotateLeft(b, 30)
      e += (a << 5 | a >>> 27) + ffunc(b, c, d) + x[idx++] + y1;
      b = b << 30 | b >>> 2;

      d += (e << 5 | e >>> 27) + ffunc(a, b, c) + x[idx++] + y1;
      a = a << 30 | a >>> 2;

      c += (d << 5 | d >>> 27) + ffunc(e, a, b) + x[idx++] + y1;
      e = e << 30 | e >>> 2;

      b += (c << 5 | c >>> 27) + ffunc(d, e, a) + x[idx++] + y1;
      d = d << 30 | d >>> 2;

      a += (b << 5 | b >>> 27) + ffunc(c, d, e) + x[idx++] + y1;
      c = c << 30 | c >>> 2;
    }

    //
    // round 2
    //
    for (int j = 0; j < 4; j++) {
      // E = rotateLeft(a, 5) + hfunc(b, c, d) + E + x[idx++] + y2
      // B = rotateLeft(b, 30)
      e += (a << 5 | a >>> 27) + hfunc(b, c, d) + x[idx++] + y2;
      b = b << 30 | b >>> 2;

      d += (e << 5 | e >>> 27) + hfunc(a, b, c) + x[idx++] + y2;
      a = a << 30 | a >>> 2;

      c += (d << 5 | d >>> 27) + hfunc(e, a, b) + x[idx++] + y2;
      e = e << 30 | e >>> 2;

      b += (c << 5 | c >>> 27) + hfunc(d, e, a) + x[idx++] + y2;
      d = d << 30 | d >>> 2;

      a += (b << 5 | b >>> 27) + hfunc(c, d, e) + x[idx++] + y2;
      c = c << 30 | c >>> 2;
    }

    //
    // round 3
    //
    for (int j = 0; j < 4; j++) {
      // E = rotateLeft(a, 5) + gfunc(b, c, d) + E + x[idx++] + y3
      // B = rotateLeft(b, 30)
      e += (a << 5 | a >>> 27) + gfunc(b, c, d) + x[idx++] + y3;
      b = b << 30 | b >>> 2;

      d += (e << 5 | e >>> 27) + gfunc(a, b, c) + x[idx++] + y3;
      a = a << 30 | a >>> 2;

      c += (d << 5 | d >>> 27) + gfunc(e, a, b) + x[idx++] + y3;
      e = e << 30 | e >>> 2;

      b += (c << 5 | c >>> 27) + gfunc(d, e, a) + x[idx++] + y3;
      d = d << 30 | d >>> 2;

      a += (b << 5 | b >>> 27) + gfunc(c, d, e) + x[idx++] + y3;
      c = c << 30 | c >>> 2;
    }

    //
    // round 4
    //
    for (int j = 0; j <= 3; j++) {
      // E = rotateLeft(a, 5) + hfunc(b, c, d) + E + x[idx++] + y4
      // B = rotateLeft(b, 30)
      e += (a << 5 | a >>> 27) + hfunc(b, c, d) + x[idx++] + y4;
      b = b << 30 | b >>> 2;

      d += (e << 5 | e >>> 27) + hfunc(a, b, c) + x[idx++] + y4;
      a = a << 30 | a >>> 2;

      c += (d << 5 | d >>> 27) + hfunc(e, a, b) + x[idx++] + y4;
      e = e << 30 | e >>> 2;

      b += (c << 5 | c >>> 27) + hfunc(d, e, a) + x[idx++] + y4;
      d = d << 30 | d >>> 2;

      a += (b << 5 | b >>> 27) + hfunc(c, d, e) + x[idx++] + y4;
      c = c << 30 | c >>> 2;
    }

    h1 += a;
    h2 += b;
    h3 += c;
    h4 += d;
    h5 += e;

    //
    // reset start of the buffer.
    //
    xOff = 0;
    for (int i = 0; i < 16; i++) {
      x[i] = 0;
    }
  }

  public void timeSHA1DigestProcessBlock(int iterations) {
    for (int i = 0; i < iterations; i++) {
      h1 = h2 = h3 = h4 = h5 = 0;
      processBlock();
    }
  }

  public boolean verifySHa1DigestProcessBlock() {
    byte[] buf = SOURCE_Text.getBytes();
    for (int i = 0; i < 80; i++) {
      x[i] = buf[i];
    }
    processBlock();
    return h1 == 1347341312 &&
           h2 == -1669350125 &&
           h3 == -362661148 &&
           h4 == 1604346378 &&
           h5 == -833338986;
  }

  /**
   * Integer rotate right patterns.
   */

  int resultIntegerRightRegVCSubV;
  int resultIntegerRightRegVNegV;

  public static int rotateIntegerRightRegVCSubV(int value, int distance) {
    return (value >>> distance) | (value << (32 - distance));
  }

  public static int rotateIntegerRightRegVNegV(int value, int distance) {
    return (value >>> distance) | (value << -distance);
  }

  public void timeIntegerRotateRight(int iterations) {
    for (int i = 0; i < iterations; i++) {
      for (int distance = 0; distance < Integer.SIZE; distance++) {
        resultIntegerRightRegVCSubV += rotateIntegerRightRegVCSubV(0xCAFEBABE, distance);
        resultIntegerRightRegVNegV += rotateIntegerRightRegVNegV(0xCAFEBABE, distance);
      }
    }
  }

  public boolean verifyIntegerRotateRight() {
    return (rotateIntegerRightRegVCSubV(0xCAFEBABE, 16) == 0xBABECAFE) &&
           (rotateIntegerRightRegVNegV(0xCAFEBABE, -4) == 0xAFEBABEC);
  }

  /**
   * Integer rotate left patterns.
   */

  int resultIntegerLeftRegCSubVV;
  int resultIntegerLeftRegNegVV;

  public static int rotateIntegerLeftRegCSubVV(int value, int distance) {
    return (value >>> (32 - distance)) | (value << distance);
  }

  public static int rotateIntegerLeftRegNegVV(int value, int distance) {
    return (value >>> -distance) | (value << distance);
  }

  public void timeIntegerRotateLeft(int iterations) {
    for (int i = 0; i < iterations; i++) {
      for (int distance = 0; distance < Integer.SIZE; distance++) {
        resultIntegerLeftRegCSubVV += rotateIntegerLeftRegCSubVV(0xCAFEBABE, distance);
        resultIntegerLeftRegNegVV += rotateIntegerLeftRegNegVV(0xCAFEBABE, distance);
      }
    }
  }

  public boolean verifyIntegerRotateLeft() {
    return (rotateIntegerLeftRegCSubVV(0xCAFEBABE, 4) == 0xAFEBABEC) &&
           (rotateIntegerLeftRegNegVV(0xCAFEBABE, -4) == 0xECAFEBAB);
  }

  /**
   * Long rotate right patterns.
   */

  int resultLongRightRegVCSubV;
  int resultLongRightRegVNegV;

  public static long rotateLongRightRegVCSubV(long value, int distance) {
    return (value >>> distance) | (value << (64 - distance));
  }

  public static long rotateLongRightRegVNegV(long value, int distance) {
    return (value >>> distance) | (value << -distance);
  }

  public void timeLongRotateRight(int iterations) {
    for (int i = 0; i < iterations; i++) {
      for (int distance = 0; distance < Long.SIZE; distance++) {
        resultLongRightRegVCSubV += rotateLongRightRegVCSubV(0xCAFEBABEBAADF00DL, distance);
        resultLongRightRegVNegV += rotateLongRightRegVNegV(0xCAFEBABEBAADF00DL, distance);
      }
    }
  }

  public boolean verifyLongRotateRight() {
    return (rotateLongRightRegVCSubV(0xCAFEBABEBAADF00DL, 32) == 0xBAADF00DCAFEBABEL) &&
           (rotateLongRightRegVNegV(0xCAFEBABEBAADF00DL, -16) == 0xBABEBAADF00DCAFEL);
  }

  /**
   * Long rotate left patterns.
   */

  int resultLongLeftRegCSubVV;
  int resultLongLeftRegNegVV;

  public static long rotateLongLeftRegCSubVV(long value, int distance) {
    return (value >>> (64 - distance)) | (value << distance);
  }

  public static long rotateLongLeftRegNegVV(long value, int distance) {
    return (value >>> -distance) | (value << distance);
  }

  public void timeLongRotateLeft(int iterations) {
    for (int i = 0; i < iterations; i++) {
      for (int distance = 0; distance < Long.SIZE; distance++) {
        resultLongLeftRegCSubVV += rotateLongLeftRegCSubVV(0xCAFEBABEBAADF00DL, distance);
        resultLongLeftRegNegVV += rotateLongLeftRegNegVV(0xCAFEBABEBAADF00DL, distance);
      }
    }
  }

  public boolean verifyLongRotateLeft() {
    return (rotateLongLeftRegCSubVV(0xCAFEBABEBAADF00DL, 4) == 0xAFEBABEBAADF00DCL) &&
           (rotateLongLeftRegNegVV(0xCAFEBABEBAADF00DL, -16) == 0xF00DCAFEBABEBAADL);
  }

  public static void main(String[] args) {
    BitfieldRotate br = new BitfieldRotate();
    long before = System.currentTimeMillis();
    br.timeSHA1DigestProcessBlock(1000);
    br.timeIntegerRotateRight(1000);
    br.timeIntegerRotateLeft(1000);
    br.timeLongRotateRight(1000);
    br.timeLongRotateLeft(1000);
    long after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/BitfieldRotate: " + (after - before));
  }
}

