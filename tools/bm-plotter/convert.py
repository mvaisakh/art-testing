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
import os
import sys

from collections import OrderedDict

dir_bm_plotter = os.path.dirname(os.path.realpath(__file__))
dir_tools = os.path.join(dir_bm_plotter, '..')
sys.path.insert(0, dir_tools)
import utils

def BuildOptions():
    parser = argparse.ArgumentParser(
        description = '''Convert output JSON files to bm-plotter format. The
        output should fed to bm-plotter:
        https://github.com/ARM-software/bm-plotter. See `README.md` for
        examples''',
        # Print default values.
        formatter_class = argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument('json_files', nargs='+')
    return parser.parse_args()

if __name__ == "__main__":
    args = BuildOptions()

    csv_data = [['Column', 'Set', 'Benchmark', 'Result']]

    for json_file in args.json_files:
        with open(json_file, 'r') as result_file:
            data = json.load(result_file, object_pairs_hook=OrderedDict)
            data = utils.Flatten(data)
            for bench in data:
                for value in data[bench]:
                    print(','.join(['column', json_file, bench, str(value)]))

