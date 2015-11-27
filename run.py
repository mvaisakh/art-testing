#! /usr/bin/env python3

#    Copyright 2015 ARM Limited
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# TODO: error handling

import argparse
import csv
import fnmatch
import os
import subprocess
import sys
import time

from collections import OrderedDict

dir_root = os.path.dirname(os.path.realpath(__file__))
dir_tools = os.path.join(dir_root, 'tools')
sys.path.insert(0, dir_tools)
import utils
import utils_adb
import utils_stats

bench_runner_main = 'org.linaro.bench.RunBench'

# Options

default_mode = ''
default_n_iterations = 1

def BuildOptions():
    parser = argparse.ArgumentParser(
        description = "Run java benchmarks.",
        # Print default values.
        formatter_class = argparse.ArgumentDefaultsHelpFormatter)
    utils.AddCommonRunOptions(parser)
    utils.AddOutputFormatOptions(parser, utils.default_output_formats + ['csv'])
    parser.add_argument('--dont-auto-calibrate',
                        action='store_true', default = False,
                        dest = 'no_auto_calibrate', help='''Do not auto-calibrate
                        the benchmarks. Instead, run each benchmark's `main()`
                        function directly.''')
    parser.add_argument('-n', '--norun', action='store_true',
                        help='Build and configure everything, but do not run the benchmarks.')
    parser.add_argument('--noverbose', action='store_true', default = False,
                        help='Do not print extra information and commands run.')
    parser.add_argument('-f', '--filter', action = 'append',
                        help='Quoted (benchmark name) filter pattern.')
    parser.add_argument('-F', '--filter-out', action = 'append',
                        help='''Filter out the benchmarks matching this patern.
                             Defaults to \'benchmarks/deprecated/*\' if no other filter is
                             specified.''')

    args = parser.parse_args()

    if args.mode and not args.target:
        utils.Error('The `--mode` option is only valid when `--target` is specified.')

    return args

def host_java(command):
    utils.VerbosePrint(' '.join(command))
    p = subprocess.Popen(command, cwd = utils.dir_build_java_classes,
                         stdout = subprocess.PIPE, stderr = subprocess.PIPE)
    rc = p.wait()
    out, err = p.communicate()
    return rc, out, err

def DeleteAppInDalvikCache(target_copy_path, target):
    # We delete the entire dalvik-cache in the test path.
    # Delete any cached version of the benchmark app.
    # With the current defaults, the pattern is "data@local@tmp@java-benchs.apk*"
    utils_adb.shell('rm -rf ' + os.path.join(target_copy_path, 'dalvik-cache'), target)

def BuildBenchmarks(build_for_target):
    # Call the build script.
    command = [os.path.join(dir_root, 'build.sh')]
    if build_for_target:
        command += ['-t']
    utils.VerbosePrint(' '.join(command))
    subprocess.check_call(command)

def RunBenchADB(mode, auto_calibrate, apk, classname, target):
    dalvikvm = 'dalvikvm%s' % mode
    command = ("cd %s && ANDROID_DATA=`pwd` DEX_LOCATION=`pwd` %s -cp %s"
            % (os.path.dirname(apk), dalvikvm, apk))
    if auto_calibrate:
        # Run the benchmark's time* method(s) via bench_runner_main
        command += " %s %s" % (bench_runner_main, classname)
    else:
        # Run the benchmark as a main class directly
        command += " %s" % (classname)
    if utils.verbose:
        command += " --debug"
    rc, out, err = utils_adb.shell(command, target)
    return rc, out.decode(), err.decode()

def RunBenchHost(mode, auto_calibrate, apk, classname, target):
    if auto_calibrate:
        command = ['java', bench_runner_main, classname]
    else:
        command = ['java', classname]
    rc, out, err = host_java(command)
    return rc, out.decode(), err.decode()


# TODO: Avoid using global variables.
result = dict()

