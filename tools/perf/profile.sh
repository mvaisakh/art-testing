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

# Arguments handling

usage="Usage: $(basename "$0")
Do performance analysis for the benchmarks.

Options:
    -h                  Show this help message.
    -b SUITE/BENCH.java Collect perf data only for particular benchmark.
                        Example: -b micro/ShifterOperand.java.
    -f                  Pass options to dalvikvm in quotes.
                        Example: -f \"-Xcompiler-option -g\".
"

while getopts ':hb:f:' option; do
  case "$option" in
    h) echo "$usage"; exit ;;
    b) single_bench_mode="ON"
       single_bench=$OPTARG
       ;;
    f) vm_cl_flags_opt="-f $OPTARG"
       vm_cl_flags="$OPTARG"
       ;;
    \?)
      echo "Illegal option: -$OPTARG" >&2
      echo "$usage"
      exit 1
      ;;
    :)
      echo "Option -$OPTARG requires an argument." >&2
      exit 1
      ;;
  esac
done

shift $((OPTIND - 1))

if [[ $# -ne 0 ]]; then
  echo "$usage"
  error "Wrong number of arguments"
fi

# Check if the AOSP build matches the target board.
board=$($ADB shell getprop ro.build.product)
print_info Board : $board
print_info ANDROID_PRODUCT_OUT : $ANDROID_PRODUCT_OUT
if [ "$(basename $ANDROID_PRODUCT_OUT)" != "$board" ] ; then
  error AOSP build does not match the target board!
fi

if [[ $single_bench_mode == "ON" ]]; then
  bench="benchmarks/$single_bench"
  bench_realpath="$UBENCH_SRC_FOLDER/$single_bench"

  if [[ ! (-f $bench_realpath) || ("${bench_realpath##*.}" != "java") ]]; then
    error "$0: Provide a proper benchmark for single benchmark mode."
  fi

  echo "$0: bench_realpath: $bench_realpath"

  bench_sources=$bench
  print_info "$0: Single benchmark mode: \"$bench\""

  $SCRIPT_PATH/build-wrapper.sh -b $bench_realpath "$vm_cl_flags_opt" || exit 1
else
  safe cd $(dirname $UBENCH_SRC_FOLDER)
  bench_sources=$(find $(basename $UBENCH_SRC_FOLDER) -name "*.java")
  safe cd -
  $SCRIPT_PATH/build-wrapper.sh "$vm_cl_flags_opt" || exit 1
fi

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
    $SCRIPT_PATH/record-events.sh "$vm $vm_cl_flags -cp /data/local/tmp/bench.apk $bench_name" $PERF_OUT/${bench_name}_${vm} || exit 1
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

