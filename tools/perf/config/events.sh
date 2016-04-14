#!/bin/sh

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

perf_dir=$(dirname "$0")
config_dir="$perf_dir/config"

# cycles is used as a reference for other events. It must be the first event.
general_events=$($config_dir/print_event_ids.sh $config_dir/events-generic.js)

if [[ -f "$config_dir/events-pmu-custom.js" ]]; then
  cpu_specific_events=$($config_dir/print_event_ids.sh $config_dir/events-pmu-custom.js)
else
  echo "ERROR: Unknown cpu. Only general events will be collected."
fi

events="$general_events $cpu_specific_events"
