/*
 * Copyright (C) 2015 Linaro Limited. All rights received.
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

/*
 * Ported from:
 *   webkit/PerformanceTests/SunSpider/tests/sunspider-1.0.2/crypto-sha1.js
 *
 * A JavaScript implementation of the Secure Hash Algorithm, SHA-1, as defined
 * in FIPS PUB 180-1
 * Version 2.1a Copyright Paul Johnston 2000 - 2002.
 * Other contributors: Greg Holt, Andrew Kepert, Ydnar, Lostinet
 * Distributed under the BSD License
 * See http://pajhome.org.uk/crypt/md5 for details.
 */

/* Description:       Encrypts a text fragment using the SHA1 Secure Hash
 *                    Algorithm, producing a 160-bit hash value.
 * Main Focus:        Bit operations.
 *
 */

package benchmarks.algorithm;

import java.lang.System;

public class CryptoSHA1 {
  private static String TEXT;
  private static final String TEXT_FRAGMENT =
      "Two households, both alike in dignity,\n" +
      "In fair Verona, where we lay our scene,\n" +
      "From ancient grudge break to new mutiny,\n" +
      "Where civil blood makes civil hands unclean.\n" +
      "From forth the fatal loins of these two foes\n" +
      "A pair of star-cross'd lovers take their life;\n" +
      "Whole misadventured piteous overthrows\n" +
      "Do with their death bury their parents' strife.\n" +
      "The fearful passage of their death-mark'd love,\n" +
      "And the continuance of their parents' rage,\n" +
      "Which, but their children's end, nought could remove,\n" +
      "Is now the two hours' traffic of our stage;\n" +
      "The which if you with patient ears attend,\n" +
      "What here shall miss, our toil shall strive to mend.";

  private static final String SHA1_EXPECTED =
      "2524d264def74cce2498bf112bedf00e6c0b796d";

  // bits per input character. 8 - ASCII; 16 - Unicode
  private static final int CHRSZ = 8;
  private static final int MASK = (1 << CHRSZ) - 1;

  // to convert binary values into a hex string digest
  private static final boolean HEX_UPPER_CASE = false;
  private static final String HEX_CHARS =
      HEX_UPPER_CASE ? "0123456789ABCDEF" : "0123456789abcdef";

  static {
    TEXT = TEXT_FRAGMENT;

    for (int i = 0; i < 4; ++i) {
      TEXT += TEXT;
    }
  }

  /*
   * Convert a string to an array of little-endian words
   * If CHRSZ is ASCII, characters >255 have their hi-byte silently ignored.
   */
  private int[] str2binb(String str, int padding) {
    final int len = str.length() / 4;
    int paddedLen = len;
    final int rem = paddedLen % padding;

    if (padding != 0 && rem != 0) {
      paddedLen += padding - rem;
    }

    int bin[] = new int[paddedLen];

    for (int i = 0; i < str.length() * CHRSZ; i += CHRSZ) {
      bin[i >> 5] |= (str.charAt(i / CHRSZ) & MASK) << (32 - CHRSZ - i % 32);
    }

    return bin;
  }

  /* Default padding of 16 as in the original Javascript implementation. */
  private int[] str2binb(String str) {
    return str2binb(str, 16);
  }

  /* Convert an array of little-endian words to a hex string. */
  private String binb2hex(int bin[]) {
    String str = "";

    for (int i = 0; i < bin.length * 4; i++) {
      str += HEX_CHARS.charAt((bin[i >> 2] >> ((3 - i % 4) * 8 + 4)) & 0xF);
      str += HEX_CHARS.charAt((bin[i >> 2] >> ((3 - i % 4) * 8)) & 0xF);
    }

    return str;
  }

  /*
   * Bitwise rotate a 32-bit number to the left
   * TODO: Name
   */
  private int bitRotateLeft(int num, int cnt) {
    return (num << cnt) | (num >>> (32 - cnt));
  }

  /*
   * Perform the appropriate triplet combination function for the current
   * iteration
   */
  private int sha1FT(int t, int b, int c, int d) {
    if (t < 20) {
      return (b & c) | ((~b) & d);
    } else if (t < 40) {
      return b ^ c ^ d;
    } else if (t < 60) {
      return (b & c) | (b & d) | (c & d);
    } else {
      return b ^ c ^ d;
    }
  }

  /*
   * Determine the appropriate additive constant for the current iteration
   */
  private int sha1KT(int t) {
    return (t < 20) ? 1518500249 : ((t < 40) ? 1859775393 :
        ((t < 60) ? -1894007588 : -899497514));
  }

  private int[] coreSHA1(int x[], int len) {
    /* append padding */
    x[len >> 5] |= 0x80 << (24 - len % 32);
    x[(((len + 64) >> 9) << 4) + 15] = len;

    int w[] = new int[80];
    int a =  1732584193;
    int b = -271733879;
    int c = -1732584194;
    int d =  271733878;
    int e = -1009589776;

    for (int i = 0; i < x.length; i += 16) {
      final int olda = a;
      final int oldb = b;
      final int oldc = c;
      final int oldd = d;
      final int olde = e;

      for (int j = 0; j < 80; j++) {
        if (j < 16) {
          w[j] = x[i + j];
        } else {
          w[j] = bitRotateLeft(w[j - 3] ^ w[j - 8] ^ w[j - 14] ^ w[j - 16], 1);
        }
        int t = bitRotateLeft(a, 5) + sha1FT(j, b, c, d) + e + w[j] + sha1KT(j);

        e = d;
        d = c;
        c = bitRotateLeft(b, 30);
        b = a;
        a = t;
      }
      a = a + olda;
      b = b + oldb;
      c = c + oldc;
      d = d + oldd;
      e = e + olde;
    }
    return new int[] {a, b, c, d, e};
  }

  public String hexSHA1(String stringData) {
    int bin[] = str2binb(stringData);
    int sha1[] = coreSHA1(bin, stringData.length() * CHRSZ);
    String hex = binb2hex(sha1);
    return hex;
  }

  public void timeHexSHA1(int iters) {
    for (int i = 0; i < iters; i++) {
      hexSHA1(TEXT);
    }
  }

  public boolean verify() {
    String sha1Output = hexSHA1(TEXT);
    return sha1Output.equals(SHA1_EXPECTED);
  }

  public static void main(String argv[]) {
    CryptoSHA1 obj = new CryptoSHA1();
    final long before = System.currentTimeMillis();
    obj.timeHexSHA1(1808);
    final long after = System.currentTimeMillis();
    obj.verify();
    System.out.println("CryptoSHA1: " + (after - before));
  }
}
