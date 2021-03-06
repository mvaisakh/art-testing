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

import argparse
import csv
import os
import subprocess
import sys
import time

from collections import OrderedDict

dir_benchs = os.path.dirname(os.path.realpath(__file__))
dir_tools = os.path.join(dir_benchs, '..')
sys.path.insert(0, dir_tools)
import utils
import utils_adb
import utils_print
import utils_stats

bench_runner_main = 'org.linaro.bench.RunBench'

# Options

def BuildOptions():
    parser = argparse.ArgumentParser(
        description = "Run java benchmarks.",
        # Print default values.
        formatter_class = argparse.ArgumentDefaultsHelpFormatter)
    utils.AddCommonRunOptions(parser)
    utils.AddOutputFormatOptions(parser, utils.default_output_formats + ['csv'])
    utils.AddRunFilterOptions(parser)
    parser.add_argument('--dont-auto-calibrate',
                        action='store_true', default = False,
                        dest = 'no_auto_calibrate',
                        help='''Do not auto-calibrate the benchmarks. Instead,
                        run each benchmark's `main()` function directly.''')
    parser.add_argument('-n', '--norun', action='store_true',
                        help='''Build and configure everything, but do not run
                        the benchmarks.''')


    args = parser.parse_args()

    utils.ValidateCommonRunOptions(args)

    return args

def host_java(command, command_string=None):
    return utils.Command(command, command_string, cwd=utils.dir_build_java_classes)

def DeleteAppInDalvikCache(target_copy_path, target):
    # We delete the entire dalvik-cache in the test path.
    # Delete any cached version of the benchmark app.
    # The oat files location can be:
    #     - in dalvik-cache: referred as the OAT location.
    #     - in dex_parent_dir/oat/ISA/: referred as the ODEX location.
    utils_adb.shell('rm -rf ' + utils.TargetPathJoin(target_copy_path, 'dalvik-cache'), target)
    utils_adb.shell('rm -rf ' + utils.TargetPathJoin(target_copy_path, 'oat'), target)

def BuildBenchmarks(build_for_target):
    # Call the build script.
    command = [os.path.join(utils.dir_root, 'build.sh')]
    if build_for_target:
        command += ['-t']
    utils.Command(command)

def RunBenchADB(mode, compiler_mode, android_root, auto_calibrate, apk, classname, target, cpuset):
    apk_arguments = ''
    if auto_calibrate:
        # Run the benchmark's time* method(s) via bench_runner_main
        apk_arguments += " %s %s" % (bench_runner_main, classname)
    else:
        # Run the benchmark as a main class directly
        apk_arguments += " %s" % (classname)

    command = ''
    # TODO: The command to run benchmarks depends on an approach used by
    # art/tools. ART_COMMAND is a temporary workaround to allow to use a command
    # constructed by someone knowing how to run applications with dalvikvm.
    if 'ART_COMMAND' in os.environ:
        command = os.getenv('ART_COMMAND')
    else:
        utils.Error("ART_COMMAND is not set.")

    command += ' '.join([apk_arguments])
    format_data = {'workdir': os.path.dirname(apk)}
    command = command.format(**format_data)

    return utils_adb.shell(command, target, exit_on_error=False)

def RunBenchHost(ignored_mode,
                 ignored_compiler_mode,
                 ignored_android_root,
                 auto_calibrate,
                 ignored_apk,
                 classname,
                 ignored_target,
                 cpuset):
    command_string = None

    if auto_calibrate:
        command = ['java', bench_runner_main, classname]
    else:
        command = ['java', classname]

    if cpuset:
      command_string = 'echo $BASHPID > /dev/cpuset/' + cpuset + '/tasks && '
      command_last = command_string + 'exec ' + ' '.join(command)
      command_string += ' '.join(command)
      command = ['bash', '-c', command_last]

    return host_java(command, command_string)


# TODO: Avoid using global variables.
result = dict()

