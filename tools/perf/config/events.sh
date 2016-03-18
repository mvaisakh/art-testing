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

cpu_type_cfg_file="`pwd`/config/cpu-type-cfg.js"
cpu_type=`cat $cpu_type_cfg_file`

# cycles is used as a reference for other events. It must be the first event.
general_events="cycles instructions cache-references cache-misses branch-misses ref-cycles cpu-clock task-clock page-faults context-switches cpu-migrations minor-faults major-faults alignment-faults emulation-faults L1-dcache-loads L1-dcache-load-misses L1-dcache-stores L1-dcache-store-misses branch-loads branch-load-misses"

a53_events="r00 r01 r02 r03 r04 r05 r06 r07 r08 r09 r0a r0b r0c r0d r0e r0f r10 r11 r12 r13 r14 r15 r16 r17 r18 r19 r1a r1d r1e r60 r61 r7a r86 r87 rc0 rc1 rc2 rc3 rc4 rc5 rc6 rc7 rc8 rc9 rca rcb rcc rd0 rd1 rd2 re0 re1 re2 re3 re4 re5 re6 re7 re8"

a57_events="r00 r01 r02 r03 r04 r05 r08 r09 r0A r0B r10 r11 r12 r13 r14 r15 r16 r17 r18 r19 r1A r1B r1C r1D r1E r40 r41 r42 r43 r46 r47 r48 r4C r4D r50 r51 r52 r53 r56 r57 r58 r60 r61 r62 r63 r64 r65 r66 r67 r68 r69b r6Ab r6C r6D r6E r70 r71 r72 r73 r74 r75 r76 r77 r78 r79 r7A r7C r7D r7E r81 r82 r83 r84 r86 r87 r88 r8A r8B r8C r8D r8E r8F r90 r91"


if [ "$cpu_type" = "var cpu_type = \"a53\";" ]; then
  echo "CPU_TYPE: a53"
  cpu_specific_events=$a53_events
elif [ "$cpu_type" = "var cpu_type = \"a57\";" ]; then
  echo "CPU_TYPE: a57"
  cpu_specific_events=$a57_events
else
  echo "ERROR: Unknown cpu. Only general events will be collected."
fi

events="$general_events $cpu_specific_events"
