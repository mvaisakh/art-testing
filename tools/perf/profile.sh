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

# Check if the AOSP build matches the target board.
board=$($ADB shell getprop ro.build.product)
print_info Board : $board
print_info ANDROID_PRODUCT_OUT : $ANDROID_PRODUCT_OUT
if [ "$(basename $ANDROID_PRODUCT_OUT)" != "$board" ] ; then
  error AOSP build does not match the target board!
fi

safe cd $(dirname $UBENCH_SRC_FOLDER)
bench_sources=$(find $(basename $UBENCH_SRC_FOLDER) -name "*.java")
safe cd -

# Build benchmark.
$SCRIPT_PATH/build-wrapper.sh || exit 1

# Initialize js file.
safe mkdir -p $PERF_OUT
safe rm -f $PERF_OUT/bench_result.js
safe touch $PERF_OUT/bench_result.js

# Allow record and analyze to run at the same time. Use wait pid to avoid
# analyze different output at the same time which might mess up the output.
analyze_pid=0

# Profile java micro-benchmarks.
for bench in $bench_sources ; do
  bench_name=${bench%.java}
  bench_name=${bench_name//\//.}
  for dalvikvm in $REMOTE_DALVIKVMS ; do
    vm=$(basename $dalvikvm)
    print_info Recording events for $bench_name on $vm
    $SCRIPT_PATH/record-events.sh "$vm -cp /data/local/tmp/bench.apk $bench_name" $PERF_OUT/${bench_name}_${vm} || exit 1
    test $analyze_pid -eq 0 || safe wait $analyze_pid
    print_info Analyzing profile data for $bench_name on $vm
    $SCRIPT_PATH/analyze.sh $PERF_OUT/${bench_name}_${vm} ${bench_name}_${vm} $PERF_OUT/bench_result.js || exit 1 &
    analyze_pid=$!
    print_info analyze.sh pid : $analyze_pid
  done
done

# Profile commands.
. $SCRIPT_PATH/config/commands.sh
benchs=$(grep -vP "^#" $SCRIPT_PATH/config/commands.sh | grep "=" | cut -d= -f1)
for bench in $benchs ; do
  eval cmd=\$$bench
  print_info Recording events for "$cmd"
  $SCRIPT_PATH/record-events.sh "$cmd" $PERF_OUT/$bench || exit 1
  test $analyze_pid -eq 0 || safe wait $analyze_pid
  print_info Analyzing profile data for "$cmd"
  $SCRIPT_PATH/analyze.sh $PERF_OUT/$bench $bench $PERF_OUT/bench_result.js || exit 1 &
  analyze_pid=$!
done

wait $analyze_pid

