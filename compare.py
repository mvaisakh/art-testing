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
    return parser.parse_args()

def PrintDiff(data_1, data_2, key=None, indentation=''):
    indentation_level = '    '
    if data_1 or data_2:
        if (isinstance(data_1, OrderedDict) or isinstance(data_1, dict) or data_1 is None) and \
           (isinstance(data_2, OrderedDict) or isinstance(data_2, dict) or data_2 is None):
            if key is not None:
                print(indentation + key)
            entries = []
            list_1 = list(data_1.keys()) if data_1 else []
            list_2 = list(data_2.keys()) if data_2 else []
            for k in utils.MergeLists(list_1, list_2):
                value_1 = data_1[k] if data_1 and k in data_1 else None
                value_2 = data_2[k] if data_2 and k in data_2 else None
                maybe_entry = PrintDiff(value_1, value_2, k, indentation + indentation_level)
                if maybe_entry is not None:
                    entries.append(maybe_entry)
            if entries:
                utils_print.PrintTable([''] + utils_stats.stats_diff_headers,
                                       ['s'] + utils_stats.stats_diff_formats,
                                       entries,
                                       line_start=indentation)
                print('')
        elif (isinstance(data_1, list) or data_1 is None) and \
             (isinstance(data_2, list) or data_2 is None):
            list_1 = data_1 if data_1 else [0.0]
            list_2 = data_2 if data_2 else [0.0]
            m1, M1, ave1, d1, dp1 = utils_stats.ComputeStats(list_1)
            m2, M2, ave2, d2, dp2 = utils_stats.ComputeStats(list_2)
            return [key] + \
                   [ave1, dp1, ave2, dp2, utils_stats.GetRelativeDiff(ave1, ave2)]
        elif type(data_1) != type(data_2):
            utils.Error('The data types differ between result sets.')

if __name__ == "__main__":
    args = BuildOptions()
    pkl_file_1 = open(args.res_1, 'rb')
    pkl_file_2 = open(args.res_2, 'rb')
    res_1 = pickle.load(pkl_file_1)
    res_2 = pickle.load(pkl_file_2)
    pkl_file_1.close()
    pkl_file_2.close()
    PrintDiff(res_1, res_2)
