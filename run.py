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
import os
import pickle
import shutil
import subprocess
import sys
import time

from collections import OrderedDict

dir_root = os.path.dirname(os.path.realpath(__file__))
dir_tools = os.path.join(dir_root, 'tools')
sys.path.insert(0, dir_tools)
import utils
import utils_stats

bench_runner_main = 'org.linaro.bench.RunBench'

# The script must be executed from an Android environment, which will be passed
# to run the commands.
environment = os.environ.copy()

# Use a global `verbose` flag to allow scripts importing this file to override
# it.
verbose = False



# Options

default_mode = ''
default_n_iterations = 1
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
    parser.add_argument('--target', '-t', action='store', nargs='?', default=None, const='<default>',
                        dest='target', help='Run on target adb device.')
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
                             Defaults to \'benchmarks/deprecated/*\' if no other filter is
                             specified.''')
    parser.add_argument('--output-pkl', action = 'store',
                        help='Specify a name for the output `.pkl` file.')
    return parser.parse_args()



# Utils

def VerbosePrint(msg):
    if verbose: print(msg)

def ensure_dir(path):
    if not os.path.exists(path):
        os.makedirs(path)

# ADB helpers

def adb_push(f, target_path = default_remote_copy_path, target = None):
    command = ['adb', 'push', f, target_path]
    if target != '<default>':
        command = ['adb', '-s', target, 'push', f, target_path]
    VerbosePrint(' '.join(command))
    p = subprocess.Popen(command, env = environment,
                         stdout = subprocess.PIPE, stderr = subprocess.PIPE)
    return p.communicate()


# `command` is expected to be a string, not a list.
def adb_shell(command_arg, target):
    # We need to quote the actual command in the text printed so it can be
    # copy-pasted and executed.
    command = ['adb', 'shell', command_arg]
    if target != '<default>':
        command = ['adb', '-s', target, 'shell', command_arg]
    VerbosePrint(' '.join(command))
    p = subprocess.Popen(command, env = environment,
                         stdout = subprocess.PIPE, stderr = subprocess.PIPE)
    rc = p.wait()
    out, err = p.communicate()
    return rc, out, err

def host_java(command):
    VerbosePrint(' '.join(command))
    p = subprocess.Popen(command, cwd = utils.dir_build_java_classes,
                         stdout = subprocess.PIPE, stderr = subprocess.PIPE)
    rc = p.wait()
    out, err = p.communicate()
    return rc, out, err

def DeleteAppInDalvikCache(remote_copy_path, target):
    # We delete the entire dalvik-cache in the test path.
    # Delete any cached version of the benchmark app.
    # With the current defaults, the pattern is "data@local@tmp@java-benchs.apk*"
    adb_shell('rm -rf ' + os.path.join(remote_copy_path, 'dalvik-cache'), target)

def BuildBenchmarks(build_for_target):
    # Call the build script, with warnings treated as errors.
    command = [os.path.join(utils.dir_root, 'build.sh'), '-w']
    if build_for_target:
        command += ['-t']
    VerbosePrint(' '.join(command))
    subprocess.check_call(command)

def RunBenchADB(mode, auto_calibrate, apk, classname, target):
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
    rc, out, err = adb_shell(command, target)
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
            elif verbose:
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
    VerbosePrint('\n# Running benchmarks: ' + ' '.join(bench_names))
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
    verbose = not args.noverbose

    BuildBenchmarks(args.target)

    remote_apk = None
    if args.target:
        DeleteAppInDalvikCache(args.remote_copy_path, args.target)
        apk = os.path.join(utils.dir_root, 'build/bench.apk')
        apk_name = os.path.basename(apk)
        adb_push(apk, args.remote_copy_path, args.target)
        remote_apk = os.path.join(args.remote_copy_path, apk_name)

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
                   args.auto_calibrate,
                   args.iterations,
                   args.mode)
    result = OrderedDict(sorted(result.items()))
    utils_stats.PrintStats(result, iterations = args.iterations)
    print('')
    # Write the results to a file so they can later be used with `compare.py`.
    if args.output_pkl is None:
        default_pkl_out_dir = os.path.join(utils.dir_root, 'pkl')
        utils.ensure_dir(default_pkl_out_dir)
        res_file = 'res.' + time.strftime("%Y.%m.%d-%H:%M:%S") + '.pkl'
        res_file = os.path.join(default_pkl_out_dir, res_file)
    else:
        res_file = args.output_pkl
    with open(res_file, 'wb') as pickle_file:
        # We create a python2-compatible pickle dump.
        pickle.dump(result, pickle_file, 2)
        print(('Wrote results to %s.' % res_file))

    if rc != 0:
        print("ERROR: The benchmarks did *not* run successfully. (rc = %d)" % rc)
    sys.exit(rc)
