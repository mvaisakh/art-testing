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

package org.linaro.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RunBench {
  // Minimum valid calibration time.
  public static final long DEFAULT_CALIBRATION_MIN_TIME_NS =
      TimeUnit.NANOSECONDS.convert(50, TimeUnit.MILLISECONDS);
  // The target benchmark running time.
  public static final long DEFAULT_BENCH_TARGET_RUN_TIME_NS =
      TimeUnit.NANOSECONDS.convert(400, TimeUnit.MILLISECONDS);

  private SimpleLogger log;
  private long calibrationMinTimeNs;
  private long benchmarkTargetRunTimeNs;

  public RunBench() {
    this.log = SimpleLogger.getInstance();
    calibrationMinTimeNs = DEFAULT_CALIBRATION_MIN_TIME_NS;
    benchmarkTargetRunTimeNs = DEFAULT_BENCH_TARGET_RUN_TIME_NS;
  }

  public void setLogLevel(SimpleLogger.LogLevel level) {
    this.log = SimpleLogger.getInstance();
    log.setLogLevel(level);
  }

  public int runBenchSet(String target, boolean verify) {
    if (target == null) {
      return 1;
    }

    try {
      Benchmark benchmark = new Benchmark(target, calibrationMinTimeNs,
          benchmarkTargetRunTimeNs);
      Benchmark.Result[] results = benchmark.run();
      int verifyFailures = 0;
      if (verify) {
        verifyFailures = benchmark.verify();
      }
      for (Benchmark.Result result : results) {
        System.out.println(result.toString());
      }
      if (verifyFailures > 0) {
        return 1;
      }
    } catch (Exception e) {
      // TODO: filter exceptions.
      e.printStackTrace();
      return 1;
    }
    return 0;
  }

  public static final String helpMessage =
      "Usage: java org.linaro.bench.RunBench [OPTIONS] [Benchmark...]\n"
      + "OPTIONS:\n"
      + "\t--help               Print this error message.\n"
      + "\t--verbose            Be verbose.\n"
      + "\t--debug              Be more verbose than the verbose mode.\n"
      + "\t--list_benchmarks    List available benchmarks and exit.\n"
      /* TODO: Add a `--list_sub_benchmarks` option. */
      + "\t--benchmark_run_time <time in ms>\n"
      + "\t                     Set the target running time for benchmarks.\n"
      + "\t                     (default: "
      + TimeUnit.MILLISECONDS.convert(DEFAULT_BENCH_TARGET_RUN_TIME_NS, TimeUnit.NANOSECONDS)
      + ")\n"
      + "\t--calibration_min_time <time in ms>\n"
      + "\t                     Set the minimum running time for benchmark calibration.\n"
      + "\t                     (default: "
      + TimeUnit.MILLISECONDS.convert(DEFAULT_CALIBRATION_MIN_TIME_NS, TimeUnit.NANOSECONDS)
      + ")\n";

  public int parseCmdlineAndRun(String[] args) {
    int errors = 0;
    String subtest = null;
    boolean verify = true;  // Verify all benchmark results by default.
    List<String> benchmarks = new ArrayList<String>();

    for (int argIndex = 0; argIndex < args.length; argIndex++) {
      if (args[argIndex].startsWith("--")) {
        String option = args[argIndex].substring(2);
        if (option.equals("help")) {
          System.out.println(helpMessage);
          System.exit(0);
        } else if (option.equals("verbose")) {
          setLogLevel(SimpleLogger.LogLevel.INFO);
        } else if (option.equals("debug")) {
          setLogLevel(SimpleLogger.LogLevel.DEBUG);
        } else if (option.equals("list_benchmarks")) {
          for (int i = 0; i < BenchmarkList.benchmarkList.length; i++) {
            System.out.println(BenchmarkList.benchmarkList[i]);
          }
          System.exit(0);
        } else if (option.equals("benchmark_run_time")) {
          argIndex++;
          if (argIndex < args.length) {
            this.benchmarkTargetRunTimeNs =
              TimeUnit.NANOSECONDS.convert(Long.valueOf(args[argIndex]), TimeUnit.MILLISECONDS);
          } else {
            log.fatal("Require time.");
          }
        } else if (option.equals("calibration_min_time")) {
          argIndex++;
          if (argIndex < args.length) {
            this.calibrationMinTimeNs =
              TimeUnit.NANOSECONDS.convert(Long.valueOf(args[argIndex]), TimeUnit.MILLISECONDS);
          } else {
            log.fatal("Require time.");
          }
        } else if (option.equals("noverify")) {
          verify = false;
        } else {
          log.error("Unknown option `--" + option + "`.");
          System.out.println(helpMessage);
          System.exit(1);
        }
      } else {
        benchmarks.add(args[argIndex]);
      }
    }

    if (benchmarks.size() == 0) {
      // No benchmarks were specified on the command line. Run all
      // benchmarks available.
      for (int i = 0; i < BenchmarkList.benchmarkList.length; i++) {
        benchmarks.add(BenchmarkList.benchmarkList[i]);
      }
    }
    // Run the benchmarks.
    for (int i = 0; i < benchmarks.size(); i++) {
      if (runBenchSet(benchmarks.get(i), verify) != 0) {
        log.error("Test failed.");
        errors++;
      }
    }

    return errors;
  }

  public static void main(String[] args) {
    RunBench bench = new RunBench();
    // Set default log level.
    int errors = bench.parseCmdlineAndRun(args);
    System.exit(errors);
  }
}
