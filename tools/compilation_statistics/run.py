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
             work_dir,
             boot_oat_comp,
             boot_oat_file):
    apk_path = os.path.join(target_copy_path, apk)
    if boot_oat_comp:
        oat = "%s/boot.%s.oat" % (target_copy_path, isa)
        art = "%s/boot.%s.art" % (target_copy_path, isa)

        # Check if dump file exists.
        dump_oat_file_location = "/data/local/boot.oat.%s.txt" % (isa)
        dump_exists_command = "if [ -f %s ] ; then echo found; fi; exit 0" \
                              % (dump_oat_file_location)
        # Since we are interested in whether the dump file exists or not, we can't simply execute
        # [ -f file_name ] since newer versions of adb return the error code of the command that's
        # being executed. Therefore, if the file is found we output a string and at the end we
        # always return error code 0, so that we can get an error only if the failure was due
        # to adb not executing properly.
        rc, out = utils_adb.shell(dump_exists_command, target)
        # The command prints an extra new line as well.
        if out[:-1] == "found":
            print("Dump file exists, move on.")
        else:
            # Dump oat file the first time (for each arch).
            # Remove leading dot from filename.
            dump_command = "oatdump --oat-file=%s --output=%s" % (boot_oat_file, \
                                                                  dump_oat_file_location)
            print("Dumping oat file...")
            utils_adb.shell(dump_command, target)
        # Read dex2oat-host from dump file.
        dex2oat_host_command = 'grep "dex2oat-host" %s' % (dump_oat_file_location)
        rc, out = utils_adb.shell(dex2oat_host_command, target)
        if rc:
            utils.Error("Dump file doesn't contain dex2oat-host.")
        if out == 'dex2oat-host = x86-64\n':
            utils.Error("boot.oat was built on a x84-64 machine, which is most likely the" \
                        " host: %s \nWe want it to be built on the target instead. Have you" \
                        " configured the device with WITH_DEXPREOPT=false ?" % out)

        # Read command.
        dex2oat_cmdline_command = 'grep "dex2oat-cmdline" %s' % (dump_oat_file_location)
        rc, out = utils_adb.shell(dex2oat_cmdline_command, target)
        if rc:
            utils.Error("Dump file doesn't contain dex2oat-cmldine.")
        command = out
        # Replace destination: --oat-file, fix beginning of command.
        command = re.sub("--oat-file=(.+?) --", "--oat-file=%s --" % oat, command)
        command = re.sub("--image=(.+?) --", "--image=%s --" % art, command)
        command = re.sub("dex2oat-cmdline +=", "dex2oat", command)
        # Force 1 thread only - we want compilation times to be as stable as possible and we are
        # interested in single thread performance, not multi-thread (throughput).
        command = re.sub(" -j\d+ ", " -j1 ", command)
        # Remove newline at end.
        command = re.sub("\n$", "", command)
        command = '(echo $BASHPID && exec ' + command + ' ) | head -n1'
    else:
        oat = apk_path + '.oat'
        dex2oat_options = utils.GetDex2oatOptions(compiler_mode)
        # Only the output of the first command is necessary; execute in a subshell
        # to guarantee PID value; only one thread is used for compilation to reduce
        # measurement noise.
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
    memory_stats = OrderedDict()
    byte_size = True

    for m in re.findall(' (.*?)=([0-9]+)([GKM]?)B( \(([0-9]+)B\))?', out):
        # Old versions of dex2oat do not show the exact memory usage values in bytes, so
        # try to parse the output in the new format first, and if that fails, fall back
        # to the legacy one.
        if m[4]:
            value = int(m[4])
        else:
            value = int(m[1]) * memory_unit_prefixes[m[2]]

            if m[2]:
                byte_size = False

        memory_stats[m[0]] = [value]

    if not byte_size:
        utils.Warning('Memory usage values have been rounded down, so they might be '
                      'inaccurate.')

    if boot_oat_comp:
        local_oat = os.path.join(utils.dir_root, work_dir, "boot.%s.oat" % isa)
    else:
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


def GetCompilationStatisticsResults(args):
    utils.CheckDependencies(['adb', 'size'])
    res = OrderedDict()
    work_dir = tempfile.mkdtemp()
    apk_list = []

    boot_oat_files = []
    for pathname in args.pathnames:
        if os.path.isfile(pathname):
            apk_list.append(pathname)
        elif pathname == "boot.oat":
            isa_list = utils_adb.GetISAList(args.target)
            # Find .oat file on device (for each arch).
            find_command = 'find / -type d \( -name proc -o -name sys \) -prune -o ' \
                           '-name "*boot.oat" -print 2>/dev/null'
            rc, out = utils_adb.shell(find_command, args.target)
            boot_oat_files = out.split('\n')[:-1]

            if len(boot_oat_files) != len(isa_list):
                utils.Error("Number of architectures different from number of boot.oat files." \
                            " The list of boot.oat files is here:\n\n %s\n\nMake sure there are" \
                            " no stale boot.oat files in /data/local or some other directory." \
                            " Another possibility is that you didn't build your device with" \
                            " WITH_DEXPREOPT=false. Do a lunch and then WITH_DEX_PREOPT=false" \
                            " make -j$(nproc) ." % boot_oat_files)
            # Order both lists. Now, as long as both oat files have the same parent dir, order
            # should match.
            isa_list.sort()
            boot_oat_files.sort()

            for isa_l in isa_list:
                apk_list.append("boot.oat " + isa_l)
        else:
            dentries = [dentry for dentry in glob.glob(os.path.join(pathname, '*.apk'))
                        if os.path.isfile(dentry)]
            apk_list[len(apk_list):] = dentries
    # Oat file index.
    oat_file_ind = 0

    for apk in sorted(apk_list):
        if apk[:8] == "boot.oat":
            isa = apk[9:]
            # Remove leading dot and pass to function.
            boot_oat_files[oat_file_ind] = boot_oat_files[oat_file_ind][1:]
            res[apk] = GetStats(apk, args.target, isa,
                                args.compiler_mode, args.target_copy_path,
                                args.iterations, work_dir, True, boot_oat_files[oat_file_ind])
            oat_file_ind += 1
        else:
            isa = utils_adb.GetISA(args.target, args.mode)
            utils_adb.push(apk, args.target_copy_path, args.target)
            apk_name = os.path.basename(apk)
            res[apk_name] = GetStats(apk_name, args.target, isa,
                                     args.compiler_mode, args.target_copy_path,
                                     args.iterations, work_dir, False, None)

    shutil.rmtree(work_dir)
    return res

def GetAndPrintCompilationStatisticsResults(args):
    results = GetCompilationStatisticsResults(args)
    utils.PrintData(results)
    print('')
    return results

if __name__ == "__main__":
    # TODO: Mac OS support
    if os.uname().sysname != 'Linux':
        utils.Error('Running this script is supported only on Linux.')

    args = BuildOptions()
    stats = GetAndPrintCompilationStatisticsResults(args)

    utils.OutputObject(stats, 'pkl', args.output_pkl)
    utils.OutputObject(stats, 'json', args.output_json)
