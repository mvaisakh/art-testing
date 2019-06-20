/*
 *    Copyright 2019 Linaro Limited
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Benchmark is a class that is a bridge to the actual code doing benchmarking.
 * Benchmark supports code following the format:
 *
 *    - Instance methods with the prefix 'time' are benchmark code. They
 *      must have one parameter of the type 'int' which means a number of
 *      iterations. A method can have IterationAnnotation which specifies a
 *      number of iterations. If the method does not it, a calibration process
 *      is used to find out a needed number of iterations.
 *
 *    - Instance methods with the prefix 'setup' are used to initialize
 *      benchmark data.
 *
 *    - Instance methods with the prefix 'verify' are used to check that
 *      'time' methods produce correct results.
 */
public class Benchmark {
  private static final String TIME_BENCH_METHOD_PREFIX = "time";

  private static final String SETUP_METHOD_PREFIX = "setup";

  private static final String VERIFY_BENCH_METHOD_PREFIX = "verify";

  private static final int ITERATIONS_LIMIT = 0x400000;

  /*
   * BenchmarkMethod is a class to work with methods containing benchmarking code.
   * Those methods run benchmarking code a number of iterations. The number of
   * iterations can either be provided by the methods via IterationsAnnotation
   * or be calculated using calibration process.
   */
  private static class BenchmarkMethod {
    private Object parent;
    private Method method;
    private int iterationsCount;
    private String id;
    private boolean doWarmup;

    public BenchmarkMethod(Object parent, Method method) {
      this.parent = parent;
      this.method = method;
      this.id = benchmarkIdentifier(method);
      this.doWarmup = true;

      this.iterationsCount = -1;
      IterationsAnnotation annotation = method.getAnnotation(IterationsAnnotation.class);
      if (annotation != null) {
        this.doWarmup = !annotation.noWarmup();
        if (annotation.iterations() > 0) {
          this.iterationsCount = annotation.iterations();
        }
      }
    }

    public String getID() {
      return id;
    }

    public boolean needsCalibration() {
      return iterationsCount == -1;
    }

    public void calibrateIterations(long calibrationMinTimeNs, long targetRunTimeNs) {
      // Estimate how long it takes to run one iteration.
      long iterations = 1;
      long duration = -1;
      while ((duration < calibrationMinTimeNs) && (iterations < ITERATIONS_LIMIT)) {
        iterations *= 2;
        duration = timeIterations((int) iterations);
      }
      // Estimate the number of iterations to run based on the calibration
      // phase, and benchmark the function.
      double iterTime = duration / (double) iterations;
      this.iterationsCount = (int) Math.max(1.0, targetRunTimeNs / iterTime);
    }

    public Result run() {
      SimpleLogger log = SimpleLogger.getInstance();
      log.debug("Running method: " + method.toString());

      if (this.doWarmup) {
        warmup();
      }

      long duration = timeIterations();
      log.info(id + ": " + duration + " ns for " + iterationsCount + " iterations");

      return new Result(this, duration, iterationsCount);
    }

    private long timeIterations() {
      int iterations = this.iterationsCount;
      if (needsCalibration()) {
        SimpleLogger log = SimpleLogger.getInstance();
        log.error(id + " is not calibrated. The iterations count to be used is 1.");
        iterations = 1;
      }

      return timeIterations(iterations);
    }

    private void warmup() {
      int iterations = this.iterationsCount / 10;
      if (iterations == 0) iterations = 1;
      timeIterations(iterations);
    }

    private long timeIterations(int iterationsCount) {
      long start = 0;
      long end = 0;
      try {
        start = System.nanoTime();
        method.invoke(parent, iterationsCount);
        end = System.nanoTime();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      return end - start;
    }
  }

  /*
   * Result represents a result of a benchmarking method.
   */
  public static class Result {
    private BenchmarkMethod benchMethod;
    private long duration;
    private int iterations;

    private Result(BenchmarkMethod benchMethod, long duration, int iterations) {
      this.benchMethod = benchMethod;
      this.duration = duration;
      this.iterations = iterations;
    }

    public String toString() {
      return String.format(
          "%-40s%.2f ns per iteration", benchMethod.getID() + ": ", duration / (double) iterations);
    }

    public boolean isBetterThan(Result result) {
      if (result == null) {
        return true;
      }
      return this.duration / this.iterations < result.duration / result.iterations;
    }
  }

  private interface MethodSelector {
    public boolean accept(Method method);
  }

  private final static class SetupMethodSelector implements MethodSelector {
    public boolean accept(Method method) {
      return method.getName().startsWith(SETUP_METHOD_PREFIX) && method.getParameterCount() == 0;
    }
  }

  private static boolean DoesMethodHaveOneIntParam(Method method) {
    return method.getParameterCount() == 1 && method.getParameterTypes()[0] == int.class;
  }

  private final static class TimeBenchmarkMethodSelector implements MethodSelector {
    public boolean accept(Method method) {
      return method.getName().startsWith(TIME_BENCH_METHOD_PREFIX)
          && DoesMethodHaveOneIntParam(method);
    }
  }

  private final static class ParticularBenchmarkMethodSelector implements MethodSelector {
    private String particularBenchMethodName;

