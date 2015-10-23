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

. $(dirname "$0")/common.sh

usage() {
  echo "Usage: $(basename "$0") <perf_data_folder> [<case_name> <js_file>]"
  echo "  Analyze a series of perf data and convert to some human readable files."
  echo "  The output file names will be store in a js object and append to the given js file."
  echo "Example:"
  echo "  $(basename "$0") perf-out/find find perf-out/bench_result.js"
}

if [ $# -eq 3 ] ; then
  data=$(realpath $1)
  name=$2
  touch $3
  temp_js_file=/tmp/temp.js
  js_file=$(realpath $3)
elif [ $# -eq 1 ] ; then
  data=$(realpath $1)
  name=ignored
  temp_js_file=/dev/null
  js_file=/dev/null
else
  usage
  exit
fi

num_cpus=$(grep -c ^processor /proc/cpuinfo)
if [ $num_cpus -gt 1 ] ; then
max_annotate_jobs=$(($num_cpus - 1))
else
max_annotate_jobs=$num_cpus
fi

# TODO: Remove the limination once we can use `-i` with `perf`.
# Since all annotate jobs rely on the local copy of `perf.data`, we cannot
# annotate hotspots in parallel.
max_annotate_jobs=1

# Load events from events.sh
. $SCRIPT_PATH/config/events.sh

# Check if perf data has been captured with cycles event included.
for event in cycles $events ; do
  if [ ! -f $data/${event}.perf.data ] ; then
    print_error Missing ${event}.perf.data
    usage
    exit
  fi
done

# Load max_hotspots from max_hotspots.sh
. $SCRIPT_PATH/config/max_hotspots.sh


# Print warning message if vmlinux does not exist.
if [ ! -f $VMLINUX ] ; then
  print_warning Kernel symbol file not found! Kernel symbols will be missing.
fi

# TODO: Ideally we would like to run `perf` with the `-i` option but the version
# of perf distributed in Ubuntu 15.04 (3.19.3 at least) has a bug that ignore
# the `-i` option for `perf annotate`. So instead we locally copy the
# `perf.data` file.
safe cp -f $data/cycles.perf.data ./perf.data
# Note: Only report and flame graph for cycles event is generated.
# Generate report.
unsafe $PERF_REPORT $PERF_BINUTILS_FLAG $PERF_SYMBOL_FLAG > $data/cycles.perf.report
# Generate script
unsafe $PERF_SCRIPT $PERF_SYMBOL_FLAG > $data/cycles.perf.script
# Stack collapse.
unsafe $SCRIPT_PATH/FlameGraph/stackcollapse-perf.pl $data/cycles.perf.script > $data/cycles.perf.stackcollapse
# Flame graph.
unsafe $SCRIPT_PATH/FlameGraph/flamegraph.pl $data/cycles.perf.stackcollapse > $data/cycles.perf.html
# Remove the local copy of perf.data
# TODO: Remove the below line, once the local copy is no longer needed.
safe rm -f ./perf.data


# Write information to temp js file.
echo -n "
bench_result.push({
  name:'$name',
  log:'bench.log',
  report:'cycles.perf.report',
  flame_graph:'cycles.perf.html',
  hotspots:[" > $temp_js_file

# Annotate hotspots.
i=1
while [ $i -le $max_hotspots ] ; do
# Get ith hotspot.
  hotspot=$(echo $(grep -P "^\s*\d*\.\d*%" $data/cycles.perf.report | head -n $i | tail -n 1 | sed 's/.*\[.\]//'))
# Append information to temp js file.
  echo -n "
    {
      name:'$hotspot',
      events:[" >> $temp_js_file
# Annotate for each event.
  for event in $events ; do
    # TODO: Ideally we would like to run `perf` with the `-i` option but the
    # version of perf distributed in Ubuntu 15.04 (3.19.3 at least) has a bug
    # that ignore the `-i` option for `perf annotate`. So instead we locally
    # copy the `perf.data` file.
    safe cp -f $data/${event}.perf.data ./perf.data
    rate=$(echo $(unsafe $PERF_REPORT $PERF_BINUTILS_FLAG $PERF_SYMBOL_FLAG | grep -P "^\s*\d*\.\d*%" | grep -F "$hotspot" | head -n 1 | awk -F"%" '{print $1}'))
    # Remove the local copy of perf.data
    # TODO: Remove the below line, once the local copy is no longer needed.
    safe rm -f ./perf.data
    if [ "$rate" = "" ] ; then
      rate=0
    fi
# Annotate in plan_src folder if exists, since we do not have path information for java sources.
    test -d $STRUCTURED_SOURCE_FOLDER && safe cd $STRUCTURED_SOURCE_FOLDER
# FIXME: run, safe and unsafe don't work well with "
    print_info Annotating $event in $hotspot.
    # TODO: Ideally we would like to run `perf` with the `-i` option but the
    # version of perf distributed in Ubuntu 15.04 (3.19.3 at least) has a bug
    # that ignore the `-i` option for `perf annotate`. So instead we locally
    # copy the `perf.data` file.
    safe cp -f $data/${event}.perf.data ./perf.data
    $PERF_ANNOTATE $PERF_BINUTILS_FLAG $PERF_SYMBOL_FLAG "$hotspot" > $data/hotspot_${i}.${event}.perf.annotate &
# Wait if the number of annotation tasks reaches its limitation.
    while [ $(jobs -p | wc -l) -ge $max_annotate_jobs ] ; do
      safe wait -n
    done
    # Remove the local copy of perf.data
    # TODO: Remove the below line, once the local copy is no longer needed.
    safe rm -f ./perf.data
    test -d $STRUCTURED_SOURCE_FOLDER && safe cd -
# Append information to temp js file.
    echo -n "
        {name:'$event',rate:$rate,file:'hotspot_${i}.${event}.perf.annotate'}," >> $temp_js_file
  done
# Append information to temp js file.
  echo -n "
      ],
    }," >> $temp_js_file
  i=$(($i + 1))
done

# Append information to temp js file.
echo "
  ]
});" >> $temp_js_file

# Append temp js file to js file.
cat $temp_js_file >> $js_file

# Wait for all background annotation tasks.
safe wait

