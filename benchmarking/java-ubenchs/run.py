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

import utils

dir_pwd = os.path.dirname(os.path.realpath(__file__))
dir_benchmarks = os.path.join(dir_pwd, 'benchmarks')
dir_framework = os.path.join(dir_pwd, 'framework')
dir_tools = os.path.join(dir_pwd, 'tools')
dir_build = os.path.join(dir_pwd, 'build')
dir_build_java_class = os.path.join(dir_build, 'java_class')
bench_runner_main = 'com.arm.microbench.RunBench'

# The script must be executed from an Android environment, which will be passed
# to run the commands.
environment = os.environ.copy()

# When used as a script, argument parsing will override this value.
verbose = False



# Options

default_mode = ''
default_n_iterations = 5
default_remote_copy_path = '/data/local/tmp'
host = False
calibrate = False

def BuildOptions():
    parser = argparse.ArgumentParser(
        description = "Run java benchmarks.",
        # Print default values.
        formatter_class = argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument('--iterations', '-i', metavar = 'N', type = int,
                        default = default_n_iterations,
                        help = "Run <N> iterations of the benchmarks.")
    parser.add_argument('--auto-calibrate', action='store_true', default = True,
                        dest = 'calibrate', help='''Do not run the benchmarks'
                        `main()` function directly. Instead, calibrate to run
                        each benchmark for a certain amount of time.''')
    parser.add_argument('--host', action='store_true', default = False,
                        help='Run on host JVM')
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
    return parser.parse_args()



# Utils

def VerbosePrint(msg):
    if verbose: print(msg)

def ensure_dir(path):
    if not os.path.exists(path):
        os.makedirs(path)

def get_current_path_files_by_extension(ext, path):
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
    p = subprocess.Popen(command, cwd = dir_build_java_class,
                         stdout = subprocess.PIPE, stderr = subprocess.PIPE)
    return p.communicate()

def DeleteAppInDalvikCache(remote_copy_path):
    # We delete the entire dalvik-cache in the test path.
    # Delete any cached version of the benchmark app.
    # With the current defaults, the pattern is "data@local@tmp@java-benchs.apk*"
    adb_shell('rm -rf ' + os.path.join(remote_copy_path, 'dalvik-cache'))

def BuildBenchmarks(args):
    # Call the build script, with warnings treated as errors.
    command = ['./build.sh', '-w']
    if args.host:
        # Only build for the host.
        command += ['-H']
    VerbosePrint(' '.join(command))
    subprocess.check_call(command)

def run_adb(mode, apk, classname):
    dalvikvm = 'dalvikvm%s' % mode
    command = "cd %s && ANDROID_DATA=`pwd` DEX_LOCATION=`pwd` dalvikvm -cp %s" % (os.path.dirname(apk), apk)
    if args.calibrate:
        command += " %s %s" % (bench_runner_main, classname)
    else:
        command = " %s" % (classname)
    out, err = adb_shell(command)
    return out.decode('UTF-8')

def run_host(mode, apk, classname):
    if args.calibrate:
        command = ['java', bench_runner_main, classname]
    else:
        command = ['java', classname]
    out, err = host_java(command)
    return out.decode('UTF-8')


# TODO: Avoid using global variables.
result = dict()

def RunBench(apk, classname,
             run_helper = run_adb,
             iterations = default_n_iterations, mode = default_mode):
    for iteration in range(iterations):
        try:
            if args.host:
                out = run_host(mode, apk, classname)
            else:
                out = run_helper(mode, apk, classname)
            out = out.rstrip('\n')
            if verbose:
                print(out)
        except Exception as e:
            print(e)
            sys.stderr.write("  \-> FAILED, continuing anyway\n")
            continue

        for line in out.rstrip().split("\n"):
            name = line.split(":")[0].rstrip()
            score = float(line.split(":")[1].strip().split(" ")[0].strip())

            if name not in result:
                result[name] = list()

            result[name].append(score)


def RunBenchs(apk, bench_names,
              iterations = default_n_iterations, mode = default_mode):
    VerbosePrint('\n# Running benchmarks')
    for bench in bench_names:
        RunBench(apk, bench, iterations = iterations, mode = mode)


if __name__ == "__main__":
    args = BuildOptions()
    verbose = not args.noverbose

    BuildBenchmarks(args)

    remote_apk = None
    if not args.host:
        DeleteAppInDalvikCache(args.remote_copy_path)
        apk = './build/bench.apk'
        apk_name = os.path.basename(apk)
        adb_push(apk, args.remote_copy_path)
        remote_apk = os.path.join(args.remote_copy_path, apk_name)

    if args.norun:
        sys.exit(0)

    bench_files = get_current_path_files_by_extension('java', dir_benchmarks)
    bench_names = [os.path.basename(f).replace('.java', '') for f in bench_files]
    bench_names.sort()

    RunBenchs(remote_apk, bench_names, args.iterations, args.mode)
    utils.PrintStats(result, iterations = args.iterations)
    print('')
    # Write the results to a file so they can later be used with `compare.py`.
    res_filename = 'res.' + time.strftime("%Y.%m.%d-%H:%M:%S") + '.pkl'
    with open(res_filename, 'wb') as pickle_file:
        pickle.dump(result, pickle_file)
        print(('Wrote results to %s.' % res_filename))
