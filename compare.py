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
import json

from collections import OrderedDict

from tools import utils
from tools import utils_print
from tools import utils_stats

def BuildOptions():
    parser = argparse.ArgumentParser(
        description = "Compare two result sets.",
        # Print default values.
        formatter_class = argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument('res_1', metavar = 'res_1.json')
    parser.add_argument('res_2', metavar = 'res_2.json')
    utils.AddFilterOptions(parser)
    parser.add_argument('--print-extended', '-e',
                        action='count', default=0,
                        help='''A cumulative option to print more information.
                        A first occurrence prints `mean` data. A second prints
                        raw data on top of relative information''')
    parser.add_argument('--significant-changes', '-s',
                        action = 'store_true', default = False,
                        help = '''Only show statistically significant changes
                        between the two sets of results. The tests used are the
                        Wilcoxon signed test and Student's t-test.''')
    parser.add_argument('--order-by-diff', '-o',
                        action = 'store_true', default = False,
                        help = '''Order results according to the median
                        difference.''')
    parser.add_argument('--wilcoxon-p-threshold', '--wilcp',
                        type = float, default = 0.05,
                        help = '''Minimum p-value allowed for the Wilcoxon test.
                        All results with a higher p-value than specified are
                        discarded. The default is 0.05, corresponding to 95%%
                        certainty of rejecting the null hypothesis.''')
    parser.add_argument('--ttest-p-threshold', '--ttp',
                        type = float, default = 0.05,
                        help = '''Minimum p-value allowed for the Student's
                        t-test.  All results with a higher p-value than
                        specified are discarded. The default is 0.05,
                        corresponding to 95%% certainty of rejecting the null
                        hypothesis.''')
    parser.add_argument('--output-for-linaro-automation',
                        action='store_true', default=False,
                        help='Print results formatted for Linaro automation.')
    return parser.parse_args()

# Filter out benchmarks that do not show any significant difference between the
# two set of results.
def FilterSignificantChanges(in_1, in_2, wilcoxon_p_threshold, ttest_p_threshold):
    out_1 = OrderedDict()
    out_2 = OrderedDict()
    benchmarks = [x for x in in_1 if x in in_2]
    for bench in benchmarks:
        wilcoxon_p, ttest_p = utils_stats.ComputeStatsTests(in_1[bench], in_2[bench])
        if wilcoxon_p < wilcoxon_p_threshold and ttest_p < ttest_p_threshold:
            out_1[bench] = in_1[bench]
            out_2[bench] = in_2[bench]
    return out_1, out_2

def OrderByDiff(entries):
    return sorted(entries, key = lambda values : values[3])

print_extended_mean_data = 1
print_extended_raw_data = 2

def PrintDiff(data_1, data_2,
              key=None,
              indentation='',
              print_extended=0,
              order_by_diff=False):
    indentation_level = '    '
    headers = ['', 'Wilcoxon P', 'T-test P',
               'median diff (%)', 'mad1 (%)', 'mad2 (%)']
    if print_extended >= print_extended_mean_data:
        headers.extend(['mean diff (%)', 'stdev1 (%)', 'stdev2 (%)'])
    if print_extended >= print_extended_raw_data:
        headers.extend(['median1', 'median2', 'mean1', 'mean2'])

    if not data_1 and not data_2:
        # There is nothing to compare or print.
        return

    if utils.IsDictionaryOrNone(data_1) and utils.IsDictionaryOrNone(data_2):
        if key is not None:
            print(indentation + key)
        entries = []
        list_1 = list(data_1.keys()) if data_1 else []
        list_2 = list(data_2.keys()) if data_2 else []
        for k in utils.MergeLists(list_1, list_2):
            value_1 = data_1[k] if data_1 and k in data_1 else None
            value_2 = data_2[k] if data_2 and k in data_2 else None
            maybe_entry = PrintDiff(value_1, value_2, k,
                                    indentation + indentation_level,
                                    print_extended=print_extended)
            if maybe_entry is not None:
                entries.append(maybe_entry)
        if entries:
            if order_by_diff:
                entries = OrderByDiff(entries)
            utils_print.PrintTable(headers, entries, line_start=indentation)
            print('')
    elif utils.IsListOrNone(data_1) and utils.IsListOrNone(data_2):
        no_results = ('', '', '', '', '', '', '', '')
        _, _, med1, _, madp1, ave1, _, dp1 = \
                utils_stats.ComputeStats(data_1) if data_1 else no_results
        _, _, med2, _, madp2, ave2, _, dp2 = \
                utils_stats.ComputeStats(data_2) if data_2 else no_results
        wilcoxon_p, ttest_p = utils_stats.ComputeStatsTests(data_1, data_2)
        if data_1 and data_2:
            median_diff = utils_stats.GetRelativeDiff(med1, med2)
            mean_diff = utils_stats.GetRelativeDiff(ave1, ave2)
        else:
            median_diff = ''
            mean_diff = ''
        res = [key, wilcoxon_p, ttest_p, median_diff, madp1, madp2]
        if print_extended >= print_extended_mean_data:
            res.extend([mean_diff, dp1, dp2])
        if print_extended >= print_extended_raw_data:
            res.extend([med1, med2, ave1, ave2])
        return res
    else:
        utils.Error('Unexpected data types %s and %s.' % \
                    (str(type(data_1)), str(type(data_2))))

if __name__ == "__main__":
    args = BuildOptions()
    file_1 = open(args.res_1, 'r')
    file_2 = open(args.res_2, 'r')
    res_1 = json.load(file_1, object_pairs_hook=OrderedDict)
    res_2 = json.load(file_2, object_pairs_hook=OrderedDict)
    file_1.close()
    file_2.close()

    res_1 = utils.Filter(res_1, args.filter, args.filter_out)
    res_2 = utils.Filter(res_2, args.filter, args.filter_out)

    if args.significant_changes:
        res_1, res_2 = \
            FilterSignificantChanges(res_1, res_2,
                                     args.wilcoxon_p_threshold,
                                     args.ttest_p_threshold)

    PrintDiff(res_1, res_2,
              print_extended=args.print_extended,
              order_by_diff=args.order_by_diff)
    if utils.HaveSameKeys(res_1, res_2):
        utils_stats.ComputeAndPrintRelationGeomean(
            utils.Unflatten(res_1),
            utils.Unflatten(res_2),
            args.print_extended >= print_extended_raw_data)
    else:
        utils.Info("Not comparing the geomeans because the two result sets "
                   "have different keys.")
