/*
 *    Copyright 2015 Linaro Limited
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

/******* NOTICE *********

 Apache Harmony
 Copyright 2006, 2010 The Apache Software Foundation.

 This product includes software developed at
 The Apache Software Foundation (http://www.apache.org/).

 Portions of Apache Harmony were originally developed by
 Intel Corporation and are licensed to the Apache Software
 Foundation under the "Software Grant and Corporate Contribution
 License Agreement" and for which the following copyright notices
 apply
 (C) Copyright 2005 Intel Corporation
 (C) Copyright 2005-2006 Intel Corporation
 (C) Copyright 2006 Intel Corporation


 The following copyright notice(s) were affixed to portions of the code
 with which this file is now or was at one time distributed
 and are placed here unaltered.

 (C) Copyright 1997,2004 International Business Machines Corporation.
 All rights reserved.

 (C) Copyright IBM Corp. 2003.


 This software contains code derived from UNIX V7, Copyright(C)
 Caldera International Inc.

 ************************/

/*
 * A benchmark case for hash map, which is converted from:
 * http://browserbench.org/JetStream/sources/hash-map.js
 */

package benchmarks.micro;

import java.util.HashMap;
import java.util.Map;

public class HashMapBench {

  private static final int COUNT = 5000;
  private static final int resultExpect = 1050000;
  private static final long keySumExpect = 12497500;
  private static final int valueSumExpect = 210000;
  private int result = 0;
  private long keySum = 0;
  private int valueSum = 0;

  private Map<Integer, Integer> map = new HashMap<Integer, Integer>();

  public void timeTestHashMap(int iters) {
    for (int i = 0; i < iters; i++) {
      for (int j = 0; j < COUNT; j++) {
        map.put(j, 42);
      }

      result = 0;
      for (int k = 0; k < 5; k++) {
        for (int j = 0; j < COUNT; j++) {
          result += map.get(j);
        }
      }

      keySum = 0;
      valueSum = 0;
      for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
        keySum += entry.getKey();
        valueSum += entry.getValue();
      }
    }
  }

  /**
   * Verify
   **/

  /**
   * Called by the framework to assert the benchmarks have done the right thing.
   **/
  public boolean verify() {
    boolean verified = true;

    if (result != resultExpect) {
      System.out.println("ERROR: Expected result " + resultExpect + " but found " + result);
      verified = false;
    }

    if (keySum != keySumExpect) {
      System.out.println("ERROR: Expected keySum " + keySumExpect + " but found " + keySum);
      verified = false;
    }

    if (valueSum != valueSumExpect) {
      System.out.println("ERROR: Expected valueSum " + valueSumExpect + " but found " + valueSum);
      verified = false;
    }

    return verified;
  }

  /**
   * *NOT* called by the framework by default, provided for direct use only.
   **/
  public static void main(String args[]) {
    HashMapBench obj = new HashMapBench();
    long before = System.currentTimeMillis();
    obj.timeTestHashMap(100);

    long after = System.currentTimeMillis();
    System.out.println("HashMapBench: " + (after - before));
  }
}
