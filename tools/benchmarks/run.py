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
    parser.add_argument('--dont-auto-calibrate',
                        action='store_true', default = False,
                        dest = 'no_auto_calibrate',
                        help='''Do not auto-calibrate the benchmarks. Instead,
                        run each benchmark's `main()` function directly.''')
    parser.add_argument('-n', '--norun', action='store_true',
                        help='''Build and configure everything, but do not run
                        the benchmarks.''')
    parser.add_argument('-f', '--filter', action = 'append',
                        help='''Quoted (benchmark name) filter pattern. If no
                        filters match, filtering will be attempted with all the
                        patterns prefixed and suffixed with `*`.''')
    parser.add_argument('-F', '--filter-out', action = 'append',
                        help='''Filter out the benchmarks matching this pattern.
                        Defaults to \'benchmarks/deprecated/*\' if no other
                        filter is specified.''')

    args = parser.parse_args()

    utils.ValidateCommonRunOptions(args)

    return args

def host_java(command):
    return utils.Command(command, cwd=utils.dir_build_java_classes)

def DeleteAppInDalvikCache(target_copy_path, target):
    # We delete the entire dalvik-cache in the test path.
    # Delete any cached version of the benchmark app.
    # With the current defaults, the pattern is "data@local@tmp@java-benchs.apk*"
    utils_adb.shell('rm -rf ' + os.path.join(target_copy_path, 'dalvik-cache'), target)

def BuildBenchmarks(build_for_target):
    # Call the build script.
    command = [os.path.join(utils.dir_root, 'build.sh')]
    if build_for_target:
        command += ['-t']
    utils.Command(command)

def RunBenchADB(mode, compiler_mode, android_root, auto_calibrate, apk,
                 classname, target):
    format_data={'workdir': os.path.dirname(apk), 'rootpath': android_root,
                'mode': {"":"", "32":"", "64":"64"}[mode] }
    # Escaping through `adb shell` is fiddly, so we expand the path fully in
    # the environment configuration.
    environment_config = 'ANDROID_DATA={workdir} DEX_LOCATION={workdir}'
    dalvikvm = 'dalvikvm%s' % mode
    dalvikvm_options = ''
    apk_arguments = ''

    if android_root:
        # Add additional options.
        environment_config = "ANDROID_ROOT={rootpath} " + environment_config
        environment_config += " LD_LIBRARY_PATH={rootpath}/lib{mode}"
        dalvikvm = android_root + '/bin/' + dalvikvm
    if auto_calibrate:
        # Run the benchmark's time* method(s) via bench_runner_main
        apk_arguments += " %s %s" % (bench_runner_main, classname)
    else:
        # Run the benchmark as a main class directly
        apk_arguments += " %s" % (classname)

    if utils_print.verbose:
        apk_arguments += " --debug"

    dex2oat_options = utils.GetDex2oatOptions(compiler_mode)
    for opt in dex2oat_options:
        dalvikvm_options += ' -Xcompiler-option %s' % opt
        # We want the compiler options to be used both for the APK and the
        # boot-image.
        dalvikvm_options += ' -Ximage-compiler-option %s' % opt
    if compiler_mode == 'jit':
        dalvikvm_options += ' -Xusejit:true'

    command = 'cd {workdir} && ' + \
        ' '.join([environment_config, dalvikvm,
                  dalvikvm_options, '-cp', apk, apk_arguments])
    command = command.format(**format_data)

    return utils_adb.shell(command, target, exit_on_error=False)

def RunBenchHost(ignored_mode,
                 ignored_compiler_mode,
                 ignored_android_root,
                 auto_calibrate,
                 ignored_apk,
                 classname,
                 ignored_target):
    if auto_calibrate:
        command = ['java', bench_runner_main, classname]
    else:
        command = ['java', classname]
    return host_java(command)


# TODO: Avoid using global variables.
result = dict()

def RunBench(apk, classname,
             run_helper,
             auto_calibrate,
             iterations = utils.default_n_iterations,
             mode = utils.default_mode,
             compiler_mode = utils.default_compiler_mode,
             android_root = utils.default_android_root,
             target = None):
    rc = 0
    for iteration in range(iterations):
        try:
            local_rc, outerr = run_helper(mode,
                                          compiler_mode,
                                          android_root,
                                          auto_calibrate,
                                          apk,
                                          classname,
                                          target)
            rc += local_rc
            outerr = outerr.rstrip('\n')
            utils_print.VerbosePrint(outerr)
        except Exception as e:
            utils.Warning(str(e) + "\n  \-> FAILED, continuing anyway\n", e)
            rc += 1
            continue

        try:
            for line in outerr.rstrip().split("\n"):
                name = line.split(":")[0].rstrip()
                # Ignore any java logging from --debug
                if name not in ['INFO', 'DEBUG', 'ERROR']:
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
              android_root=utils.default_android_root):
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
                       target = target)
    return rc


def ListAllBenchmarks():
    rc, out = utils.Command(
        ['java', 'org.linaro.bench.RunBench', '--list_benchmarks'],
        cwd=utils.dir_build_java_classes)
    out = out.rstrip()
    benchs = out.split('\n')
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
    benchmarks = utils.FilterList(benchmarks, args.filter, filter_out)

    rc = RunBenchs(remote_apk,
                   benchmarks,
                   args.target,
                   not args.no_auto_calibrate,
                   args.iterations,
                   args.mode,
                   args.compiler_mode,
                   args.android_root)

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
