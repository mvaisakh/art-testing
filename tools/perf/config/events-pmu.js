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

// a53 pmu events definition.
// http://infocenter.arm.com/help/index.jsp?topic=/com.arm.doc.ddi0500f/BIIDBAFB.html
var events_pmu = [
  {
    "EventCode": "0x00",
    "EventName": "SW_INCR",
    "BriefDescription": "Software increment. The register is incremented only on writes to the Software Increment Register",
    "PublicDescription": "Software increment. The register is incremented only on writes to the Software Increment Register"
  } ,
  {
    "EventCode": "0x01",
    "EventName": "L1I_CACHE_REFILL",
    "BriefDescription": "L1 Instruction cache refill",
    "PublicDescription": "L1 Instruction cache refill"
  } ,
  {
    "EventCode": "0x02",
    "EventName": "L1I_TLB_REFILL",
    "BriefDescription": "L1 Instruction TLB refill",
    "PublicDescription": "L1 Instruction TLB refill"
  } ,
  {
    "EventCode": "0x03",
    "EventName": "L1D_CACHE_REFILL",
    "BriefDescription": "L1 Data cache refill",
    "PublicDescription": "L1 Data cache refill"
  } ,
  {
    "EventCode": "0x04",
    "EventName": "L1D_CACHE",
    "BriefDescription": "L1 Data cache access",
    "PublicDescription": "L1 Data cache access"
  } ,
  {
    "EventCode": "0x05",
    "EventName": "L1D_TLB_REFILL",
    "BriefDescription": "L1 Data TLB refill",
    "PublicDescription": "L1 Data TLB refill"
  } ,
  {
    "EventCode": "0x06",
    "EventName": "LD_RETIRED",
    "BriefDescription": "Instruction architecturally executed, condition check pass - load",
    "PublicDescription": "Instruction architecturally executed, condition check pass - load"
  } ,
  {
    "EventCode": "0x07",
    "EventName": "ST_RETIRED",
    "BriefDescription": "Instruction architecturally executed, condition check pass - store",
    "PublicDescription": "Instruction architecturally executed, condition check pass - store"
  } ,
  {
    "EventCode": "0x08",
    "EventName": "INST_RETIRED",
    "BriefDescription": "Instruction architecturally executed",
    "PublicDescription": "Instruction architecturally executed"
  } ,
  {
    "EventCode": "0x09",
    "EventName": "EXC_TAKEN",
    "BriefDescription": "Exception taken",
    "PublicDescription": "Exception taken"
  } ,
  {
    "EventCode": "0x0A",
    "EventName": "EXC_RETURN",
    "BriefDescription": "Exception return",
    "PublicDescription": "Exception return"
  } ,
  {
    "EventCode": "0x0B",
    "EventName": "CID_WRITE_RETIRED",
    "BriefDescription": "Change to Context ID retired",
    "PublicDescription": "Change to Context ID retired"
  } ,
  {
    "EventCode": "0x0C",
    "EventName": "PC_WRITE_RETIRED",
    "BriefDescription": "Instruction architecturally executed, condition check pass, software change of the PC",
    "PublicDescription": "Instruction architecturally executed, condition check pass, software change of the PC"
  } ,
  {
    "EventCode": "0x0D",
    "EventName": "BR_IMMED_RETIRED",
    "BriefDescription": "Instruction architecturally executed, immediate branch",
    "PublicDescription": "Instruction architecturally executed, immediate branch"
  } ,
  {
    "EventCode": "0x0E",
    "EventName": "BR_RETURN_RETIRED",
    "BriefDescription": "Instruction architecturally executed, condition code check pass, procedure return",
    "PublicDescription": "Instruction architecturally executed, condition code check pass, procedure return"
  } ,
  {
    "EventCode": "0x0F",
    "EventName": "UNALIGNED_LDST_RETIRED",
    "BriefDescription": "Instruction architecturally executed, condition check pass, unaligned load or store",
    "PublicDescription": "Instruction architecturally executed, condition check pass, unaligned load or store"
  } ,
  {
    "EventCode": "0x10",
    "EventName": "BR_MIS_PRED",
    "BriefDescription": "Mispredicted or not predicted branch speculatively executed",
    "PublicDescription": "Mispredicted or not predicted branch speculatively executed"
  } ,
  {
    "EventCode": "0x11",
    "EventName": "CPU_CYCLES",
    "BriefDescription": "Cycle",
    "PublicDescription": "Cycle"
  } ,
  {
    "EventCode": "0x12",
    "EventName": "BR_PRED",
    "BriefDescription": "Predictable branch speculatively executed",
    "PublicDescription": "Predictable branch speculatively executed"
  } ,
  {
    "EventCode": "0x13",
    "EventName": "MEM_ACCESS",
    "BriefDescription": "Data memory access",
    "PublicDescription": "Data memory access"
  } ,
  {
    "EventCode": "0x14",
    "EventName": "L1I_CACHE",
    "BriefDescription": "L1 Instruction cache access",
    "PublicDescription": "L1 Instruction cache access"
  } ,
  {
    "EventCode": "0x15",
    "EventName": "L1D_CACHE_WB",
    "BriefDescription": "L1 Data cache Write-Back",
    "PublicDescription": "L1 Data cache Write-Back"
  } ,
  {
    "EventCode": "0x16",
    "EventName": "L2D_CACHE",
    "BriefDescription": "L2 Data cache access",
    "PublicDescription": "L2 Data cache access"
  } ,
  {
    "EventCode": "0x17",
    "EventName": "L2D_CACHE_REFILL",
    "BriefDescription": "L2 Data cache refill",
    "PublicDescription": "L2 Data cache refill"
  } ,
  {
    "EventCode": "0x18",
    "EventName": "L2D_CACHE_WB",
    "BriefDescription": "L2 Data cache Write-Back",
    "PublicDescription": "L2 Data cache Write-Back"
  } ,
  {
    "EventCode": "0x19",
    "EventName": "BUS_ACCESS",
    "BriefDescription": "Bus access",
    "PublicDescription": "Bus access"
  } ,
  {
    "EventCode": "0x1A",
    "EventName": "MEMORY_ERROR",
    "BriefDescription": "Local memory error",
    "PublicDescription": "Local memory error"
  } ,
  {
    "EventCode": "0x1D",
    "EventName": "BUS_CYCLES",
    "BriefDescription": "Bus cycle",
    "PublicDescription": "Bus cycle"
  } ,
  {
    "EventCode": "0x1E",
    "EventName": "CHAIN",
    "BriefDescription": "Odd performance counter chain mode",
    "PublicDescription": "Odd performance counter chain mode"
  } ,
  {
    "EventCode": "0x60",
    "EventName": "BUS_ACCESS_LD",
    "BriefDescription": "Bus access - Read",
    "PublicDescription": "Bus access - Read"
  } ,
  {
    "EventCode": "0x61",
    "EventName": "BUS_ACCESS_ST",
    "BriefDescription": "Bus access - Write",
    "PublicDescription": "Bus access - Write"
  } ,
  {
    "EventCode": "0x7A",
    "EventName": "BR_INDIRECT_SPEC",
    "BriefDescription": "Branch speculatively executed - Indirect branch",
    "PublicDescription": "Branch speculatively executed - Indirect branch"
  } ,
  {
    "EventCode": "0x86",
    "EventName": "EXC_IRQ",
    "BriefDescription": "Exception taken, IRQ",
    "PublicDescription": "Exception taken, IRQ"
  } ,
  {
    "EventCode": "0x87",
    "EventName": "EXC_FIQ",
    "BriefDescription": "Exception taken, FIQ",
    "PublicDescription": "Exception taken, FIQ"
  } ,
  {
    "EventCode": "0xC0",
    "EventName": "None",
    "BriefDescription": "External memory request",
    "PublicDescription": "External memory request"
  } ,
  {
    "EventCode": "0xC1",
    "EventName": "None",
    "BriefDescription": "Non-cacheable external memory request",
    "PublicDescription": "Non-cacheable external memory request"
  } ,
  {
    "EventCode": "0xC2",
    "EventName": "None",
    "BriefDescription": "Linefill because of prefetch",
    "PublicDescription": "Linefill because of prefetch"
  } ,
  {
    "EventCode": "0xC3",
    "EventName": "None",
    "BriefDescription": "Instruction Cache Throttle occurred",
    "PublicDescription": "Instruction Cache Throttle occurred"
  } ,
  {
    "EventCode": "0xC4",
    "EventName": "None",
    "BriefDescription": "Entering read allocate mode",
    "PublicDescription": "Entering read allocate mode"
  } ,
  {
    "EventCode": "0xC5",
    "EventName": "None",
    "BriefDescription": "Read allocate mode",
    "PublicDescription": "Read allocate mode"
  } ,
  {
    "EventCode": "0xC6",
    "EventName": "None",
    "BriefDescription": "Pre-decode error",
    "PublicDescription": "Pre-decode error"
  } ,
  {
    "EventCode": "0xC7",
    "EventName": "None",
    "BriefDescription": "Data Write operation that stalls the pipeline because the store buffer is full",
    "PublicDescription": "Data Write operation that stalls the pipeline because the store buffer is full"
  } ,
  {
    "EventCode": "0xC8",
    "EventName": "None",
    "BriefDescription": "SCU Snooped data from another CPU for this CPU",
    "PublicDescription": "SCU Snooped data from another CPU for this CPU"
  } ,
  {
    "EventCode": "0xC9",
    "EventName": "None",
    "BriefDescription": "Conditional branch executed",
    "PublicDescription": "Conditional branch executed"
  } ,
  {
    "EventCode": "0xCA",
    "EventName": "None",
    "BriefDescription": "Indirect branch mispredicted",
    "PublicDescription": "Indirect branch mispredicted"
  } ,
  {
    "EventCode": "0xCB",
    "EventName": "None",
    "BriefDescription": "Indirect branch mispredicted because of address miscompare",
    "PublicDescription": "Indirect branch mispredicted because of address miscompare"
  } ,
  {
    "EventCode": "0xCC",
    "EventName": "None",
    "BriefDescription": "Conditional branch mispredicted",
    "PublicDescription": "Conditional branch mispredicted"
  } ,
  {
    "EventCode": "0xD0",
    "EventName": "None",
    "BriefDescription": "L1 Instruction Cache (data or tag) memory error",
    "PublicDescription": "L1 Instruction Cache (data or tag) memory error"
  } ,
  {
    "EventCode": "0xD1",
    "EventName": "None",
    "BriefDescription": "L1 Data Cache (data, tag or dirty) memory error, correctable or non-correctable",
    "PublicDescription": "L1 Data Cache (data, tag or dirty) memory error, correctable or non-correctable"
  } ,
  {
    "EventCode": "0xD2",
    "EventName": "None",
    "BriefDescription": "TLB memory error",
    "PublicDescription": "TLB memory error"
  } ,
  {
    "EventCode": "0xE0",
    "EventName": "None",
    "BriefDescription": "Attributable Performance Impact Event. Counts every cycle that the DPU IQ is empty and that is not because of a recent micro-TLB miss, instruction cache miss or pre-decode error",
    "PublicDescription": "Attributable Performance Impact Event. Counts every cycle that the DPU IQ is empty and that is not because of a recent micro-TLB miss, instruction cache miss or pre-decode error"
  } ,
  {
    "EventCode": "0xE1",
    "EventName": "None",
    "BriefDescription": "Attributable Performance Impact Event. Counts every cycle the DPU IQ is empty and there is an instruction cache miss being processed",
    "PublicDescription": "Attributable Performance Impact Event. Counts every cycle the DPU IQ is empty and there is an instruction cache miss being processed"
  } ,
  {
    "EventCode": "0xE2",
    "EventName": "None",
    "BriefDescription": "Attributable Performance Impact Event. Counts every cycle the DPU IQ is empty and there is an instruction micro-TLB miss being processed",
    "PublicDescription": "Attributable Performance Impact Event. Counts every cycle the DPU IQ is empty and there is an instruction micro-TLB miss being processed"
  } ,
  {
    "EventCode": "0xE3",
    "EventName": "None",
    "BriefDescription": "Attributable Performance Impact Event. Counts every cycle the DPU IQ is empty and there is a pre-decode error being processed",
    "PublicDescription": "Attributable Performance Impact Event. Counts every cycle the DPU IQ is empty and there is a pre-decode error being processed"
  } ,
  {
    "EventCode": "0xE4",
    "EventName": "None",
    "BriefDescription": "Attributable Performance Impact Event. Counts every cycle there is an interlock that is not because of an Advanced SIMD or Floating-point instruction, and not because of a load/store instruction waiting for data to calculate the address in the AGU. Stall cycles because of a stall in Wr, typically awaiting load data, are excluded",
    "PublicDescription": "Attributable Performance Impact Event. Counts every cycle there is an interlock that is not because of an Advanced SIMD or Floating-point instruction, and not because of a load/store instruction waiting for data to calculate the address in the AGU. Stall cycles because of a stall in Wr, typically awaiting load data, are excluded"
  } ,
  {
    "EventCode": "0xE5",
    "EventName": "None",
    "BriefDescription": "Attributable Performance Impact Event. Counts every cycle there is an interlock that is because of a load/store instruction waiting for data to calculate the address in the AGU. Stall cycles because of a stall in Wr, typically awaiting load data, are excluded",
    "PublicDescription": "Attributable Performance Impact Event. Counts every cycle there is an interlock that is because of a load/store instruction waiting for data to calculate the address in the AGU. Stall cycles because of a stall in Wr, typically awaiting load data, are excluded"
  } ,
  {
    "EventCode": "0xE6",
    "EventName": "None",
    "BriefDescription": "Attributable Performance Impact Event. Counts every cycle there is an interlock that is because of an Advanced SIMD or Floating-point instruction. Stall cycles because of a stall in the Wr stage, typically awaiting load data, are excluded",
    "PublicDescription": "Attributable Performance Impact Event. Counts every cycle there is an interlock that is because of an Advanced SIMD or Floating-point instruction. Stall cycles because of a stall in the Wr stage, typically awaiting load data, are excluded"
  } ,
  {
    "EventCode": "0xE7",
    "EventName": "None",
    "BriefDescription": "Attributable Performance Impact Event Counts every cycle there is a stall in the Wr stage because of a load miss",
    "PublicDescription": "Attributable Performance Impact Event Counts every cycle there is a stall in the Wr stage because of a load miss"
  } ,
  {
    "EventCode": "0xE8",
    "EventName": "None",
    "BriefDescription": "Attributable Performance Impact Event. Counts every cycle there is a stall in the Wr stage because of a store",
    "PublicDescription": "Attributable Performance Impact Event. Counts every cycle there is a stall in the Wr stage because of a store"
  }
];

