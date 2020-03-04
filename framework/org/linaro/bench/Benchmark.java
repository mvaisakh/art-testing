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
import java.util.concurrent.TimeUnit;
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

  // Default target running time for benchmarks.
  public static final long DEFAULT_TARGET_RUNNING_TIME_MS = 400;

  // The constant to indicate the unknown value of the calibration time.
  private static final long UNKNOWN_CALIBRATION_TIME = -1;

  /*
   * BenchmarkMethod is a class to work with methods containing benchmarking code.
   * Those methods run benchmarking code a number of iterations. The number of
   * iterations can be explicitly provided in the constructor or via methods IterationsAnnotation
   * or be calculated using calibration process.
   */
  private static class BenchmarkMethod {
    // The constant to indicate the unknown value of the iteration count.
    private static final int UNKNOWN_ITERATION_COUNT = -1;

    private Object parent;
    private Method method;
    private int iterationsCount;
    private String id;
    private boolean doWarmup;

    // Construct BenchmarkMethod with the provided iteration count.
    // Arguments:
    //   parent - an instance of the class containing benchmarking methods.
    //   method - a method containing benchmarking code.
    //   iterationCount - a number of iterations the method to run
    //                    benchmarking code. UNKNOWN_ITERATION_COUNT can be used
    //                    if the iteration count is provided later.
    public BenchmarkMethod(Object parent, Method method, int iterationCount) {
      this.parent = parent;
      this.method = method;
      this.id = benchmarkIdentifier(method);
      this.doWarmup = true;
      this.iterationsCount = iterationCount;
    }

    // Construct BenchmarkMethod based on the annotation if the method provides it.
    // If the method does not have the annotation, the iteration count will be set to
    // UNKNOWN_ITERATION_COUNT.
    // Arguments:
    //   parent - an instance of the class containing benchmarking methods.
    //   method - a method containing benchmarking code.
    public BenchmarkMethod(Object parent, Method method) {
      this(parent, method, UNKNOWN_ITERATION_COUNT);
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

    public String getName() {
      return method.getName();
    }

    public int getIterationCount() {
      return iterationsCount;
    }

    public boolean needsCalibration() {
      return iterationsCount == UNKNOWN_ITERATION_COUNT;
    }

    public void calibrateIterations() {
      // Estimate how long it takes to run one iteration.
      long iterations = 1;
      long duration = -1;
      if (Benchmark.calibrationTimeNs == UNKNOWN_CALIBRATION_TIME) {
        Benchmark.calibrationTimeNs = Benchmark.calculateCalibrationTimeNs();
      }
      while ((duration < calibrationTimeNs) && (iterations < ITERATIONS_LIMIT)) {
        iterations *= 2;
        duration = timeIterations((int) iterations);
      }
      // Estimate the number of iterations to run based on the calibration
      // phase, and benchmark the function.
      double iterTime = duration / (double) iterations;
      this.iterationsCount = (int) Math.max(1.0, Benchmark.targetRunningTimeNs / iterTime);
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

  private static final class SetupMethodSelector implements MethodSelector {
    public boolean accept(Method method) {
      return method.getName().startsWith(SETUP_METHOD_PREFIX) && method.getParameterCount() == 0;
    }
  }

  private static boolean doesMethodHaveOneIntParam(Method method) {
    return method.getParameterCount() == 1 && method.getParameterTypes()[0] == int.class;
  }

  private static final class TimeBenchmarkMethodSelector implements MethodSelector {
    public boolean accept(Method method) {
      return method.getName().startsWith(TIME_BENCH_METHOD_PREFIX)
          && doesMethodHaveOneIntParam(method);
    }
  }

  private static final class VerifyMethodSelector implements MethodSelector {
    public boolean accept(Method method) {
      return method.getName().startsWith(VERIFY_BENCH_METHOD_PREFIX)
          && method.getReturnType() == boolean.class
          && method.getParameterCount() == 0;
    }
  }

  /*
   * This class allows to access information encoded into the benchmark specification.
   * The benchmark specification format:
   *   <benchmark_class_name>[:<benchmark_method>:<iterations>]+
   */
  private static class BenchmarkSpecification {
    String[] parts;

    public BenchmarkSpecification(String str) {
      parts = str.split(":");
      if (parts.length < 3  || (parts.length % 2) != 1) {
        throw new IllegalArgumentException("The provided benchmark specification is invalid.");
      }
    }

    public String getClassName() {
      return parts[0];
    }

    public int getMethodCount() {
      // The method count is the number of parts minus 1 for the class name and divided by 2
      // because it is in format <method name>:<iteration count>
      return (parts.length - 1) / 2;
    }

    private int getMethodPartIndex(int index) {
      return 2 * index + 1; // skipping the class name.
    }

    public String getMethodName(int index) {
      return parts[getMethodPartIndex(index)];
    }

    public int getMethodIterationCount(int index) {
      return Integer.parseInt(parts[getMethodPartIndex(index) + 1]);
    }
  }

  // The target running time for benchmarks.
  private static long targetRunningTimeNs =
        TimeUnit.NANOSECONDS.convert(DEFAULT_TARGET_RUNNING_TIME_MS, TimeUnit.MILLISECONDS);
  // The time used for calibration of benchmarks. It is usually less than the target running time
  // to have the calibration process as quick as possible. UNKNOWN_CALIBRATION_TIME means the time
  // has not been provided by an user. It will be calculated based on the target running time.
  private static long calibrationTimeNs = UNKNOWN_CALIBRATION_TIME;

  private Object benchInstance;
  private List<Method> setupMethods = new ArrayList<Method>();
  private List<BenchmarkMethod> benchMethods = new ArrayList<BenchmarkMethod>();
  private List<Method> verifyMethods = new ArrayList<Method>();

  private void findSetupAndVerifyMethods() {
    // Each method declared in benchmarkClass is checked whether it is
    // one of special methods ('setup', 'verify').
    // Found methods are stored into corresponding lists.
    MethodSelector setupMethodsSelector = new SetupMethodSelector();
    MethodSelector verifyMethodsSelector = new VerifyMethodSelector();

    for (Method method : benchInstance.getClass().getDeclaredMethods()) {
      if (setupMethodsSelector.accept(method)) {
        setupMethods.add(method);
      } else if (verifyMethodsSelector.accept(method)) {
        verifyMethods.add(method);
      }
    }
  }

  private void findTimeBenchmarkMethods() {
    // Each method declared in benchmarkClass is checked whether it is
    // a special method 'time'.
    MethodSelector benchMethodsSelector = new TimeBenchmarkMethodSelector();
    for (Method method : benchInstance.getClass().getDeclaredMethods()) {
      if (benchMethodsSelector.accept(method)) {
        benchMethods.add(new BenchmarkMethod(benchInstance, method));
      }
    }
  }

  private void createBenchInstance(String className) {
    try {
      Class<?> clazz = Class.forName(className);
      benchInstance = clazz.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Failed to create a benchmark instance: " + className, e);
    }
  }

  // Construct Benchmark based on BenchmarkSpecification, without calibration.
  private Benchmark(BenchmarkSpecification benchmarkSpecification) {
    createBenchInstance(benchmarkSpecification.getClassName());
    findSetupAndVerifyMethods();
    for (int i = 0; i < benchmarkSpecification.getMethodCount(); ++i) {
      String methodName = benchmarkSpecification.getMethodName(i);
      int iterationCount = benchmarkSpecification.getMethodIterationCount(i);
      try {
        Method method = benchInstance.getClass().getDeclaredMethod(methodName, int.class);
        benchMethods.add(new BenchmarkMethod(benchInstance, method, iterationCount));
      } catch (Exception e) {
        throw new RuntimeException("Failed to get the benchmark method: " + methodName, e);
      }
    }
    // After all methods are processed the benchmark is setup.
    setup();
  }

  // Construct Benchmark based on the name format:
  //   path/to/BenchmarkClass(.Benchmark)?
  // All benchmarking methods of Benchmark are calibrated if needed.
  private Benchmark(String benchName) {
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

    createBenchInstance(benchmarkClassPath + benchmarkClass);
    findSetupAndVerifyMethods();

    try {
      if (benchmarkMethodName == null) {
        findTimeBenchmarkMethods();
      } else {
        Method method = benchInstance.getClass().getDeclaredMethod(TIME_BENCH_METHOD_PREFIX
            + benchmarkMethodName, int.class);
        benchMethods.add(new BenchmarkMethod(benchInstance, method));
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to create a benchmark: " + benchName, e);
    }

    if (benchMethods.isEmpty()) {
      throw new RuntimeException("No benchmark method in the benchmark: " + benchName);
    }

    // After all methods are processed the benchmark is setup and calibrated.
    setup();
    calibrate();
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

  private void setup() {
    try {
      for (Method method : setupMethods) {
        method.invoke(benchInstance);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void calibrate() {
    try {
      for (BenchmarkMethod method : benchMethods) {
        if (method.needsCalibration()) {
          method.calibrateIterations();
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

  // Return the string representation of the benchmark.
  // Its format:
  //   <benchmark_class_name>[:<benchmark_method>:<iterations>]+
  public String toString() {
    StringBuilder resultBuilder = new StringBuilder();
    resultBuilder.append(benchInstance.getClass().getName());
    for (BenchmarkMethod method : benchMethods) {
      resultBuilder.append(':').append(method.getName());
      resultBuilder.append(':').append(method.getIterationCount());
    }
    return resultBuilder.toString();
  }

  // Return an instance of Benchmark based on the provided string.
  public static Benchmark fromString(String str) {
    if (str == null) {
      throw new NullPointerException("The provided string is null.");
    }
    if (str.isEmpty()) {
      throw new IllegalArgumentException("The provided string is empty.");
    }
    if (str.indexOf(':') != -1) {
      // Provided str has the benchmark specification format:
      // <benchmark_class_name>[:<benchmark_method>:<iterations>]+
      return new Benchmark(new BenchmarkSpecification(str));
    } else {
      // Assume that the provided str has the benchmark name format:
      // path/to/BenchmarkClass(.Benchmark)?
      return new Benchmark(str);
    }
  }

  public static void setTargetRunningTime(long time) {
    targetRunningTimeNs =
      TimeUnit.NANOSECONDS.convert(Long.valueOf(time), TimeUnit.MILLISECONDS);
  }

  public static long getTargetRunningTimeNs() {
    return targetRunningTimeNs;
  }

  public static void setCalibrationTime(long time) {
    calibrationTimeNs =
      TimeUnit.NANOSECONDS.convert(Long.valueOf(time), TimeUnit.MILLISECONDS);
  }

  public static long calculateCalibrationTimeNs() {
    // As we want the calibration process to be quick, the calibration time is chosen
    // to be the tenth of the target running time.
    return targetRunningTimeNs / 10;
  }
}
