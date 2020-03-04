/*
 *    Copyright 2020 Linaro Limited
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

package org.linaro.bench;

public class CalibrateBench {
  private static final String HELP_MESSAGE =
      "Usage: java org.linaro.bench.CalibrateBench --target_running_time <time in ms> Benchmark\n"
      + "Identifies a number of iterations needed for the specified benchmark methods to have\n"
      + "the target running time.\n"
      + "The result is output in the format:\n"
      + "<benchmark_class_name>[:<benchmark_method>:<iterations>]+\n"
      + "Examples of results:\n"
      + "benchmarks.caffeinemark.FloatAtom:timeFloatAtom@100\n"
      + "benchmarks.micro.ArrayAccess:timeAccessArrayConstants:10000:timeAccessArrayVariables:500\n"
      + "OPTIONS:\n"
      + "\t--target_running_time  <time in ms>\n"
      + "\t                       The target running time for benchmark methods.\n";

  private String benchmarkName;

  private void run() {
    System.out.println(Benchmark.fromString(benchmarkName).toString());
  }

  private void parseArgs(String[] args) {
    if (args.length != 3 ||
        !args[0].equals("--target_running_time")) {
      System.out.println(HELP_MESSAGE);
      System.exit(1);
    }

    Benchmark.setTargetRunningTime(Long.valueOf(args[1]));
    this.benchmarkName = args[2];
    if (this.benchmarkName.isEmpty()) {
      System.out.println("ERROR: benchmark name is not provided.");
      System.exit(1);
    }
  }

  public static void main(String[] args) {
    CalibrateBench calibrateBench = new CalibrateBench();
    calibrateBench.parseArgs(args);
    calibrateBench.run();
  }
}
