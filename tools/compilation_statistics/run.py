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

from collections import OrderedDict

dir_compilation_statistics = os.path.dirname(os.path.realpath(__file__))
dir_tools = os.path.join(dir_compilation_statistics, '..')
sys.path.insert(0, dir_tools)

import utils
import utils_adb
import utils_stats

memory_unit_prefixes = {'' : 1, 'G' : 2 ** 30, 'K' : 2 ** 10, 'M' : 2 ** 20}
sections = set(['.bss', '.rodata', '.text', 'Total'])

def BuildOptions():
    parser = argparse.ArgumentParser(
        description = '''Collect statistics about the APK compilation process on a target
                         adb device: Compilation time, memory usage by the compiler
                         (arena, Java, and native allocations, and free native memory),
                         and size of the generated executable (total, .bss, .rodata, and
                         .text section sizes).''',
        # Print default values.
        formatter_class = argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument('pathnames',
                        nargs = '+',
                        help='''Path containing APK files or a file name for which
                                compilation statistics should be collected.''')
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

def GetStats(apk,
             target,
             isa,
             compiler_mode,
             target_copy_path,
             iterations,
             work_dir):
    apk_path = os.path.join(target_copy_path, apk)
    oat = apk_path + '.oat'
    # Only the output of the first command is necessary; execute in a subshell
    # to guarantee PID value; only one thread is used for compilation to reduce
    # measurement noise.
    dex2oat_options = utils.GetDex2oatOptions(compiler_mode)
    command = '(echo $BASHPID && exec dex2oat -j1 ' + \
        ' '.join(dex2oat_options) + \
        ' --dex-file=' + apk_path + ' --oat-file=' + oat
    command += ' --instruction-set=' + isa + ') | head -n1'
    compilation_times = []

    for i in range(iterations):
        rc, out = utils_adb.shell(command, target)
        # To simplify parsing, assume that PID values are rarely recycled by the system.
        stats_command = 'logcat -dsv process dex2oat | grep "^I([[:space:]]*' + \
                        out.rstrip() + ').*took" | tail -n1'
        rc, out = utils_adb.shell(stats_command, target)
        compile_time = re.match('.*?took (?P<value>.*?)(?P<unit>[mnu]{,1})s.*?\)', out)

        if not compile_time:
            utils.Error('dex2oat failed; check adb logcat.')

        value = float(compile_time.group('value')) * \
                utils.si_unit_prefixes[compile_time.group('unit')]
        compilation_times.append(value)

    # The rest of the statistics are deterministic, so there is no need to run several
    # iterations; just get the values from the last run.
    out = out[compile_time.end():]
    memory_stats = OrderedDict((m[0], [int(m[1]) * memory_unit_prefixes[m[2]]]) for m
                               in re.findall(' (.*?)=([0-9]+)([GKM]{,1})B', out))
    local_oat = os.path.join(utils.dir_root, work_dir, apk + '.oat')
    utils_adb.pull(oat, local_oat, target)
    command = ['size', '-A', '-d', local_oat]
    rc, outerr = utils.Command(command)
    section_sizes = OrderedDict((s[0], [int(s[1])]) for s
                                in re.findall('(\S+)\s+([0-9]+).*', outerr)
                                if s[0] in sections)
    return OrderedDict([(utils.compilation_times_label, compilation_times),
                       (utils.memory_stats_label, memory_stats),
                       (utils.oat_size_label, section_sizes)])

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
    utils.CheckDependencies(['adb', 'size'])
    isa = GetISA(args.target, args.mode)
    res = OrderedDict()
    work_dir = tempfile.mkdtemp()
    apk_list = []

    for pathname in args.pathnames:
        if os.path.isfile(pathname):
            apk_list.append(pathname)
        else:
            dentries = [dentry for dentry in glob.glob(os.path.join(pathname, '*.apk'))
                        if os.path.isfile(dentry)]
            apk_list[len(apk_list):] = dentries

    for apk in sorted(apk_list):
        utils_adb.push(apk, args.target_copy_path, args.target)
        apk_name = os.path.basename(apk)
        res[apk_name] = GetStats(apk_name, args.target, isa,
                                 args.compiler_mode, args.target_copy_path,
                                 args.iterations, work_dir)

    shutil.rmtree(work_dir)
    return res

if __name__ == "__main__":
    # TODO: Mac OS support
    if os.uname().sysname != 'Linux':
        utils.Error('Running this script is supported only on Linux.')

    args = BuildOptions()
    stats = GetCompilationStats(args)
    utils.PrintResult(stats)
    utils.OutputObject(stats, 'pkl', args.output_pkl)
    utils.OutputObject(stats, 'json', args.output_json)
