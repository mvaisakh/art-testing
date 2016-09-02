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
 * Description:     Simple loops around CRC32 intrinsics.
 * Main Focus:      CRC32-related intrinsics.
 */

package benchmarks.micro.intrinsics;

import java.util.Random;
import java.util.zip.CRC32;

public class CRC32Bench {
  private byte []bytes = new byte[8192];
  private CRC32 crc32 = new CRC32();
  private static final int iterCount = 100;
  private static final int loopSize = 3000;

  public CRC32Bench() {
    Random rnd = new Random(0);
    rnd.nextBytes(bytes);
  }

  public void timeUpdateInt(int iterations) {
    for (int i = 0; i < iterations; i++) {
      benchUpdateInt();
    }
  }

  public void timeUpdateBytes(int iterations) {
    for (int i = 0; i < iterations; i++) {
      benchUpdateBytes();
    }
  }

  public void benchUpdateInt() {
    int count = loopSize * 230;
    crc32.reset();
    for (int i = 0; i < count; i++) {
      crc32.update(i);
    }
  }

  public void benchUpdateBytes() {
    crc32.reset();

    for (int i = 0; i < loopSize; i++) {
      crc32.update(bytes);
    }
  }

  public boolean verifyCRC32Bench() {
    byte []tmp = new String("Linaro ART Team 2016.").getBytes();

    crc32.reset();
    crc32.update(2016);
    if (crc32.getValue() != 0x72080DF5L) {
      return false;
    }

    crc32.reset();
    crc32.update(tmp, 0, tmp.length - 1);
    if (crc32.getValue() != 0x7A7583F7L) {
      return false;
    }

    crc32.reset();
    crc32.update(tmp);
    if (crc32.getValue() != 0x2D77F07EL) {
      return false;
    }

    return true;
  }

  public static void main(String[] args) {
    int rc = 0;
    CRC32Bench obj = new CRC32Bench();

    long before = System.currentTimeMillis();
    obj.timeUpdateInt(iterCount);
    long after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/intrinsics/CRC32Bench/UpdateInt: " + (after - before));

    before = System.currentTimeMillis();
    obj.timeUpdateBytes(iterCount);
    after = System.currentTimeMillis();
    System.out.println("benchmarks/micro/intrinsics/CRC32Bench/UpdateBytes: " + (after - before));

    if (!obj.verifyCRC32Bench()) {
      rc++;
    }

    System.exit(rc);
  }
}
