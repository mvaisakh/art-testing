/*
 * Copyright (C) 2016 Linaro Limited. All rights reserved.
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

/*
 * Description:     Tracks performance of System.arraycopy intrinsics.
 * Main Focus:      Looped load store for varying copy lengths.
 * Secondary Focus:
 *
 */

package benchmarks.micro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.StringBuilder;
import java.lang.System;
import java.util.Random;

public class SystemArrayCopy {

  private static Random rnd = new Random();
  private static int ARRAY_COPY_SMALL = 16;
  private static int ARRAY_COPY_MEDIUM = 128;
  private static int ARRAY_COPY_LARGE = 1024;
  private static int MAX_BUFFER_BYTES = 8192;
  private static int ARRAY_LENGTH = 1024;
  private static String RANDOM_STRING = generateRandomString(MAX_BUFFER_BYTES);
  private static char[] cbuf = new char[MAX_BUFFER_BYTES];
  private static char arrayCopyCharBufferedReadSmallResult;
  private static char arrayCopyCharBufferedReadMediumResult;
  private static char arrayCopyCharBufferedReadLargeResult;

  private static String[] stringArray = new String[ARRAY_LENGTH];
  private static String[] stringArraySmall = new String[ARRAY_LENGTH];
  private static String[] stringArrayMedium = new String[ARRAY_LENGTH];
  private static String[] stringArrayLarge = new String[ARRAY_LENGTH];

  private static String generateRandomString(int sz) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < sz; i++) {
      sb.append(Character.valueOf((char)rnd.nextInt()));
    }
    return sb.toString();
  }

  static {
    for (int i = 0; i < ARRAY_LENGTH; i++) {
      stringArray[i] = String.valueOf(i);
    }
  }

  private void bufferedReadLoop(char[] cbuf, int copyLength) throws IOException {
    BufferedReader reader = new BufferedReader(new StringReader(RANDOM_STRING));
    int offset = 0;
    String s;
    /* Read 16Kb RANDOM_STRING in chunks of copyLength chars until EOF */
    while (offset < MAX_BUFFER_BYTES && (reader.read(cbuf, offset, copyLength)) != -1) {
      offset += copyLength;
    }
  }

  public boolean verify() throws IOException {
    boolean result = true;

    timeArrayCopySmall(1);
    for (int i = 0; i < ARRAY_COPY_SMALL; i++) {
      result &= stringArray[i].equals(stringArraySmall[i]);
    }

    timeArrayCopyMedium(1);
    for (int i = 0; i < ARRAY_COPY_MEDIUM; i++) {
      result &= stringArray[i].equals(stringArrayMedium[i]);
    }

    timeArrayCopyLarge(1);
    for (int i = 0; i < ARRAY_COPY_LARGE; i++) {
      result &= stringArray[i].equals(stringArrayLarge[i]);
    }

    result &= arrayCopyCharBufferedReadSmallResult == RANDOM_STRING.charAt(MAX_BUFFER_BYTES - 1);
    result &= arrayCopyCharBufferedReadMediumResult == RANDOM_STRING.charAt(MAX_BUFFER_BYTES - 1);
    result &= arrayCopyCharBufferedReadLargeResult == RANDOM_STRING.charAt(MAX_BUFFER_BYTES - 1);
    return result;
  }

  public void timeArrayCopyCharBufferedReadSmall(int iterations) throws IOException {
    for (int i = 0; i < iterations; i++) {
      bufferedReadLoop(cbuf, ARRAY_COPY_SMALL);
      arrayCopyCharBufferedReadSmallResult = cbuf[MAX_BUFFER_BYTES - 1];
    }
  }

  public void timeArrayCopyCharBufferedReadMedium(int iterations) throws IOException {
    for (int i = 0; i < iterations; i++) {
      bufferedReadLoop(cbuf, ARRAY_COPY_MEDIUM);
      arrayCopyCharBufferedReadMediumResult = cbuf[MAX_BUFFER_BYTES - 1];
    }
  }

  public void timeArrayCopyCharBufferedReadLarge(int iterations) throws IOException {
    for (int i = 0; i < iterations; i++) {
      bufferedReadLoop(cbuf, ARRAY_COPY_LARGE);
      arrayCopyCharBufferedReadLargeResult = cbuf[MAX_BUFFER_BYTES - 1];
    }
  }

  public void timeArrayCopySmall(int iterations) {
    for (int i = 0; i < iterations; i++) {
      System.arraycopy(stringArray, 0, stringArraySmall, 0, ARRAY_COPY_SMALL);
    }
  }

  public void timeArrayCopyMedium(int iterations) {
    for (int i = 0; i < iterations; i++) {
      System.arraycopy(stringArray, 0, stringArrayMedium, 0, ARRAY_COPY_MEDIUM);
    }
  }

  public void timeArrayCopyLarge(int iterations) {
    for (int i = 0; i < iterations; i++) {
      System.arraycopy(stringArray, 0, stringArrayLarge, 0, ARRAY_COPY_LARGE);
    }
  }

  private static final int ITER_COUNT = 4000;

  public static void main(String[] args) {
    int result = 0;
    SystemArrayCopy obj = new SystemArrayCopy();
    final long before = System.currentTimeMillis();
    try {
      obj.timeArrayCopySmall(ITER_COUNT);
      obj.timeArrayCopyMedium(ITER_COUNT);
      obj.timeArrayCopyLarge(ITER_COUNT);

      obj.timeArrayCopyCharBufferedReadSmall(ITER_COUNT);
      obj.timeArrayCopyCharBufferedReadMedium(ITER_COUNT);
      obj.timeArrayCopyCharBufferedReadLarge(ITER_COUNT);
      if (!obj.verify()) {
        result++;
        System.out.println("ERROR: verify() failed.");
      }
    } catch (IOException ex) {
      System.out.println("ERROR: benchmarks/micro/SystemArrayCopy: " + ex.getMessage());
    }
    final long after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/SystemArrayCopy: " + (after - before));
    System.exit(result);
  }
}
