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

// This benchmark performs bubble sort in the worst case scenario.
public class BubbleSort {
  public int[] inputArr;
  public int ARRAY_COUNT = 512;
  public void timeSort(int iterations) {
    this.inputArr = new int[ARRAY_COUNT];
    for(int iter = 0; iter < iterations; ++iter) {
      // Initialize the array.
      for(int i = 0; i < this.inputArr.length; ++i) {
          this.inputArr[i] = i;
      }
      // Perform sort.
      for(int i = 0; i < this.inputArr.length; ++i) {
          for(int j = 0; j < this.inputArr.length - 1; ++j) {
              if (this.inputArr[j] < this.inputArr[j + 1]) {
                 int temp = this.inputArr[j];
                 this.inputArr[j] = this.inputArr[j + 1];
                 this.inputArr[j + 1] = temp;
              }
          }
       }
    }
  }

  public boolean verify() {
    // Verify sorted output.
    for(int i = 0; i < this.inputArr.length; ++i) {
        int expected = this.inputArr.length - i - 1;
        int actual = this.inputArr[i];
        if(expected != actual) {
          System.out.println("ERROR: Mismatch at position " + i +
                             " Expected " + expected +
                             " Actual " + actual);
          return false;
        }
    }
    return true;
  }

  public static void main(String[] args) {
    BubbleSort obj = new BubbleSort();
    long before = System.currentTimeMillis();
    obj.timeSort(1);
    long after = System.currentTimeMillis();
    obj.verify();
    System.out.println("BubbleSort(): " + (after - before));
  }
}

