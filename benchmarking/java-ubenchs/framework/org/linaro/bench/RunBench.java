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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RunBench {
    // Minimum valid calibration time: 400ms.
    public final static long DEFAULT_CALIBRATION_MIN_TIME_NS = TimeUnit.NANOSECONDS.convert(400, TimeUnit.MILLISECONDS);
    // The target benchmark running time: 2s.
    public final static long DEFAULT_BENCHMARK_TARGET_RUN_TIME_NS = TimeUnit.NANOSECONDS.convert(2, TimeUnit.SECONDS);
    public final static int ITERATIONS_LIMIT = 0x400000;

    // A method with this name will be executed as a microbenchmark.
    public static final String TESTNAME_PREFIX = "time";

    private SimpleLogger log;
    private long calibrationMinTimeNS;
    private long benchmarkTargetRunTimeNS;

    public RunBench() {
        this.log = SimpleLogger.getInstance();
        calibrationMinTimeNS = DEFAULT_CALIBRATION_MIN_TIME_NS;
        benchmarkTargetRunTimeNS = DEFAULT_BENCHMARK_TARGET_RUN_TIME_NS;
    }

    public void setLogLevel(SimpleLogger.LogLevel level) {
        this.log = SimpleLogger.getInstance();
        log.setLogLevel(level);
    }

    public IterationsAnnotation getTestProperties(Method method) {
        IterationsAnnotation it = method.getAnnotation(IterationsAnnotation.class);
        return it;
    }

    /*
     * Returns duration of given iterations in nano seconds.
     */
    public static long timeIterations(Object object, Method method, int iters) {
        long start = 0;
        long end = 0;
        try {
            start = System.nanoTime();
            method.invoke(object, iters);
            end = System.nanoTime();
        } catch (Exception e) {
            return -1;
        }
        return end - start;
    }

    static String benchmarkIdentifier(Method method) {
        Pattern format = Pattern.compile("((?:\\w+\\.)*)(\\w+)");
        Matcher matcher = format.matcher(method.getDeclaringClass().getName());
        if (! matcher.matches()) {
            return null;
        }
        String path = matcher.group(1);
        path = path.replace('.', '/');
        String className = matcher.group(2);
        // Filter the "time" prefix.
        String benchName = method.getName().substring(4);
        return path + className + "." + benchName;
    }

    /*
     * Run one benchmark. May have auto-calibration depends on method's IterationsAnnotation.
     */
    public void runOneBench(Object instance, Method method) throws Exception {
        log.debug("Running method: " + method.toString());

        IterationsAnnotation anno = getTestProperties(method);
        long iterations;
        long duration = -1;
        double time;
        double iteration_time;

        if (anno != null && anno.iterations() > 0) {
          iterations = anno.iterations();
          duration = timeIterations(instance, method, (int) iterations);
        } else {
          // Estimate how long it takes to run one iteration.
          iterations = 1;
          while ((duration < calibrationMinTimeNS) && (iterations < ITERATIONS_LIMIT)) {
            iterations *= 2;
            duration = timeIterations(instance, method, (int) iterations);
          }
          // Estimate the number of iterations to run based on the calibration
          // phase, and benchmark the function.
          double iter_time = duration / (double) iterations;
          iterations = (int) Math.max(1.0, benchmarkTargetRunTimeNS / iter_time);
          duration = timeIterations(instance, method, (int) iterations);
        }

        iteration_time = duration / (float) iterations;

        log.info(benchmarkIdentifier(method) + ": " +
                duration + " ns for " + iterations + " iterations");
        System.out.printf("%-40s%.2f ns per iteration\n",
                benchmarkIdentifier(method) + ":", iteration_time);
    }

    public int runBenchSet(String target, boolean verify) {
        if (target == null) {
            return 1;
        }

        // The target format is:
        //    path/to/BenchmarkClass(.Benchmark)?
        Pattern format = Pattern.compile("((?:\\w+\\/)*)(\\w+)(?:\\.(\\w+))?$");
        Matcher matcher = format.matcher(target);
        if (! matcher.matches()) {
          return 1;
        }
        String benchmarkClassPath = matcher.group(1);
        if (!benchmarkClassPath.startsWith("benchmarks/")) {
            benchmarkClassPath = "benchmarks/" + benchmarkClassPath;
        }
        benchmarkClassPath = benchmarkClassPath.replace('/', '.');
        String benchmarkClass = matcher.group(2);
        String benchmark = matcher.group(3);

        List<Method> benchMethods = new ArrayList<Method>(5);
        Method verifyMethod = null;
        try {
            Class<?> clazz = Class.forName(benchmarkClassPath + benchmarkClass);
            Object instance = clazz.newInstance();
            if (benchmark != null) {
                Method m = clazz.getMethod(TESTNAME_PREFIX + benchmark, int.class);
                benchMethods.add(m);
            } else {
                for (Method m : clazz.getDeclaredMethods()) {
                    if (m.getName().startsWith(TESTNAME_PREFIX)) {
                        benchMethods.add(m);
                    } else if (m.getName().equals("verify") && m.getReturnType() == boolean.class) {
                        verifyMethod = m;
                    }
                }
            }
            // Sort benchMethods by name.
            Collections.sort(benchMethods, new Comparator<Method>() {
                @Override
                public int compare(Method m1, Method m2) {
                    return m1.getName().compareTo(m2.getName());
                }
            });

            for (Method m : benchMethods) {
                // Run each method as a benchmark.
                runOneBench(instance, m);
            }

            // Optionally verify benchmark results.
            if (verify && verifyMethod != null) {
                if (!(Boolean)verifyMethod.invoke(instance)) {
                    log.error(clazz.getName() + " failed verification.");
                    return 1;
                }
            }
        } catch (Exception e) {
            // TODO: filter exceptions.
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    public static final String helpMessage =
        "Usage: java org.linaro.bench.RunBench [OPTIONS] [Benchmark...]\n" +
        "OPTIONS:\n" +
        "\t--help                              Print this error message.\n" +
        "\t--verbose                           Be verbose.\n" +
        "\t--debug                             Be more verbose than the verbose mode.\n" +
        "\t--list_benchmarks                   List available benchmarks and exit.\n" +
        /* TODO: Add a `--list_sub_benchmarks` option. */
        "\t--benchmark_run_time <time in s>    Set the target running time for benchmarks.\n" +
        "\t                                    (default: " + DEFAULT_BENCHMARK_TARGET_RUN_TIME_NS + ")\n" +
        "\t--calibration_min_time <time in s>  Set the minimum running time for benchmark calibration.\n" +
        "\t                                    (default: " + DEFAULT_CALIBRATION_MIN_TIME_NS + ")\n" +
        "";
    public void parseCmdlineAndRun(String[] args) {
        String subtest = null;
        boolean verify = true;  // Verify all benchmark results by default.
        List<String> benchmarks = new ArrayList<String>();

        for (int arg_index = 0; arg_index < args.length; arg_index++) {
            if (args[arg_index].startsWith("--")) {
                String option = args[arg_index].substring(2);
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
                    arg_index++;
                    if (arg_index < args.length) {
                        this.benchmarkTargetRunTimeNS = TimeUnit.NANOSECONDS.convert(Long.valueOf(args[arg_index]), TimeUnit.MILLISECONDS);
                    } else {
                        log.fatal("Require time.");
                    }
                } else if (option.equals("calibration_min_time")) {
                    arg_index++;
                    if (arg_index < args.length) {
                        this.calibrationMinTimeNS = TimeUnit.NANOSECONDS.convert(Long.valueOf(args[arg_index]), TimeUnit.MILLISECONDS);
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
                benchmarks.add(args[arg_index]);
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
            }
        }
    }

    public static void main(String[] args) {
        RunBench bench = new RunBench();
        // Set default log level.
        bench.parseCmdlineAndRun(args);
    }
}