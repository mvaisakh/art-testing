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
 *   webkit/PerformanceTests/SunSpider/tests/sunspider-1.0.2/crypto-md5.js
 *
 * A JavaScript implementation of the RSA Data Security, Inc. MD5 Message
 * Digest Algorithm, as defined in RFC 1321.
 * Version 2.1 Copyright (C) Paul Johnston 1999 - 2002.
 * Other contributors: Greg Holt, Andrew Kepert, Ydnar, Lostinet
 * Distributed under the BSD License
 * See http://pajhome.org.uk/crypt/md5 for more info.
 */

/* Description:       Encrypts a text fragment using the MD5 message-digest
 *                    algorithm, producing a 128-bit hash value.
 * Main Focus:        Bit operations.
 *
 */

package benchmarks.algorithm;

import java.lang.System;

public class CryptoMD5 {
  private static String TEXT;
  private static final String TEXT_FRAGMENT =
      "Rebellious subjects, enemies to peace,\n" +
      "Profaners of this neighbour-stained steel,--\n" +
      "Will they not hear? What, ho! you men, you beasts,\n" +
      "That quench the fire of your pernicious rage\n" +
      "With purple fountains issuing from your veins,\n" +
      "On pain of torture, from those bloody hands\n" +
      "Throw your mistemper'd weapons to the ground,\n" +
      "And hear the sentence of your moved prince.\n" +
      "Three civil brawls, bred of an airy word,\n" +
      "By thee, old Capulet, and Montague,\n" +
      "Have thrice disturb'd the quiet of our streets,\n" +
      "And made Verona's ancient citizens\n" +
      "Cast by their grave beseeming ornaments,\n" +
      "To wield old partisans, in hands as old,\n" +
      "Canker'd with peace, to part your canker'd hate:\n" +
      "If ever you disturb our streets again,\n" +
      "Your lives shall pay the forfeit of the peace.\n" +
      "For this time, all the rest depart away:\n" +
      "You Capulet; shall go along with me:\n" +
      "And, Montague, come you this afternoon,\n" +
      "To know our further pleasure in this case,\n" +
      "To old Free-town, our common judgment-place.\n" +
      "Once more, on pain of death, all men depart.";

  private static final String MD5_EXPECTED =
      "a831e91e0f70eddcb70dc61c6f82f6cd";

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

  /* Bitwise rotate a 32-bit number to the left. */
  private int bitRotateLeft(int num, int cnt) {
    return (num << cnt) | (num >>> (32 - cnt));
  }

  /*
   * Convert a string to an array of little-endian words
   * If CHRSZ is ASCII, characters >255 have their hi-byte silently ignored.
   */
  private int[] str2binl(String str, int padding) {
    final int len = str.length() / 4;
    int paddedLen = len;
    final int rem = paddedLen % padding;

    if (padding != 0 && rem != 0) {
      paddedLen += padding - rem;
    }

    int[] bin = new int[paddedLen];

    for (int i = 0; i < str.length() * CHRSZ; i += CHRSZ) {
      bin[i >> 5] |= (str.charAt(i / CHRSZ) & MASK) << (i % 32);
    }

    return bin;
  }

  /* Default padding of 16 as in the original Javascript implementation. */
  private int[] str2binl(String str) {
    return str2binl(str, 16);
  }

  /* Convert an array of little-endian words to a hex string. */
  private String binl2hex(int[] bin) {
    String str = "";

    for (int i = 0; i < bin.length * 4; i++) {
      str += HEX_CHARS.charAt((bin[i >> 2] >> ((i % 4) * 8 + 4)) & 0xF);
      str += HEX_CHARS.charAt((bin[i >> 2] >> ((i % 4) * 8)) & 0xF);
    }

    return str;
  }

  private int md5Common(int q, int a, int b, int x, int s, int t) {
    return bitRotateLeft(a + q + x + t, s) + b;
  }

  private int md5FF(int a, int b, int c, int d, int x, int s, int t) {
    return md5Common((b & c) | ((~b) & d), a, b, x, s, t);
  }

  private int md5GG(int a, int b, int c, int d, int x, int s, int t) {
    return md5Common((b & d) | (c & (~d)), a, b, x, s, t);
  }

  private int md5HH(int a, int b, int c, int d, int x, int s, int t) {
    return md5Common(b ^ c ^ d, a, b, x, s, t);
  }

  private int md5II(int a, int b, int c, int d, int x, int s, int t) {
    return md5Common(c ^ (b | (~d)), a, b, x, s, t);
  }

