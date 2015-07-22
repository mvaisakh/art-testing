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

package com.arm.microbench;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RunBench {
    // Minimum valid calibration time: 300ms.
    public final static long DEFAULT_CALIBRATION_MIN_TIME = 30000000L;
    // The target benchmark running time: 2s.
    public final static long DEFAULT_BENCHMARK_TARGET_RUN_TIME = 2000000000L;
    public final static int ITERATIONS_LIMIT = 0x40000000;

    // A method with this name will be executed as a microbenchmark.
    public static final String TESTNAME_PREFIX = "time";

    private SimpleLogger log;
    private long calibrationMinTime;
    private long benchmarkTargetRunTime;

    public RunBench() {
        this.log = SimpleLogger.getInstance();
        calibrationMinTime = DEFAULT_CALIBRATION_MIN_TIME;
        benchmarkTargetRunTime = DEFAULT_BENCHMARK_TARGET_RUN_TIME;
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
          while ((duration < calibrationMinTime) && (iterations < ITERATIONS_LIMIT)) {
            iterations *= 2;
            duration = timeIterations(instance, method, (int) iterations);
          }
          // Estimate the number of iterations to run based on the calibration
          // phase, and benchmark the function.
          double iter_time = duration / (double) iterations;
          iterations = (int) Math.max(1.0, benchmarkTargetRunTime / iter_time);
          duration = timeIterations(instance, method, (int) iterations);
        }

        iteration_time = duration / (float) iterations;

        log.info(method.getDeclaringClass().getName() + "." + method.getName().substring(4) + ": "
                + duration + " ns for " + iterations + " iterations");
        System.out.printf("%-40s%.2f ns per iteration\n", method.getDeclaringClass().getName()
                + "." + method.getName().substring(4) + ":", iteration_time);
    }

    public int runBenchSet(String test, String subtest) {
        if (test == null) {
            return 1;
        }
        List<Method> methods = new ArrayList<Method>(5);
        try {
            Class<?> clazz = Class.forName(test);
            Object instance = clazz.newInstance();
            if (subtest != null) {
                Method m = clazz.getMethod(TESTNAME_PREFIX + subtest, int.class);
                methods.add(m);
            } else {
                for (Method m : clazz.getDeclaredMethods()) {
                    if (m.getName().startsWith(TESTNAME_PREFIX)) {
                        methods.add(m);
                    }
                }
            }
            // Sort methods by name.
            Collections.sort(methods, new Comparator<Method>() {
                @Override
                public int compare(Method m1, Method m2) {
                    return m1.getName().compareTo(m2.getName());
                }
            });

            for (Method m : methods) {
                // Run each method as a benchmark.
                runOneBench(instance, m);
            }

        } catch (Exception e) {
            // TODO: filter exceptions.
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    public void parseCmdlineAndRun(String[] args) {
        String test = null;
        String subtest = null;
        // TODO: help message
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                String option = args[i].substring(2);
                if (option.equals("subtest")) {
                    i++;
                    if (i < args.length) {
                        subtest = args[i];
                    } else {
                        log.fatal("Require subtest name.");
                    }
                } else if (option.equals("verbose")) {
                    setLogLevel(SimpleLogger.LogLevel.INFO);
                } else if (option.equals("debug")) {
                    setLogLevel(SimpleLogger.LogLevel.DEBUG);
                } else if (option.equals("benchmark_run_time")) {
                    i++;
                    if (i < args.length) {
                        this.benchmarkTargetRunTime = Long.valueOf(args[i]) * 1000000; // milliseconds
                    } else {
                        log.fatal("Require time.");
                    }
                } else if (option.equals("calibration_min_time")) {
                    i++;
                    if (i < args.length) {
                        this.calibrationMinTime = Long.valueOf(args[i]) * 1000000; // milliseconds
                    } else {
                        log.fatal("Require time.");
                    }
                }
            } else {
                test = args[i];
            }
        }
        if (runBenchSet(test, subtest) != 0) {
            log.error("Test failed.");
        }
    }

    public static void main(String[] args) {
        RunBench bench = new RunBench();
        // Set default log level.
        bench.parseCmdlineAndRun(args);
    }
}
