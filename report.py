#! /usr/bin/env python3

# Copyright (C) 2016 Linaro Limited. All rights received.
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
from tools import utils_stats

def BuildOptions():
    parser = argparse.ArgumentParser(
        description = "Display the results for a json result file.",
        # Print default values.
        formatter_class = argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument('result_file', metavar = 'res.json')
    utils.AddReportFilterOptions(parser)
    return parser.parse_args()

def Report(data):
    data = utils.Filter(data, args.filter, args.filter_out)
    utils.PrintData(data)
    unflattened_data = utils.Unflatten(data)
    utils_stats.ComputeAndPrintGeomeanWithRelativeDiff(unflattened_data)

if __name__ == "__main__":
    args = BuildOptions()
    with open(args.result_file, 'r') as result_file:
        data = json.load(result_file, object_pairs_hook=OrderedDict)
    Report(data)
