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
    utils.AddCommonCompareOptions(parser)
    parser.add_argument('--print-means',
                        action='store_true', default=False,
                        help='Print means for both data sets.')
    return parser.parse_args()

def IsDictionaryOrNone(d):
    return isinstance(d, OrderedDict) or isinstance(d, dict) or d is None

def IsListOrNone(d):
    return isinstance(d, list) or d is None

def PrintDiff(data_1, data_2, key=None, indentation='', print_means=False):
    indentation_level = '    '
    if print_means:
        headers = ['', 'diff (%)', 'mean1', 'stdev1 (%)', 'mean2', 'stdev2 (%)']
    else:
        headers = ['', 'diff (%)', 'stdev1 (%)', 'stdev2 (%)']

    if not data_1 and not data_2:
        # There is nothing to compare or print.
        return

    if IsDictionaryOrNone(data_1) and IsDictionaryOrNone(data_2):
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
                                    print_means=print_means)
            if maybe_entry is not None:
                entries.append(maybe_entry)
        if entries:
            utils_print.PrintTable(headers, entries, line_start=indentation)
            print('')
    elif IsListOrNone(data_1) and IsListOrNone(data_2):
        no_results = ('', '', '', '', '')
        _, _, ave1, _, dp1 = \
                utils_stats.ComputeStats(data_1) if data_1 else no_results
        _, _, ave2, _, dp2 = \
                utils_stats.ComputeStats(data_2) if data_2 else no_results
        diff = utils_stats.GetRelativeDiff(ave1, ave2) \
                if data_1 and data_2 else ''
        if print_means:
            return [key, diff, ave1, dp1, ave2, dp2]
        else:
            return [key, diff, dp1, dp2]
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

    PrintDiff(res_1, res_2, print_means=args.print_means)
