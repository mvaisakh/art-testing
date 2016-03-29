# ART Performance Tests



## General repository information.

The top-level contains scripts used to build, run, and compare the results of
the Java benchmarks and the APK compilation process statistics.
Other tools are available under tools/<tool> for example to gather memory
statistics or gather profiling information. See the [Tools][] section.

All scripts must include a `--help` or `-h` command-line option displaying
a useful help message.


## Running

### Running via the script helper

Statistics can be obtained with the `run.py` script on host with

    ./run.py

To obtain the results on target, `dx` and `adb` need to be available in your
`PATH`. This will be the case if you run from your Android environment.

    ./run.py --target
    ./run.py --target=<adb target device>

`run.py` provides multiple options.

    ./run.py --target --iterations=5


### Running manually

    ./build.sh

On host

    cd build/classes
    java org/linaro/bench/RunBench --help
    # Run all the benchmarks.
    java org/linaro/bench/RunBench
    # Run a specific benchmark.
    java org/linaro/bench/RunBench benchmarks/micro/Base64
    # Run a specific sub-benchmark.
    java org/linaro/bench/RunBench benchmarks/micro/Base64.Encode
    # Run the specified class directly without auto-calibration.
    java benchmarks/micro/Base64

And similarly on target

    cd build/
    adb push bench.apk /data/local/tmp
    adb shell "cd /data/local/tmp && dalvikvm -cp /data/local/tmp/bench.apk org/linaro/bench/RunBench"
    adb shell "cd /data/local/tmp && dalvikvm -cp /data/local/tmp/bench.apk org/linaro/bench/RunBench benchmarks/micro/Base64"
    adb shell "cd /data/local/tmp && dalvikvm -cp /data/local/tmp/bench.apk org/linaro/bench/RunBench benchmarks/micro/Base64.Encode"
    adb shell "cd /data/local/tmp && dalvikvm -cp /data/local/tmp/bench.apk benchmarks/micro/Base64"


### Comparing the results

The results of `run.py` can be compared using `compare.py`.


    ./run.py --target --iterations=10 --output-json=/tmp/res1.json
    ./run.py --target --iterations=10 --output-json=/tmp/res2.json
    ./compare.py /tmp/res1.json /tmp/res2.json



## Tools

This repository includes other development tools and utilities.

### Benchmarks

The `run.py` and `compare.py` scripts in `tools/benchmarks` allow collecting
and comparing the run times of the Java benchmarks. The options for these
scripts are similar to the API for the top-level scripts. See
`tools/benchmarks/run.py --help` and `tools/benchmarks/compare.py --help`.

### Compilation statistics

The `run.py` and `compare.py` scripts in `tools/compilation_statistics` allow
collecting and comparing statistics about the APK compilation process on target.
The options for these scripts are similar to the API for the top-level scripts.
See `tools/compilation_statistics/run.py --help` and
`tools/compilation_statistics/compare.py --help`.

### Profiling

The `tools/perf` directory includes tools to profile the Java benchmarks on
target and generate an html output. See `tools/perf/PERF.README` for details.



## How to Write a Benchmark

Each set of related benchmarks is implemented as a Java class and kept in the
benchmarks/ folder.

Before contributing, make sure that `test/test.py` passes.


## How to Port an Existing Benchmark

Similar to writing a benchmark, above guidelines also applies to porting an
existing benchmark. Besides, developers should also notice:
1. Licenses:
   Make sure the benchmark has appropriate license for us to integrate it
   freely into our test framework. Apache-v2.0, BSD, MIT licenses are well-
   known and preferred. Check with the gatekeepers for other licenses.
   The original license header in the ported benchmark MUST be *preserved* and
   *unmodified*.

2. Porting a benchmark should be done in two commits:
   (1) Add *untouched* original file *with* its license and copyright header.
   (2) Modify the benchmark as necessary.
   This allows easily showing (`git diff <first commit> <second commit>`)
   what modifications have been made to the original benchmarks.

3. Keep the original code as it is:
   This includes indents, spaces, tabs, etc. Only make changes to original code
   when you have to (e.g. fit into our framework), but keep the changes as
   minimal as possible. When we have to investigate why we're getting different
   results than other projects or developers using the same benchmark, a 'diff'
   should show as few changes as possible. If the original code has some coding
   style which cannot pass our 'checkstyle' script, use 'CHECKSTYLE.OFF' to
   bypass.

4. Header comment:
   When you have modified the code, make sure you comply with the license terms.
   Provide a full copy of the license (Apache2, BSD, MIT, etc.) and notices
   stating that you changed the files (required by Apache2, etc) in the header
   comment. Also, please put description in the header: where did you find the
   benchmark source code and a link to original source.

### Rules

1. Test method names start with "time" -- Test launcher will find all timeXXX()
   methods and run them.
2. Verify methods start with "verify" -- all boolean verifyXXX() methods will
   be run to check the benchmark is working correctly.
3. Leave iterations as parameter -- Test launcher will fill it with a value
   to make sure it runs in a reasonable duration.
4. Without auto-calibration benchmarks should run for a reasonable amount of
   time on target. Between 1 and 10 seconds is acceptable.
   (`tools/benchmarks/run.py --target --dont-auto-calibrate`)

### Example

    public class MyBenchmark {
           public static void main(String [] args) {
                  MyBenchmark b = new MyBenchmark();
                  long before = System.currentTimeMillis();
                  b.timeMethod0(1000);
                  b.timeMethod1(1000);
                  long after = System.currentTimeMillis();
                  System.out.println("MyBenchmark: " + (after - before));
           }

    //                  +----> test method prefix should be "time..."
    //                  |
    // ignored <---+    |              +-------> No need to set iterations. Test
                   |    |              |         framework will try to fill a
                   |    |              |         reasonable value automatically.
    //             |    |              |
           public int timeTestAdd(int iters) {
                  int result = 0;
                  for (int i = 0; i < iters; i++) {
                      // test code
                      testAddResults[i] = i + i;
                  }
                  return result;
           }

           public boolean verifyTestAdd() {
                  boolean result = // test contents of testAddResults[]
                  return result;
           }

    // If you want to fill iterations with your own value. Write a method like:

    //    Don't warm up test <-----+               +---------> Your choice
    //                             |               |
           @IterationsAnnotation(noWarmup=true, iterations=600)
           public long timeSfib(int iters) {
              long sum = 0;
              for (int i = 0; i < iters; i++) {
                  sum += sfib(20);
              }
              return sum;
           }
    }

    // Please refer to existing benchmarks for further examples.


## Test Suite Details

TODO: Detail all benchmarks here, especially what they are intended to achieve.

### e.g. Raytrace

Description, License (if any), Main Focus, Secondary Focus, Additional Comments

### Control Flow Recursive

Control flow recursive is ported from:
https://github.com/WebKit/webkit/blob/master/PerformanceTests/SunSpider/tests/sunspider-1.0.2/controlflow-recursive.js

License is Revised BSD licence:
http://benchmarksgame.alioth.debian.org/license.html

### HashMapBench

Benchmark for hash map, which is converted from:
http://browserbench.org/JetStream/sources/hash-map.js

License is Apache 2.0.

### BitfieldRotate

Large portions Copyright (c) 2000-2015 The Legion of the Bouncy Castle Inc. (http://www.bouncycastle.org)

See BitfieldRotate.java header for license text.

License iS BSD-like.

