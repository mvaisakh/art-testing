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
import fnmatch
import glob
import os
import pickle
import shutil
import subprocess
import sys
import time
import re

import utils

dir_pwd = os.path.dirname(os.path.realpath(__file__))
dir_benchmarks = os.path.join(dir_pwd, 'benchmarks')
dir_framework = os.path.join(dir_pwd, 'framework')
dir_tools = os.path.join(dir_pwd, 'tools')
dir_build = os.path.join(dir_pwd, 'build')
dir_build_java_classes = os.path.join(dir_build, 'classes')
bench_runner_main = 'org.linaro.bench.RunBench'

# The script must be executed from an Android environment, which will be passed
# to run the commands.
environment = os.environ.copy()

# Use a global `verbose` flag to allow scripts importing this file to override
# it.
verbose = False



# Options

default_mode = ''
default_n_iterations = 5
default_remote_copy_path = '/data/local/tmp'

def BuildOptions():
    parser = argparse.ArgumentParser(
        description = "Run java benchmarks.",
        # Print default values.
        formatter_class = argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument('--iterations', '-i', metavar = 'N', type = int,
                        default = default_n_iterations,
                        help = "Run <N> iterations of the benchmarks.")
    parser.add_argument('--dont-auto-calibrate',
                        action='store_false', default = True,
                        dest = 'auto_calibrate', help='''Do not auto-calibrate
                        the benchmarks. Instead, run each benchmark's `main()`
                        function directly.''')
    parser.add_argument('--target', '-t', action='store_true', default = False,
                        dest='run_on_target', help='Run on target adb device.')
    parser.add_argument('--mode', action = 'store',
                        choices = ['32', '64', ''], default = default_mode,
                        help='''Run with dalvikvm32, dalvikvm64, or dalvikvm''')
    parser.add_argument('-n', '--norun', action='store_true',
                        help='Build and configure everything, but do not run the benchmarks.')
    parser.add_argument('--noverbose', action='store_true', default = False,
                        help='Do not print extra information and commands run.')
    parser.add_argument('--remote_copy_path', action = 'store',
                        default = default_remote_copy_path,
                        help = '''Path where objects should be copied on the
                        target.''')
    parser.add_argument('-f', '--filter', action = 'append',
                        help='Quoted (benchmark name) filter pattern.')
    parser.add_argument('-F', '--filter-out', action = 'append',
                        help='''Filter out the benchmarks matching this patern.
                             (default: [\'deprecated/*\']''')
    parser.add_argument('--output-pkl', action = 'store',
                        help='Specify a name for the output `.pkl` file.')
    return parser.parse_args()



# Utils

def VerbosePrint(msg):
    if verbose: print(msg)

def ensure_dir(path):
    if not os.path.exists(path):
        os.makedirs(path)

def get_files(ext, path):
    return glob.glob(os.path.join(path, '*.' + ext))

# ADB helpers

def adb_push(f, target_path = default_remote_copy_path):
    command = ['adb', 'push', f, target_path]
    VerbosePrint(' '.join(command))
    p = subprocess.Popen(command, env = environment,
                         stdout = subprocess.PIPE, stderr = subprocess.PIPE)
    return p.communicate()


# `command` is expected to be a string, not a list.
def adb_shell(command):
    # We need to quote the actual command in the text printed so it can be
    # copy-pasted and executed.
    VerbosePrint('adb shell ' + "\"%s\"" % command)
    command = ['adb', 'shell', command]
    p = subprocess.Popen(command, env = environment,
                         stdout = subprocess.PIPE, stderr = subprocess.PIPE)
    return p.communicate()

def host_java(command):
    VerbosePrint(' '.join(command))
    p = subprocess.Popen(command, cwd = dir_build_java_classes,
                         stdout = subprocess.PIPE, stderr = subprocess.PIPE)
    return p.communicate()

def DeleteAppInDalvikCache(remote_copy_path):
    # We delete the entire dalvik-cache in the test path.
    # Delete any cached version of the benchmark app.
    # With the current defaults, the pattern is "data@local@tmp@java-benchs.apk*"
    adb_shell('rm -rf ' + os.path.join(remote_copy_path, 'dalvik-cache'))

def BuildBenchmarks(build_for_target):
    # Call the build script, with warnings treated as errors.
    command = ['./build.sh', '-w']
    if not build_for_target:
        # Only build for the host.
        command += ['-H']
    VerbosePrint(' '.join(command))
    subprocess.check_call(command)

def RunBenchADB(mode, auto_calibrate, apk, classname):
    dalvikvm = 'dalvikvm%s' % mode
    command = ("cd %s && ANDROID_DATA=`pwd` DEX_LOCATION=`pwd` %s -cp %s"
            % (os.path.dirname(apk), dalvikvm, apk))
    if args.auto_calibrate:
        # Run the benchmark's time* method(s) via bench_runner_main
        command += " %s %s" % (bench_runner_main, classname)
    else:
        # Run the benchmark as a main class directly
        command += " %s" % (classname)
    if verbose:
        command += " --debug"
    out, err = adb_shell(command)
    return out.decode('UTF-8')

def RunBenchHost(mode, auto_calibrate, apk, classname):
    if auto_calibrate:
        command = ['java', bench_runner_main, classname]
    else:
        command = ['java', classname]
    out, err = host_java(command)
    return out.decode('UTF-8')


# TODO: Avoid using global variables.
result = dict()

def RunBench(apk, classname,
             run_helper,
             auto_calibrate,
             iterations = default_n_iterations, mode = default_mode):
    for iteration in range(iterations):
        try:
            out = run_helper(mode, auto_calibrate, apk, classname)
            out = out.rstrip('\n')
            if verbose:
                print(out)
        except Exception as e:
            print(e)
            sys.stderr.write("  \-> FAILED, continuing anyway\n")
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
            break



def RunBenchs(apk, bench_names,
              run_on_target,
              auto_calibrate,
              iterations = default_n_iterations, mode = default_mode):
    VerbosePrint('\n# Running benchmarks: ' + ' '.join(bench_names))
    run_helper = RunBenchADB if run_on_target else RunBenchHost
    for bench in bench_names:
        RunBench(apk, bench, run_helper, auto_calibrate, iterations = iterations, mode = mode)


def ListAllBenchmarks():
    list_benchs = []
    # List java files in 'benchmarks/'.
    list_benchs += get_files('java', dir_benchmarks)
    # List java files in subdirectories of 'benchmarks/', except in 'com' which
    # contain the framework code.
    bench_subdirs = [x for x in os.listdir(dir_benchmarks) if os.path.isdir(os.path.join(dir_benchmarks, x)) and x != 'com']
    for subdir in bench_subdirs:
        for root, dirs, files in os.walk(os.path.join(dir_benchmarks, subdir)):
            list_benchs += map(lambda x : os.path.join(root, x), files)
    list_benchs = [f for f in list_benchs if re.match(r'^[\w\/-]+\.java$', f)]
    list_benchs = list(map(lambda x : os.path.relpath(x, dir_benchmarks), list_benchs))
    list_benchs = list(map(lambda x : x.replace('.java', ''), list_benchs))
    list_benchs.sort()
    return list_benchs


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
    verbose = not args.noverbose

    BuildBenchmarks(args.run_on_target)

    remote_apk = None
    if args.run_on_target:
        DeleteAppInDalvikCache(args.remote_copy_path)
        apk = './build/bench.apk'
        apk_name = os.path.basename(apk)
        adb_push(apk, args.remote_copy_path)
        remote_apk = os.path.join(args.remote_copy_path, apk_name)

    if args.norun:
        sys.exit(0)

    benchmarks = ListAllBenchmarks()

    # The deprecated benchmarks should not be implicitly filtered out when
    # filters are explicitly specified on the command line.
    if args.filter is not None or args.filter_out is not None:
        filter_out = args.filter_out
    else:
        filter_out = ['deprecated/*']
    benchmarks = FilterBenchmarks(benchmarks, args.filter, filter_out)
    bench_class_names = list(map(os.path.basename, benchmarks))

    RunBenchs(remote_apk, bench_class_names, args.run_on_target, args.auto_calibrate, args.iterations, args.mode)
    utils.PrintStats(result, iterations = args.iterations)
    print('')
    # Write the results to a file so they can later be used with `compare.py`.
    res_filename = args.output_pkl if args.output_pkl is not None else 'res.' + time.strftime("%Y.%m.%d-%H:%M:%S") + '.pkl'
    with open(res_filename, 'wb') as pickle_file:
        pickle.dump(result, pickle_file)
        print(('Wrote results to %s.' % res_filename))