    public ParticularBenchmarkMethodSelector(String methodName) {
      particularBenchMethodName = methodName;
    }

    public boolean accept(Method method) {
      return method.getName().equals(particularBenchMethodName)
          && DoesMethodHaveOneIntParam(method);
    }
  }

  private final static class VerifyMethodSelector implements MethodSelector {
    public boolean accept(Method method) {
      return method.getName().startsWith(VERIFY_BENCH_METHOD_PREFIX)
          && method.getReturnType() == boolean.class
          && method.getParameterCount() == 0;
    }
  }

  private Object benchInstance;
  private List<Method> setupMethods;
  private List<BenchmarkMethod> benchMethods;
  private List<Method> verifyMethods;

  public Benchmark(String benchName, long calibrationMinTimeNs, long benchmarkTargetRunTimeNs) {
    if (benchName == null) {
      throw new NullPointerException("The provided benchmark name is null.");
    }

    if (benchName.isEmpty()) {
      throw new IllegalArgumentException("The provided benchmark name is an empty string");
    }

    // The benchmark name format is:
    //    path/to/BenchmarkClass(.Benchmark)?
    Pattern format = Pattern.compile("((?:\\w+\\/)*)(\\w+)(?:\\.(\\w+))?$");
    Matcher matcher = format.matcher(benchName);
    if (!matcher.matches()) {
      throw new IllegalArgumentException(
          "The provided benchmark name has an unexpected format: " + benchName);
    }
    String benchmarkClassPath = matcher.group(1);
    if (!benchmarkClassPath.startsWith("benchmarks/")) {
      benchmarkClassPath = "benchmarks/" + benchmarkClassPath;
    }
    benchmarkClassPath = benchmarkClassPath.replace('/', '.');
    String benchmarkClass = matcher.group(2);
    String benchmarkMethodName = matcher.group(3);

    // Each method declared in benchmarkClass is checked whether it is
    // one of special methods ('time', 'setup', 'verify').
    // Found methods are stored into corresponding lists.
    setupMethods = new ArrayList<Method>();
    benchMethods = new ArrayList<BenchmarkMethod>();
    verifyMethods = new ArrayList<Method>();
    try {
      Class<?> clazz = Class.forName(benchmarkClassPath + benchmarkClass);
      benchInstance = clazz.newInstance();

      MethodSelector setupMethodsSelector = new SetupMethodSelector();
      MethodSelector benchMethodsSelector =
          (benchmarkMethodName != null)
              ? new ParticularBenchmarkMethodSelector(
                  TIME_BENCH_METHOD_PREFIX + benchmarkMethodName)
              : new TimeBenchmarkMethodSelector();
      MethodSelector verifyMethodsSelector = new VerifyMethodSelector();

      for (Method method : clazz.getDeclaredMethods()) {
        if (setupMethodsSelector.accept(method)) {
          setupMethods.add(method);
        } else if (benchMethodsSelector.accept(method)) {
          benchMethods.add(new BenchmarkMethod(benchInstance, method));
        } else if (verifyMethodsSelector.accept(method)) {
          verifyMethods.add(method);
        }
      }

      if (benchMethods.isEmpty()) {
        throw new RuntimeException("No benchmark method in the benchmark: " + benchName);
      }

      // After all methods are processed a benchmarks is setup. This includes
      // the iteration calibration process if it is needed.
      setup(calibrationMinTimeNs, benchmarkTargetRunTimeNs);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create a benchmark: " + benchName, e);
    }
  }

  public Result[] run() {
    Result[] results = new Result[benchMethods.size()];

    int i = 0;
    for (BenchmarkMethod method : benchMethods) {
      results[i++] = method.run();
    }

    // Sort results by method's name.
    Arrays.sort(
        results,
        new Comparator<Result>() {
          @Override
          public int compare(Result r1, Result r2) {
            return r1.benchMethod.getID().compareTo(r2.benchMethod.getID());
          }
        });

    return results;
  }

  public int verify() {
    SimpleLogger log = SimpleLogger.getInstance();
    int verifyFailures = 0;
    try {
      for (Method verifyMethod : verifyMethods) {
        if (!(Boolean) verifyMethod.invoke(benchInstance)) {
          log.error(verifyMethod.getName() + " failed.");
          ++verifyFailures;
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return verifyFailures;
  }

  private void setup(long calibrationMinTimeNs, long benchmarkTargetRunTimeNs) {
    try {
      for (Method method : setupMethods) {
        method.invoke(benchInstance);
      }

      for (BenchmarkMethod method : benchMethods) {
        if (method.needsCalibration()) {
          method.calibrateIterations(calibrationMinTimeNs, benchmarkTargetRunTimeNs);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static String benchmarkIdentifier(Method method) {
    Pattern format = Pattern.compile("((?:\\w+\\.)*)(\\w+)");
    Matcher matcher = format.matcher(method.getDeclaringClass().getName());
    if (!matcher.matches()) {
      return null;
    }
    String path = matcher.group(1);
    path = path.replace('.', '/');
    String className = matcher.group(2);
    // Filter the "time" prefix.
    String benchName = method.getName().substring(4);
    return path + className + "." + benchName;
  }
}
