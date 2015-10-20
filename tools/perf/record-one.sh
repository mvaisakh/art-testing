#!/bin/bash

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

. $(dirname $0)/common.sh

usage() {
  echo "Usage: $(basename "$0") \"<command>\" <out_folder> <event>"
  echo "  Capture event when executing command and save data to out_folder/event.perf.data"
  echo "Example:"
  echo "  $(basename "$0") \"find > /dev/null\" perf-out/find cycles"
}

if [ $# -ne 3 ] ; then
  usage
  exit
fi

cmd=$1
out=$2
event=$3

if [ "$event" == "cycles" ] ; then
  bench_log=$out/bench.log
else
  bench_log=/dev/null
fi

safe mkdir -p $out
safe adb shell ANDROID_DATA=$UBENCH_REMOTE_DIR DEX_LOCATION=$UBENCH_REMOTE_DIR $PERF_RECORD -g -e $event -o $REMOTE_PERF_DATA $cmd > $bench_log
safe adb pull $REMOTE_PERF_DATA $out/${event}.perf.data