def RunBench(apk, classname,
             run_helper,
             auto_calibrate,
             iterations = utils.default_n_iterations,
             mode = utils.default_mode,
             compiler_mode = utils.default_compiler_mode,
             android_root = utils.default_android_root,
             target = None,
             cpuset = None):
    rc = 0
    for iteration in range(iterations):
        try:
            local_rc, outerr = run_helper(mode,
                                          compiler_mode,
                                          android_root,
                                          auto_calibrate,
                                          apk,
                                          classname,
                                          target,
                                          cpuset)
            rc += local_rc
            outerr = outerr.rstrip('\r\n')
            utils_print.VerbosePrint(outerr)
        except Exception as e:
            utils.Warning(str(e) + "\n  \-> FAILED, continuing anyway\n", e)
            rc += 1
            continue

        try:
            for line in outerr.rstrip().splitlines():
                if not line.startswith('benchmarks/'):
                    continue
                name = line.split(":")[0].rstrip()
                score = float(line.split(":")[1].strip().split(" ")[0].strip())
                if name not in result:
                    result[name] = list()
                result[name].append(score)
        except Exception as e:
            utils.Warning(str(e) + "\n  \-> Error parsing output from %s", e)
            rc += 1
            break

    return rc



def RunBenchs(apk, bench_names,
              target,
              auto_calibrate,
              iterations=utils.default_n_iterations,
              mode=utils.default_mode,
              compiler_mode=utils.default_compiler_mode,
              android_root=utils.default_android_root,
              cpuset=None):
    rc = 0
    utils_print.VerbosePrint('\n# Running benchmarks: ' + ' '.join(bench_names))
    run_helper = RunBenchADB if target else RunBenchHost
    for bench in bench_names:
        rc += RunBench(apk,
                       bench,
                       run_helper,
                       auto_calibrate,
                       iterations = iterations,
                       mode = mode,
                       compiler_mode = compiler_mode,
                       android_root = android_root,
                       target = target,
                       cpuset = cpuset)
    return rc


def ListAllBenchmarks():
    rc, out = utils.Command(
        ['java', 'org.linaro.bench.RunBench', '--list_benchmarks'],
        cwd=utils.dir_build_java_classes)
    out = out.rstrip()
    benchs = out.splitlines()
    return benchs


def GetBenchmarkResults(args):
    if getattr(args, 'filter', []) == []:
        setattr(args, 'filter', None)

    if getattr(args, 'filter_out', []) == []:
        setattr(args, 'filter_out', None)

    if getattr(args, 'no_auto_calibrate', None) is None:
        setattr(args, 'no_auto_calibrate', False)

    if getattr(args, 'norun', None) is None:
        setattr(args, 'norun', False)

    if args.target:
        utils.CheckDependencies(['adb'])

    BuildBenchmarks(args.target)

    remote_apk = None
    if args.target:
        DeleteAppInDalvikCache(args.target_copy_path, args.target)
        apk = os.path.join(utils.dir_build, 'bench.apk')
        apk_name = os.path.basename(apk)
        utils_adb.push(apk, args.target_copy_path, args.target)
        resources_tar = os.path.join(utils.dir_build, 'resources.tar')
        if os.path.isfile(resources_tar):
            utils_adb.push(resources_tar, args.target_copy_path, args.target)
            utils_adb.shell('tar xfv ' +
                    utils.TargetPathJoin(args.target_copy_path, 'resources.tar') +
                    ' -C ' + args.target_copy_path,
                    args.target)
        remote_apk = utils.TargetPathJoin(args.target_copy_path, apk_name)

    if args.norun:
        sys.exit(0)

    benchmarks = ListAllBenchmarks()

    benchmarks = utils.FilterList(benchmarks, args.filter, args.filter_out)

    rc = RunBenchs(remote_apk,
                   benchmarks,
                   args.target,
                   not args.no_auto_calibrate,
                   args.iterations,
                   args.mode,
                   args.compiler_mode,
                   args.android_root,
                   args.cpuset)

    if rc:
        utils.Error("The benchmarks did *not* run successfully. (rc = %d)" % rc, rc)

    res = OrderedDict(sorted(result.items()))
    return res

def GetAndPrintBenchmarkResults(args):
    results = GetBenchmarkResults(args)
    utils.PrintData(results)
    unflattened_results = utils.Unflatten(results)
    utils_stats.ComputeAndPrintGeomeanWithRelativeDiff(unflattened_results)
    print('')
    return results

if __name__ == "__main__":
    args = BuildOptions()
    result = GetAndPrintBenchmarkResults(args)

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
