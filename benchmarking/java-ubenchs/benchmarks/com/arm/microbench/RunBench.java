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
import java.util.concurrent.TimeUnit;

public class RunBench {
    // Minimum valid calibration time: 400ms.
    public final static long DEFAULT_CALIBRATION_MIN_TIME_NS = TimeUnit.NANOSECONDS.convert(400, TimeUnit.MILLISECONDS);
    // The target benchmark running time: 2s.
    public final static long DEFAULT_BENCHMARK_TARGET_RUN_TIME_NS = TimeUnit.NANOSECONDS.convert(2, TimeUnit.SECONDS);
    public final static int ITERATIONS_LIMIT = 0x40000000;

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

        log.info(method.getDeclaringClass().getName() + "." + method.getName().substring(4) + ": "
                + duration + " ns for " + iterations + " iterations");
        System.out.printf("%-40s%.2f ns per iteration\n", method.getDeclaringClass().getName()
                + "." + method.getName().substring(4) + ":", iteration_time);
    }

    public int runBenchSet(String test, String subtest, boolean verify) {
        if (test == null) {
            return 1;
        }
        List<Method> benchMethods = new ArrayList<Method>(5);
        Method verifyMethod = null;
        try {
            Class<?> clazz = Class.forName(test);
            Object instance = clazz.newInstance();
            if (subtest != null) {
                Method m = clazz.getMethod(TESTNAME_PREFIX + subtest, int.class);
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

    public void parseCmdlineAndRun(String[] args) {
        String test = null;
        String subtest = null;
        boolean verify = true;  // Verify all benchmark results by default.

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
                        this.benchmarkTargetRunTimeNS = TimeUnit.NANOSECONDS.convert(Long.valueOf(args[i]), TimeUnit.MILLISECONDS);
                    } else {
                        log.fatal("Require time.");
                    }
                } else if (option.equals("calibration_min_time")) {
                    i++;
                    if (i < args.length) {
                        this.calibrationMinTimeNS = TimeUnit.NANOSECONDS.convert(Long.valueOf(args[i]), TimeUnit.MILLISECONDS);
                    } else {
                        log.fatal("Require time.");
                    }
                } else if (option.equals("noverify")) {
                    verify = false;
                }
            } else {
                test = args[i];
            }
        }
        if (runBenchSet(test, subtest, verify) != 0) {
            log.error("Test failed.");
        }
    }

    public static void main(String[] args) {
        RunBench bench = new RunBench();
        // Set default log level.
        bench.parseCmdlineAndRun(args);
    }
}
