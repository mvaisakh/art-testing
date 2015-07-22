/*
 *    Copyright 2015 ARM Limited
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

import java.lang.System;

// This benchmark performs various string operations.
public class StringOps {
  public int INNER_LOOP_COUNT = 512;
  public String string1;
  public String string2;
  public String string3;
  public String string4;
  public String string5;

  public StringOps() {
    this.string1 = "first ";
    this.string2 = "second ";
    this.string3 = "third ";
    this.string4 = "fourth ";
    this.string5 = "fifth ";
  }

  public int timeAppend(int iterations) {
    StringBuffer str = new StringBuffer(" ");
    for(int i = 0; i < iterations; ++i) {
      for(int j = 0; j < this.INNER_LOOP_COUNT; ++j) {
        str.append(this.string1);
        str.append(this.string2);
        str.append(this.string3);
        str.append(this.string4);
        str.append(this.string5);
      }
    }
    return str.length();
  }

  public int timeAppendAndSearch(int iterations) {
    StringBuffer str = new StringBuffer(" ");
    int index = 0;
    for(int i = 0; i < iterations; ++i) {
      for(int j = 0; j < this.INNER_LOOP_COUNT; ++j) {
        str.append(this.string1);
      }
      str.append(this.string2);
      for(int j = 0; j < this.INNER_LOOP_COUNT; ++j) {
        index = str.toString().indexOf(this.string2, j * this.string1.length());
      }
    }
    return index;
  }

  public static void main(String[] args) {
    StringOps obj = new StringOps();
    long before = System.currentTimeMillis();
    obj.timeAppend(1);
    long after = System.currentTimeMillis();
    System.out.println("String append : " + (after - before));

    before = System.currentTimeMillis();
    obj.timeAppendAndSearch(1);
    after = System.currentTimeMillis();
    System.out.println("String append and search: " + (after - before));
  }
}

