/*
 *    Copyright 2015 ARM Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

// pre-defined perf events, can be get from "perf list".
var events_generic = [
  {
    "EventCode": "cycles",
    "EventName": "cycles",
    "BriefDescription": "Hardware event",
    "PublicDescription": "Hardware event"
  } ,
  {
    "EventCode": "instructions",
    "EventName": "instructions",
    "BriefDescription": "Hardware event",
    "PublicDescription": "Hardware event"
  } ,
  {
    "EventCode": "cache-references",
    "EventName": "cache-references",
    "BriefDescription": "Hardware event",
    "PublicDescription": "Hardware event"
  } ,
  {
    "EventCode": "cache-misses",
    "EventName": "cache-misses",
    "BriefDescription": "Hardware event",
    "PublicDescription": "Hardware event"
  } ,
  {
    "EventCode": "branch-misses",
    "EventName": "branch-misses",
    "BriefDescription": "Hardware event",
    "PublicDescription": "Hardware event"
  } ,
  {
    "EventCode": "ref-cycles",
    "EventName": "ref-cycles",
    "BriefDescription": "Hardware event",
    "PublicDescription": "Hardware event"
  } ,

  {
    "EventCode": "cpu-clock",
    "EventName": "cpu-clock",
    "BriefDescription": "Software event",
    "PublicDescription": "Software event"
  } ,
  {
    "EventCode": "task-clock",
    "EventName": "task-clock",
    "BriefDescription": "Software event",
    "PublicDescription": "Software event"
  } ,
  {
    "EventCode": "page-faults",
    "EventName": "page-faults",
    "BriefDescription": "Software event",
    "PublicDescription": "Software event"
  } ,
  {
    "EventCode": "context-switches",
    "EventName": "context-switches",
    "BriefDescription": "Software event",
    "PublicDescription": "Software event"
  } ,
  {
    "EventCode": "cpu-migrations",
    "EventName": "cpu-migrations",
    "BriefDescription": "Software event",
    "PublicDescription": "Software event"
  } ,
  {
    "EventCode": "minor-faults",
    "EventName": "minor-faults",
    "BriefDescription": "Software event",
    "PublicDescription": "Software event"
  } ,
  {
    "EventCode": "major-faults",
    "EventName": "major-faults",
    "BriefDescription": "Software event",
    "PublicDescription": "Software event"
  } ,
  {
    "EventCode": "alignment-faults",
    "EventName": "alignment-faults",
    "BriefDescription": "Software event",
    "PublicDescription": "Software event"
  } ,
  {
    "EventCode": "emulation-faults",
    "EventName": "emulation-faults",
    "BriefDescription": "Software event",
    "PublicDescription": "Software event"
  } ,

  {
    "EventCode": "L1-dcache-loads",
    "EventName": "L1-dcache-loads",
    "BriefDescription": "Hardware cache event",
    "PublicDescription": "Hardware cache event"
  } ,
  {
    "EventCode": "L1-dcache-load-misses",
    "EventName": "L1-dcache-load-misses",
    "BriefDescription": "Hardware cache event",
    "PublicDescription": "Hardware cache event"
  } ,
  {
    "EventCode": "L1-dcache-stores",
    "EventName": "L1-dcache-stores",
    "BriefDescription": "Hardware cache event",
    "PublicDescription": "Hardware cache event"
  } ,
  {
    "EventCode": "L1-dcache-store-misses",
    "EventName": "L1-dcache-store-misses",
    "BriefDescription": "Hardware cache event",
    "PublicDescription": "Hardware cache event"
  } ,
  {
    "EventCode": "branch-loads",
    "EventName": "branch-loads",
    "BriefDescription": "Hardware cache event",
    "PublicDescription": "Hardware cache event"
  } ,
  {
    "EventCode": "branch-load-misses",
    "EventName": "branch-load-misses",
    "BriefDescription": "Hardware cache event",
    "PublicDescription": "Hardware cache event"
  } ,
];

