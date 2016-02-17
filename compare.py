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

from tools import utils
from tools import utils_stats

sys.path.insert(0, os.path.join(utils.dir_tools, 'compilation_statistics'))

from compare import PrintDiff

def BuildOptions():
    parser = argparse.ArgumentParser(
        description = "Compare two results of the associated `run.py` script.",
        # Print default values.
        formatter_class = argparse.ArgumentDefaultsHelpFormatter)
    utils.AddCommonCompareOptions(parser)
    return parser.parse_args()

if __name__ == "__main__":
    args = BuildOptions()
    pkl_file_1 = open(args.res_1, 'rb')
    pkl_file_2 = open(args.res_2, 'rb')
    res_1 = pickle.load(pkl_file_1)
    res_2 = pickle.load(pkl_file_2)
    pkl_file_1.close()
    pkl_file_2.close()
    results = sorted([result for result in res_1.keys() if result in res_2])

    for r in results:
        if r == 'benchmarks':
            f = utils_stats.PrintDiff
        elif r == 'compilation_statistics':
            f = PrintDiff
        else:
            continue

        f(res_1[r], res_2[r])
        print('')