def RunBench(apk, classname,
             run_helper,
             auto_calibrate,
             iterations = default_n_iterations,
             mode = default_mode,
             target = None):
    rc = 0
    for iteration in range(iterations):
        try:
            local_rc, out, err = run_helper(mode,
                                            auto_calibrate,
                                            apk,
                                            classname,
                                            target)
            rc += local_rc
            out = out.rstrip('\n')
            if local_rc != 0:
                print("ERROR:")
                print(err)
                print(out)
            elif utils.verbose:
                print(out)
        except Exception as e:
            print(e)
            sys.stderr.write("  \-> FAILED, continuing anyway\n")
            rc += 1
            continue

        try:
            for line in out.rstrip().split("\n"):
                name = line.split(":")[0].rstrip()
                # Ignore any java logging from --debug
                if name not in ['INFO', 'DEBUG', 'ERROR']:
                    score = float(line.split(":")[1].strip().split(" ")[0].strip())
                    if name not in result:
                        result[name] = list()
                    result[name].append(score)
        except Exception as e:
            print(e)
            print("  \-> Error parsing output from %s" % classname)
            rc += 1
            break

    return rc



def RunBenchs(apk, bench_names,
              target,
              auto_calibrate,
              iterations = default_n_iterations, mode = default_mode):
    rc = 0
    utils.VerbosePrint('\n# Running benchmarks: ' + ' '.join(bench_names))
    run_helper = RunBenchADB if target else RunBenchHost
    for bench in bench_names:
        rc += RunBench(apk,
                       bench,
                       run_helper,
                       auto_calibrate,
                       iterations = iterations,
                       mode = mode,
                       target = target)
    return rc


def ListAllBenchmarks():
    out = subprocess.check_output(['java', 'org.linaro.bench.RunBench', '--list_benchmarks'],
                                  cwd=utils.dir_build_java_classes)
    out = out.decode().rstrip()
    benchs = out.split('\n')
    return benchs


def FilterBenchmarks(benchmarks, filter, filter_out):
    res = benchmarks
    if filter:
        res = []
        for f in filter:
            res += [x for x in benchmarks if fnmatch.fnmatch(x, f)]
    if filter_out:
        for f in filter_out:
            res = [x for x in res if not fnmatch.fnmatch(x, f)]
    return res


if __name__ == "__main__":
    args = BuildOptions()
    utils.verbose = not args.noverbose
    BuildBenchmarks(args.target)

    remote_apk = None
    if args.target:
        DeleteAppInDalvikCache(args.target_copy_path, args.target)
        apk = os.path.join(utils.dir_build, 'bench.apk')
        apk_name = os.path.basename(apk)
        utils_adb.push(apk, args.target_copy_path, args.target)
        remote_apk = os.path.join(args.target_copy_path, apk_name)

    if args.norun:
        sys.exit(0)

    benchmarks = ListAllBenchmarks()

    # The deprecated benchmarks should not be implicitly filtered out when
    # filters are explicitly specified on the command line.
    if args.filter is not None or args.filter_out is not None:
        filter_out = args.filter_out
    else:
        filter_out = ['benchmarks/deprecated/*']
    benchmarks = FilterBenchmarks(benchmarks, args.filter, filter_out)

    rc = RunBenchs(remote_apk,
                   benchmarks,
                   args.target,
                   not args.no_auto_calibrate,
                   args.iterations,
                   args.mode)
    result = OrderedDict(sorted(result.items()))
    utils_stats.PrintStats(result, iterations = args.iterations)
    print('')

    utils.OutputObject(result, 'pkl', args.output_pkl)
    utils.OutputObject(result, 'json', args.output_json)
    # Output in CSV format.
    # Transform the dictionary into a list of lists.
    output_filename = args.output_csv
    utils.ensure_dir(os.path.dirname(output_filename))
    data = []
    for bench in result:
        data += [[bench] + result[bench]]
    with open(output_filename, 'w') as output_file:
        writer = csv.writer(output_file, delimiter=',')
        writer.writerows(data)
        print('Wrote results to %s.' % output_filename)

    if rc != 0:
        print("ERROR: The benchmarks did *not* run successfully. (rc = %d)" % rc)
    sys.exit(rc)