  private int[] coreMD5(int[] x, int len) {
    /* append padding */
    x[len >> 5] |= 0x80 << ((len) % 32);
    x[(((len + 64) >>> 9) << 4) + 14] = len;

    int a =  1732584193;
    int b = -271733879;
    int c = -1732584194;
    int d =  271733878;

    for (int i = 0; i < x.length; i += 16) {
      final int olda = a;
      final int oldb = b;
      final int oldc = c;
      final int oldd = d;

      a = md5FF(a, b, c, d, x[i + 0], 7 , -680876936);
      d = md5FF(d, a, b, c, x[i + 1], 12, -389564586);
      c = md5FF(c, d, a, b, x[i + 2], 17,  606105819);
      b = md5FF(b, c, d, a, x[i + 3], 22, -1044525330);
      a = md5FF(a, b, c, d, x[i + 4], 7 , -176418897);
      d = md5FF(d, a, b, c, x[i + 5], 12,  1200080426);
      c = md5FF(c, d, a, b, x[i + 6], 17, -1473231341);
      b = md5FF(b, c, d, a, x[i + 7], 22, -45705983);
      a = md5FF(a, b, c, d, x[i + 8], 7 ,  1770035416);
      d = md5FF(d, a, b, c, x[i + 9], 12, -1958414417);
      c = md5FF(c, d, a, b, x[i + 10], 17, -42063);
      b = md5FF(b, c, d, a, x[i + 11], 22, -1990404162);
      a = md5FF(a, b, c, d, x[i + 12], 7 ,  1804603682);
      d = md5FF(d, a, b, c, x[i + 13], 12, -40341101);
      c = md5FF(c, d, a, b, x[i + 14], 17, -1502002290);
      b = md5FF(b, c, d, a, x[i + 15], 22,  1236535329);

      a = md5GG(a, b, c, d, x[i + 1], 5 , -165796510);
      d = md5GG(d, a, b, c, x[i + 6], 9 , -1069501632);
      c = md5GG(c, d, a, b, x[i + 11], 14,  643717713);
      b = md5GG(b, c, d, a, x[i + 0], 20, -373897302);
      a = md5GG(a, b, c, d, x[i + 5], 5 , -701558691);
      d = md5GG(d, a, b, c, x[i + 10], 9 ,  38016083);
      c = md5GG(c, d, a, b, x[i + 15], 14, -660478335);
      b = md5GG(b, c, d, a, x[i + 4], 20, -405537848);
      a = md5GG(a, b, c, d, x[i + 9], 5 ,  568446438);
      d = md5GG(d, a, b, c, x[i + 14], 9 , -1019803690);
      c = md5GG(c, d, a, b, x[i + 3], 14, -187363961);
      b = md5GG(b, c, d, a, x[i + 8], 20,  1163531501);
      a = md5GG(a, b, c, d, x[i + 13], 5 , -1444681467);
      d = md5GG(d, a, b, c, x[i + 2], 9 , -51403784);
      c = md5GG(c, d, a, b, x[i + 7], 14,  1735328473);
      b = md5GG(b, c, d, a, x[i + 12], 20, -1926607734);

      a = md5HH(a, b, c, d, x[i + 5], 4 , -378558);
      d = md5HH(d, a, b, c, x[i + 8], 11, -2022574463);
      c = md5HH(c, d, a, b, x[i + 11], 16,  1839030562);
      b = md5HH(b, c, d, a, x[i + 14], 23, -35309556);
      a = md5HH(a, b, c, d, x[i + 1], 4 , -1530992060);
      d = md5HH(d, a, b, c, x[i + 4], 11,  1272893353);
      c = md5HH(c, d, a, b, x[i + 7], 16, -155497632);
      b = md5HH(b, c, d, a, x[i + 10], 23, -1094730640);
      a = md5HH(a, b, c, d, x[i + 13], 4 ,  681279174);
      d = md5HH(d, a, b, c, x[i + 0], 11, -358537222);
      c = md5HH(c, d, a, b, x[i + 3], 16, -722521979);
      b = md5HH(b, c, d, a, x[i + 6], 23,  76029189);
      a = md5HH(a, b, c, d, x[i + 9], 4 , -640364487);
      d = md5HH(d, a, b, c, x[i + 12], 11, -421815835);
      c = md5HH(c, d, a, b, x[i + 15], 16,  530742520);
      b = md5HH(b, c, d, a, x[i + 2], 23, -995338651);

      a = md5II(a, b, c, d, x[i + 0], 6 , -198630844);
      d = md5II(d, a, b, c, x[i + 7], 10,  1126891415);
      c = md5II(c, d, a, b, x[i + 14], 15, -1416354905);
      b = md5II(b, c, d, a, x[i + 5], 21, -57434055);
      a = md5II(a, b, c, d, x[i + 12], 6 ,  1700485571);
      d = md5II(d, a, b, c, x[i + 3], 10, -1894986606);
      c = md5II(c, d, a, b, x[i + 10], 15, -1051523);
      b = md5II(b, c, d, a, x[i + 1], 21, -2054922799);
      a = md5II(a, b, c, d, x[i + 8], 6 ,  1873313359);
      d = md5II(d, a, b, c, x[i + 15], 10, -30611744);
      c = md5II(c, d, a, b, x[i + 6], 15, -1560198380);
      b = md5II(b, c, d, a, x[i + 13], 21,  1309151649);
      a = md5II(a, b, c, d, x[i + 4], 6 , -145523070);
      d = md5II(d, a, b, c, x[i + 11], 10, -1120210379);
      c = md5II(c, d, a, b, x[i + 2], 15,  718787259);
      b = md5II(b, c, d, a, x[i + 9], 21, -343485551);

      a = a + olda;
      b = b + oldb;
      c = c + oldc;
      d = d + oldd;
    }
    return new int[] {a, b, c, d};
  }

  public String hexMD5(String stringData) {
    int[] bin = str2binl(stringData);
    int[] md5 = coreMD5(bin, stringData.length() * CHRSZ);
    String hex = binl2hex(md5);
    return hex;
  }

  public void timeHexMD5(int iters) {
    for (int i = 0; i < iters; i++) {
      hexMD5(TEXT);
    }
  }

  public boolean verify() {
    String md5Output = hexMD5(TEXT);
    return md5Output.equals(MD5_EXPECTED);
  }

  public static void main(String[] argv) {
    CryptoMD5 obj = new CryptoMD5();
    final long before = System.currentTimeMillis();
    obj.timeHexMD5(2305);
    final long after = System.currentTimeMillis();
    obj.verify();
    System.out.println("CryptoMD5: " + (after - before));
  }
}
