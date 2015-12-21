/*
 * Copyright (C) 2015 Linaro Limited. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/* encode and decode methods from:
 *  https://en.wikipedia.org/wiki/Base64#Sample_Implementation_in_Java
 *
 * Available under the Creative Commons Attribution-ShareAlike License.
 *  https://en.wikipedia.org/wiki/Wikipedia: \
 *    Text_of_Creative_Commons_Attribution-ShareAlike_3.0_Unported_License.
 *
 */

/*
 * Description:     Uses a Base64 MIME implementation to check for regressions
                    in loops, array access, load/store, and string manipulation.
 * Main Focus:      General operations related to Base64 encoding/decoding.
 * Secondary Focus: Array access, load/store, string manipulation.
 *
 */

package benchmarks.micro;

import java.lang.IllegalArgumentException;
import java.lang.StringBuilder;
import java.lang.System;
import java.lang.Thread;
import java.util.Random;

public class Base64 {
  private static Random rnd = new Random();
  private static final int ENC_Length = 64;
  private static final int NUM_Encodings = 16;
  private static String[] randomStrings = new String[NUM_Encodings];
  private static String[] randomBase64 = new String[NUM_Encodings];
  private static String[] encodeResults = new String[NUM_Encodings];
  private static String[] decodeResults = new String[NUM_Encodings];
  private static final String codes =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
  private static char base64Pad = '=';

  static {
    generateRandomStrings();
    generateRandomBase64();
  }

  private static void generateRandomStrings() {
    for (int i = 0; i < NUM_Encodings; i++) {
      StringBuilder sb = new StringBuilder();
      for (int j = 0; j < ENC_Length; j++) {
        sb.append(Character.valueOf((char)rnd.nextInt()));
      }
      randomStrings[i] = sb.toString();
    }
  }

  private static void generateRandomBase64() {
    for (int i = 0; i < NUM_Encodings; i++) {
      StringBuilder sb = new StringBuilder();
      for (int j = 0; j < ENC_Length; j++) {
        sb.append(codes.charAt(rnd.nextInt(codes.length() - 1)));
      }
      randomBase64[i] = sb.toString();
    }
  }

  private static String encode(String str) {
    byte[] in = str.getBytes();
    StringBuffer out = new StringBuffer((in.length * 4) / 3);
    int b;
    for (int i = 0; i < in.length; i += 3)  {
      b = (in[i] & 0xFC) >> 2;
      out.append(codes.charAt(b));
      b = (in[i] & 0x03) << 4;
      if (i + 1 < in.length)      {
        b |= (in[i + 1] & 0xF0) >> 4;
        out.append(codes.charAt(b));
        b = (in[i + 1] & 0x0F) << 2;
        if (i + 2 < in.length)  {
          b |= (in[i + 2] & 0xC0) >> 6;
          out.append(codes.charAt(b));
          b = in[i + 2] & 0x3F;
          out.append(codes.charAt(b));
        } else {
          out.append(codes.charAt(b));
          out.append('=');
        }
      } else {
        out.append(codes.charAt(b));
        out.append("==");
      }
    }

    return out.toString();
  }

  private static byte[] decode(String input)    {
    if (input.length() % 4 != 0)    {
      System.err.println("Invalid base64 input");
      return null;
    }
    int eqPos = input.indexOf('=');
    int len = input.length();
    byte[] decoded =
      new byte[((len * 3) / 4) - (eqPos > 0 ? (len - eqPos) : 0)];
    char[] inChars = input.toCharArray();
    int j = 0;
    int[] b = new int[4];
    for (int i = 0; i < inChars.length; i += 4)     {
      b[0] = codes.indexOf(inChars[i]);
      b[1] = codes.indexOf(inChars[i + 1]);
      b[2] = codes.indexOf(inChars[i + 2]);
      b[3] = codes.indexOf(inChars[i + 3]);
      decoded[j++] = (byte) ((b[0] << 2) | (b[1] >> 4));
      if (b[2] < 64)      {
        decoded[j++] = (byte) ((b[1] << 4) | (b[2] >> 2));
        if (b[3] < 64)  {
          decoded[j++] = (byte) ((b[2] << 6) | b[3]);
        }
      }
    }
    return decoded;
  }

  public void timeEncode(int iterations) {
    for (int i = 0; i < iterations; i++) {
      for (int j = 0; j < NUM_Encodings; j++) {
        encodeResults[j] = encode(randomStrings[j]);
      }
    }
  }

  public void timeDecode(int iterations) {
    for (int i = 0; i < iterations; i++) {
      for (int j = 0; j < NUM_Encodings; j++) {
        decodeResults[j] = new String(decode(randomBase64[j]));
      }
    }
  }

  public boolean verifyEncode() {
    boolean result = true;
    result &= encode("Don't panic.").equals(
        "RG9uJ3QgcGFuaWMu");
    result &= encode("Time is an illusion.").equals(
        "VGltZSBpcyBhbiBpbGx1c2lvbi4=");
    result &= encode("Don't talk to me about life.").equals(
        "RG9uJ3QgdGFsayB0byBtZSBhYm91dCBsaWZlLg==");
    result &= encode("Ford... you're turning into a penguin.").equals(
        "Rm9yZC4uLiB5b3UncmUgdHVybmluZyBpbnRvIGEgcGVuZ3Vpbi4=");
    return result;
  }

  public boolean verifyDecode() {
    boolean result = true;
    result &= new String(decode("RG9uJ3QgcGFuaWMu")).equals(
        "Don't panic.");
    result &= new String(decode("VGltZSBpcyBhbiBpbGx1c2lvbi4=")).equals(
        "Time is an illusion.");
    result &= new String(decode("RG9uJ3QgdGFsayB0byBtZSBhYm91dCBsaWZlLg==")).equals(
        "Don't talk to me about life.");
    result &= new String(decode("Rm9yZC4uLiB5b3UncmUgdHVybmluZyBpbnRvIGEgcGVuZ3Vpbi4=")).equals(
        "Ford... you're turning into a penguin.");
    return result;
  }

  public static void main(String[] args) {
    Base64 b = new Base64();
    long before = System.currentTimeMillis();
    b.timeEncode(1000);
    b.timeDecode(1000);
    long after = System.currentTimeMillis();
    System.out.println("Base64: " + (after - before));
  }
}
