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
import os
import pickle
import sys

dir_compilation_statistics = os.path.dirname(os.path.realpath(__file__))
dir_tools = os.path.join(dir_compilation_statistics, '..')
sys.path.insert(0, dir_tools)

import utils
import utils_stats
from run import CompileStats, MemoryUsage, OATSize, memory_stats_fields, time_stats_fields

def BuildOptions():
    parser = argparse.ArgumentParser(
        description = "Compare two results of the associated `run.py` script.",
        # Print default values.
        formatter_class = argparse.ArgumentDefaultsHelpFormatter)
    utils.AddCommonCompareOptions(parser)
    return parser.parse_args()

def GetMemoryEntries(stats_1, stats_2):
    res = [['Memory usage', '', '', '']]

    for i in range(len(memory_stats_fields)):
        if stats_1.memory_usage[i].si_prefix == stats_2.memory_usage[i].si_prefix:
            res.append(['  ' + memory_stats_fields[i] + ' (' + stats_1.memory_usage[i].si_prefix + 'B)',
                        '%d' % stats_1.memory_usage[i].value, '%d' % stats_2.memory_usage[i].value,
                        '%.3f' % utils_stats.GetRelativeDiff(stats_1.memory_usage[i].value,
                                                             stats_2.memory_usage[i].value)])

    return res

def GetSizeEntries(stats_1, stats_2):
    res = [['OAT size', '', '', ''], ['  ' + 'Total (B)',
                                      '%d' % stats_1.oat_size.total_size,
                                      '%d' % stats_2.oat_size.total_size,
                                      '%.3f' % utils_stats.GetRelativeDiff(stats_1.oat_size.total_size,
                                                                           stats_2.oat_size.total_size)]]

    for s in sorted(set(stats_1.oat_size.section_sizes.keys()) | set(stats_2.oat_size.section_sizes.keys())):
        value_1 = stats_1.oat_size.section_sizes[s] if s in stats_1.oat_size.section_sizes else 0
        value_2 = stats_2.oat_size.section_sizes[s] if s in stats_2.oat_size.section_sizes else 0
        res.append(['  ' * 2 + s, '%d' % value_1, '%d' % value_2,
                    '%.3f' % utils_stats.GetRelativeDiff(value_1, value_2)])

    return res

def GetTimeEntries(stats_1, stats_2):
    res = [['Compile time', '', '', '']]
    time_stats_1 = list(utils_stats.ComputeStats(stats_1.compile_times))
    time_stats_2 = list(utils_stats.ComputeStats(stats_2.compile_times))
    si_factor, si_prefix = utils.PrettySIFactor(time_stats_1[0])

    for i in range(len(time_stats_1)):
        field = '  ' + time_stats_fields[i]
        value_1 = time_stats_1[i]
        value_2 = time_stats_2[i]
        diff = '%.3f' % utils_stats.GetRelativeDiff(value_1, value_2)

        if i < len(time_stats_1) - 1:
            field += ' (' + si_prefix + 's)'
            value_1 = value_1 / si_factor
            value_2 = value_2 / si_factor

        res.append([field, '%.3f' % value_1, '%.3f' % value_2, diff])

    return res

def PrintStatsDiff(apk, stats_1, stats_2):
    utils_stats.PrintTable([apk, 'Value 1', 'Value 2', '(value 2 - value 1) / value 1 * 100'],
                           ['s', 's', 's', 's'],
                           GetTimeEntries(stats_1, stats_2) + GetMemoryEntries(stats_1, stats_2) + \
                           GetSizeEntries(stats_1, stats_2))

if __name__ == "__main__":
    args = BuildOptions()
    pkl_file_1 = open(args.res_1, 'rb')
    pkl_file_2 = open(args.res_2, 'rb')
    res_1 = pickle.load(pkl_file_1)
    res_2 = pickle.load(pkl_file_2)
    pkl_file_1.close()
    pkl_file_2.close()
    apk_list = [apk for apk in sorted(res_1.keys()) if apk in res_2]

    for apk in apk_list:
        if apk != apk_list[0]:
            print('')

        PrintStatsDiff(apk, res_1[apk], res_2[apk])
