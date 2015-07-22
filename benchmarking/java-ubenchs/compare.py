#! /usr/bin/env python3

#    Copyright 2015 ARM Limited
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

import argparse
import pickle

import utils

def BuildOptions():
    parser = argparse.ArgumentParser(
        description = "Compare two results of the `run.py` script.",
        # Print default values.
        formatter_class = argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument('res_1', metavar = 'res_1.pkl')
    parser.add_argument('res_2', metavar = 'res_2.pkl')
    return parser.parse_args()


if __name__ == "__main__":
    args = BuildOptions()
    pkl_file_1 = open(args.res_1, 'rb')
    pkl_file_2 = open(args.res_2, 'rb')
    res_1 = pickle.load(pkl_file_1)
    res_2 = pickle.load(pkl_file_2)
    utils.PrintDiff(res_1, res_2)
    pkl_file_1.close()
    pkl_file_2.close()
