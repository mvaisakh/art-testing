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

# cycles is used as a reference for other events. It must be the fisrt event.
events="cycles instructions cache-references cache-misses branch-misses ref-cycles cpu-clock task-clock page-faults context-switches cpu-migrations minor-faults major-faults alignment-faults emulation-faults L1-dcache-loads L1-dcache-load-misses L1-dcache-stores L1-dcache-store-misses branch-loads branch-load-misses r00 r01 r02 r03 r04 r05 r06 r07 r08 r09 r0a r0b r0c r0d r0e r0f r10 r11 r12 r13 r14 r15 r16 r17 r18 r19 r1a r1d r1e r60 r61 r7a r86 r87 rc0 rc1 rc2 rc3 rc4 rc5 rc6 rc7 rc8 rc9 rca rcb rcc rd0 rd1 rd2 re0 re1 re2 re3 re4 re5 re6 re7 re8"

