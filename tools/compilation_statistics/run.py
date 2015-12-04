#! /usr/bin/env python3

# Copyright (C) 2015 Linaro Limited. All rights received.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import argparse
import collections
import glob
import json
import os
import pickle
import re
import shutil
import subprocess
import sys
import tempfile
import time

dir_compilation_statistics = os.path.dirname(os.path.realpath(__file__))
dir_tools = os.path.join(dir_compilation_statistics, '..')
sys.path.insert(0, dir_tools)

import utils
import utils_adb
import utils_stats

CompileStats = collections.namedtuple('CompileStats', ['compile_times', 'memory_usage', 'oat_size'])
MemoryUsage = collections.namedtuple('MemoryUsage', ['value', 'si_prefix'])
OATSize = collections.namedtuple('OATSize', ['total_size', 'section_sizes'])
memory_stats_fields = ['Arena', 'Java', 'Native', 'Free native']
sections = set(['.bss', '.rodata', '.text'])
time_stats_fields = ['Min', 'Max', 'Mean', 'Stdev', 'Stdev (% of mean)']

def BuildOptions():
    parser = argparse.ArgumentParser(
        description = '''Collect statistics about the APK compilation process on a target adb
                        device: Compilation time, memory usage by the compiler (arena, Java,
                        and native allocations, and free native memory), and size of the
                        generated executable (total, .bss, .rodata, and .text section sizes).''',
        # Print default values.
        formatter_class = argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument('pathnames', nargs = '+', help='''Path containing APK files or a file
                        name for which compilation statistics should be collected.''')
    utils.AddCommonRunOptions(parser)
    utils.AddOutputFormatOptions(parser, utils.default_output_formats)

    # TODO: Support running on host?
    # For now override the default value for the `--target`.
    parser.set_defaults(target=utils.adb_default_target_string)

    args = parser.parse_args()

    # This cannot fire for now since this script always runs on target, but
    # eventually we may want to run on host as well.
    utils.ValidateCommonRunOptions(args)

    return args

def GetMemoryEntries(stats):
    res = [['Memory usage', '']]

    for i in range(len(memory_stats_fields)):
        res.append(['  ' + memory_stats_fields[i] + ' (' + stats.memory_usage[i].si_prefix + 'B)',
                    '%d' % stats.memory_usage[i].value])

    return res

def GetSizeEntries(stats):
    res = [['OAT size', ''], ['  ' + 'Total (B)', '%d' % stats.oat_size.total_size]]

    for s in sorted(stats.oat_size.section_sizes.keys()):
        res.append(['  ' * 2 + s, '%d' % stats.oat_size.section_sizes[s]])

    return res

def GetTimeEntries(stats):
    res = [['Compile time', '']]
    time_stats = list(utils_stats.ComputeStats(stats.compile_times))
    si_factor, si_prefix = utils.PrettySIFactor(time_stats[0])

    for i in range(len(time_stats)):
        field = '  ' + time_stats_fields[i]
        value = time_stats[i]

        if i < len(time_stats_fields) - 1:
            field += ' (' + si_prefix + 's)'
            value = value / si_factor

        res.append([field, '%.3f' % value])

    return res

def PrintStatsTable(apk, stats):
    utils_stats.PrintTable([apk, ''], ['s', 's'], GetTimeEntries(stats) + GetMemoryEntries(stats) + \
                                                  GetSizeEntries(stats))

def GetStats(apk,
             target,
             isa,
             compiler_mode,
             target_copy_path,
             iterations,
             alloc_parser,
             size_parser,
             work_dir):
    apk_path = os.path.join(target_copy_path, apk)
    oat = apk_path + '.oat'
    # Only the output of the first command is necessary; execute in a subshell to guarantee PID value;
    # only one thread is used for compilation to reduce measurement noise.
    dex2oat_options = utils.GetDex2oatOptions(compiler_mode)
    command = '(echo $BASHPID && exec dex2oat -j1 ' + \
        ' '.join(dex2oat_options) + \
        ' --dex-file=' + apk_path + ' --oat-file=' + oat
    command += ' --instruction-set=' + isa + ') | head -n1'
    compile_times = []

    for i in range(iterations):
        rc, out = utils_adb.shell(command, target)
        # To simplify parsing, assume that PID values are rarely recycled by the system.
        stats_command = 'logcat -dsv process dex2oat | grep "^I([[:space:]]*' + \
                        out.rstrip() + ').*took" | tail -n1'
        rc, out = utils_adb.shell(stats_command, target)
        alloc_stats = alloc_parser.match(out)

        if not alloc_stats:
            utils.Error('dex2oat failed; check adb logcat.')

        compile_times.append(utils.GetTimeValue(float(alloc_stats.group(1)), alloc_stats.group(2)))

    # The rest of the statistics are deterministic, so there is no need to run several iterations;
    # just get the values from the last run.
    memory_usage = []

    for i in range(4):
        memory_usage.append(MemoryUsage(int(alloc_stats.group(2 * i + 3)), alloc_stats.group(2 * i + 4)))

    local_oat = os.path.join(utils.dir_root, work_dir, apk + '.oat')
    utils_adb.pull(oat, local_oat)
    command = ['size', '-A', '-d', local_oat]
    rc, outerr = utils.Command(command)
    section_sizes = dict()
    total_size = 0

    for s in size_parser.findall(outerr):
        value = int(s[1])

        if s[0] in sections:
            section_sizes[s[0]] = value
        elif s[0] == 'Total':
            total_size = value

    return [(apk, CompileStats(compile_times, memory_usage, OATSize(total_size, section_sizes)))]

def GetISA(target, mode):
    if not mode:
        # To be consistent with RunBenchADB(), the default mode depends on the executable
        # /system/bin/dalvikvm points to.
        command = 'readlink /system/bin/dalvikvm'
        rc, out = utils_adb.shell(command, target)

        # The default (e.g. if /system/bin/dalvikvm is not a soft link) is 64-bit.
        if '32' in out:
            mode = '32'
        else:
            mode = '64'

    # The 32-bit ISA name should be a substring of the 64-bit one.
    command = 'getprop | grep "dalvik\.vm\.isa\..*\.variant" | cut -d "." -f4 | sort'
    rc, out = utils_adb.shell(command, target)
    isa_list = out.split()

    if mode == '64':
        # 64-bit ISA names should contain '64'.
        isa = next((i for i in isa_list if mode in i), None)

        if not isa:
            utils.Error('The target adb device does not support 64-bit mode.')
    else:
        # The 32-bit ISA name comes first.
        isa = isa_list[0]

    return isa

def GetCompilationStats(args):
    alloc_parser = re.compile('.*?took (.*?)([mnu]{,1})s.*?=([0-9]+)([GKM]{,1})B' \
                              '.*?=([0-9]+)([GKM]{,1})B.*?=([0-9]+)([GKM]{,1})B' \
                              '.*?=([0-9]+)([GKM]{,1})B')
    size_parser = re.compile('(\S+)\s+([0-9]+).*')
    isa = GetISA(args.target, args.mode)
    res = dict()
    work_dir = tempfile.mkdtemp()
    apk_list = []

    for pathname in args.pathnames:
        if os.path.isfile(pathname):
            apk_list.append(pathname)
        else:
            apk_list[len(apk_list):] = [dentry for dentry in glob.glob(os.path.join(pathname, '*.apk'))
                                        if os.path.isfile(dentry)]

    for apk in apk_list:
        utils_adb.push(apk, args.target_copy_path, args.target)
        res.update(GetStats(os.path.basename(apk), args.target, isa,
                            args.compiler_mode, args.target_copy_path,
                            args.iterations, alloc_parser, size_parser, work_dir))

    shutil.rmtree(work_dir)
    apk_list = sorted(res)

    for apk in apk_list:
        PrintStatsTable(apk, res[apk])
        print('')

    return res

if __name__ == "__main__":
    # TODO: Mac OS support
    if os.uname().sysname != 'Linux':
        utils.Error('Running this script is supported only on Linux.')

    args = BuildOptions()
    stats = GetCompilationStats(args)
    utils.OutputObject(stats, 'pkl', args.output_pkl)
    utils.OutputObject(stats, 'json', args.output_json)
