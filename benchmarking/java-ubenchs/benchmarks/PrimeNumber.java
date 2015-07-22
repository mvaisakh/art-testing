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

// This benchmark generates primes up to a number.
public class PrimeNumber {
  public int[] primes;
  public int MAX_PRIME = 512;
  public int numPrimes;

  public int timeGeneratePrime(int iteration)
  {
    this.primes = new int[this.MAX_PRIME];
    this.numPrimes = 0;

    for (int i = 0; i < iteration; ++i) {
      this.primes[0] = 1;
      this.primes[1] = 2;
      this.numPrimes = 2;
      for (int num = 3; num < this.MAX_PRIME; num++)
      {
        boolean isPrime = true;
        for(int m = 1; m < this.numPrimes && this.primes[m] <= num / 2; m++)
        {
          if (num % this.primes[m] == 0) {
            isPrime = false;
            break;
          }
        }
        if (isPrime)
        {
          this.primes[this.numPrimes] = num;
          this.numPrimes++;
        }
      }
    }
    return this.numPrimes;
  }

  public static void main(String[] args) {
    PrimeNumber obj = new PrimeNumber();
    long before = System.currentTimeMillis();
    obj.timeGeneratePrime(1);
    long after = System.currentTimeMillis();
    System.out.println("Generate prime numbers : " + (after - before));
  }
}

