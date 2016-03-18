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

// a57 pmu events definition.
// Can be found: http://infocenter.arm.com/help/index.jsp?topic=/com.arm.doc.ddi0488g/way1382543438508.html
var events_pmu_a57 = [
  {
    "EventCode": "0x00",
    "EventName": "SW_INCR",
    "BriefDescription":  "Instruction architecturally executed (condition check pass) - Software increment",
    "PublicDescription": "Instruction architecturally executed (condition check pass) - Software increment"
  } ,
  {
    "EventCode": "0x01",
    "EventName": "L1I_CACHE_REFILL",
    "BriefDescription":  "Level 1 instruction cache refill",
    "PublicDescription": "Level 1 instruction cache refill"
  } ,
  {
    "EventCode": "0x02",
    "EventName": "L1I_TLB_REFILL",
    "BriefDescription":  "Level 1 instruction TLB refill",
    "PublicDescription": "Level 1 instruction TLB refill"
  } ,
  {
    "EventCode": "0x03",
    "EventName": "L1D_CACHE_REFILL",
    "BriefDescription":  "Level 1 data cache refill",
    "PublicDescription": "Level 1 data cache refill"
  } ,
  {
    "EventCode": "0x04",
    "EventName": "L1D_CACHE",
    "BriefDescription":  "Level 1 data cache access",
    "PublicDescription": "Level 1 data cache access"
  } ,
  {
    "EventCode": "0x05",
    "EventName": "L1D_TLB_REFILL",
    "BriefDescription":  "Level 1 data TLB refill",
    "PublicDescription": "Level 1 data TLB refill"
  } ,
  {
    "EventCode": "0x08",
    "EventName": "INST_RETIRED",
    "BriefDescription":  "Instruction architecturally executed",
    "PublicDescription": "Instruction architecturally executed"
  } ,
  {
    "EventCode": "0x09",
    "EventName": "EXC_TAKEN",
    "BriefDescription":  "Exception taken",
    "PublicDescription": "Exception taken"
  } ,
  {
    "EventCode": "0x0A",
    "EventName": "EXC_RETURN",
    "BriefDescription":  "Instruction architecturally executed (condition check pass) - Exception return",
    "PublicDescription": "Instruction architecturally executed (condition check pass) - Exception return"
  } ,
  {
    "EventCode": "0x0B",
    "EventName": "CID_WRITE_RETIRED",
    "BriefDescription":  "Instruction architecturally executed (condition check pass) - Write to CONTEXTIDR",
    "PublicDescription": "Instruction architecturally executed (condition check pass) - Write to CONTEXTIDR"
  } ,
  {
    "EventCode": "0x10",
    "EventName": "BR_MIS_PRED",
    "BriefDescription":  "Mispredicted or not predicted branch speculatively executed",
    "PublicDescription": "Mispredicted or not predicted branch speculatively executed"
  } ,
  {
    "EventCode": "0x11",
    "EventName": "CPU_CYCLES",
    "BriefDescription":  "Cycle",
    "PublicDescription": "Cycle"
  } ,
  {
    "EventCode": "0x12",
    "EventName": "BR_PRED",
    "BriefDescription":  "Predictable branch speculatively executed",
    "PublicDescription": "Predictable branch speculatively executed"
  } ,
  {
    "EventCode": "0x13",
    "EventName": "MEM_ACCESS",
    "BriefDescription":  "Data memory access",
    "PublicDescription": "Data memory access"
  } ,
  {
    "EventCode": "0x14",
    "EventName": "L1I_CACHE",
    "BriefDescription":  "Level 1 instruction cache access",
    "PublicDescription": "Level 1 instruction cache access"
  } ,
  {
    "EventCode": "0x15",
    "EventName": "L1D_CACHE_WB",
    "BriefDescription":  "Level 1 data cache Write-Back",
    "PublicDescription": "Level 1 data cache Write-Back"
  } ,
  {
    "EventCode": "0x16",
    "EventName": "L2D_CACHE",
    "BriefDescription":  "Level 2 data cache access",
    "PublicDescription": "Level 2 data cache access"
  } ,
  {
    "EventCode": "0x17",
    "EventName": "L2D_CACHE_REFILL",
    "BriefDescription":  "Level 2 data cache refill",
    "PublicDescription": "Level 2 data cache refill"
  } ,
  {
    "EventCode": "0x18",
    "EventName": "L2D_CACHE_WB",
    "BriefDescription":  "Level 2 data cache Write-Back",
    "PublicDescription": "Level 2 data cache Write-Back"
  } ,
  {
    "EventCode": "0x19",
    "EventName": "BUS_ACCESS",
    "BriefDescription":  "Bus access",
    "PublicDescription": "Bus access"
  } ,
  {
    "EventCode": "0x1A",
    "EventName": "MEMORY_ERROR",
    "BriefDescription":  "Local memory error",
    "PublicDescription": "Local memory error"
  } ,
  {
    "EventCode": "0x1B",
    "EventName": "INST_SPEC",
    "BriefDescription":  "Operation speculatively executed",
    "PublicDescription": "Operation speculatively executed"
  } ,
  {
    "EventCode": "0x1C",
    "EventName": "TTBR_WRITE_RETIRED",
    "BriefDescription":  "Instruction architecturally executed (condition check pass) - Write to translation table base",
    "PublicDescription": "Instruction architecturally executed (condition check pass) - Write to translation table base"
  } ,
  {
    "EventCode": "0x1D",
    "EventName": "BUS_CYCLES",
    "BriefDescription":  "Bus cycle",
    "PublicDescription": "Bus cycle"
  } ,
  {
    "EventCode": "0x1E",
    "EventName": "CHAIN",
    "BriefDescription":  "Odd performance counter chain mode",
    "PublicDescription": "Odd performance counter chain mode"
  } ,

  {
    "EventCode": "0x40",
    "EventName": "L1D_CACHE_LD",
    "BriefDescription":  "Level 1 data cache access - Read",
    "PublicDescription": "Level 1 data cache access - Read"
  } ,
  {
    "EventCode": "0x41",
    "EventName": "L1D_CACHE_ST",
    "BriefDescription":  "Level 1 data cache access - Write",
    "PublicDescription": "Level 1 data cache access - Write"
  } ,
  {
    "EventCode": "0x42",
    "EventName": "L1D_CACHE_REFILL_LD",
    "BriefDescription":  "Level 1 data cache refill - Read",
    "PublicDescription": "Level 1 data cache refill - Read"
  } ,
  {
    "EventCode": "0x43",
    "EventName": "L1D_CACHE_REFILL_ST",
    "BriefDescription":  "Level 1 data cache refill - Write",
    "PublicDescription": "Level 1 data cache refill - Write"
  } ,
  {
    "EventCode": "0x46",
    "EventName": "L1D_CACHE_WB_VICTIM",
    "BriefDescription":  "Level 1 data cache Write-back - Victim",
    "PublicDescription": "Level 1 data cache Write-back - Victim"
  } ,
  {
    "EventCode": "0x47",
    "EventName": "L1D_CACHE_WB_CLEAN",
    "BriefDescription":  "Level 1 data cache Write-back - Cleaning and coherency",
    "PublicDescription": "Level 1 data cache Write-back - Cleaning and coherency"
  } ,
  {
    "EventCode": "0x48",
    "EventName": "L1D_CACHE_INVAL",
    "BriefDescription":  "Level 1 data cache invalidate",
    "PublicDescription": "Level 1 data cache invalidate"
  } ,
  {
    "EventCode": "0x4C",
    "EventName": "L1D_TLB_REFILL_LD",
    "BriefDescription":  "Level 1 data TLB refill - Read",
    "PublicDescription": "Level 1 data TLB refill - Read"
  } ,
  {
    "EventCode": "0x4D",
    "EventName": "L1D_TLB_REFILL_ST",
    "BriefDescription":  "Level 1 data TLB refill - Write",
    "PublicDescription": "Level 1 data TLB refill - Write"
  } ,
  {
    "EventCode": "0x50",
    "EventName": "L2D_CACHE_LD",
    "BriefDescription":  "Level 2 data cache access - Read",
    "PublicDescription": "Level 2 data cache access - Read"
  } ,
  {
    "EventCode": "0x51",
    "EventName": "L2D_CACHE_ST",
    "BriefDescription":  "Level 2 data cache access - Write",
    "PublicDescription": "Level 2 data cache access - Write"
  } ,
  {
    "EventCode": "0x52",
    "EventName": "L2D_CACHE_REFILL_LD",
    "BriefDescription":  "Level 2 data cache refill - Read",
    "PublicDescription": "Level 2 data cache refill - Read"
  } ,
  {
    "EventCode": "0x53",
    "EventName": "L2D_CACHE_REFILL_ST",
    "BriefDescription":  "Level 2 data cache refill - Write",
    "PublicDescription": "Level 2 data cache refill - Write"
  } ,
  {
    "EventCode": "0x56",
    "EventName": "L2D_CACHE_WB_VICTIM",
    "BriefDescription":  "Level 2 data cache Write-back - Victim",
    "PublicDescription": "Level 2 data cache Write-back - Victim"
  } ,
  {
    "EventCode": "0x57",
    "EventName": "L2D_CACHE_WB_CLEAN",
    "BriefDescription":  "Level 2 data cache Write-back - Cleaning and coherency",
    "PublicDescription": "Level 2 data cache Write-back - Cleaning and coherency"
  } ,
  {
    "EventCode": "0x58",
    "EventName": "L2D_CACHE_INVAL",
    "BriefDescription":  "Level 2 data cache invalidate",
    "PublicDescription": "Level 2 data cache invalidate"
  } ,
  {
    "EventCode": "0x60",
    "EventName": "BUS_ACCESS_LD",
    "BriefDescription":  "Bus access - Read",
    "PublicDescription": "Bus access - Read"
  } ,
  {
    "EventCode": "0x61",
    "EventName": "BUS_ACCESS_ST",
    "BriefDescription":  "Bus access - Write",
    "PublicDescription": "Bus access - Write"
  } ,
  {
    "EventCode": "0x62",
    "EventName": "BUS_ACCESS_SHARED",
    "BriefDescription":  "Bus access - Normal",
    "PublicDescription": "Bus access - Normal"
  } ,
  {
    "EventCode": "0x63",
    "EventName": "BUS_ACCESS_NOT_SHARED",
    "BriefDescription":  "Bus access - Not normal",
    "PublicDescription": "Bus access - Not normal"
  } ,
  {
    "EventCode": "0x64",
    "EventName": "BUS_ACCESS_NORMAL",
    "BriefDescription":  "Bus access - Normal",
    "PublicDescription": "Bus access - Normal"
  } ,
  {
    "EventCode": "0x65",
    "EventName": "BUS_ACCESS_PERIPH",
    "BriefDescription":  "Bus access - Peripheral",
    "PublicDescription": "Bus access - Peripheral"
  } ,
  {
    "EventCode": "0x66",
    "EventName": "MEM_ACCESS_LD",
    "BriefDescription":  "Data memory access - Read",
    "PublicDescription": "Data memory access - Read"
  } ,
  {
    "EventCode": "0x67",
    "EventName": "MEM_ACCESS_ST",
    "BriefDescription":  "Data memory access - Write",
    "PublicDescription": "Data memory access - Write"
  } ,
  {
    "EventCode": "0x68",
    "EventName": "UNALIGNED_LD_SPEC",
    "BriefDescription":  "Unaligned access - Read",
    "PublicDescription": "Unaligned access - Read"
  } ,
  {
    "EventCode": "0x69b",
    "EventName": "UNALIGNED_ST_SPEC",
    "BriefDescription":  "Unaligned access - Write",
    "PublicDescription": "Unaligned access - Write"
  } ,
  {
    "EventCode": "0x6Ab",
    "EventName": "UNALIGNED_LDST_SPEC",
    "BriefDescription": "Unaligned access",
    "PublicDescription": "Unaligned access"
  } ,
  {
    "EventCode": "0x6C",
    "EventName": "LDREX_SPEC",
    "BriefDescription":  "Exclusive operation speculatively executed - LDREX",
    "PublicDescription": "Exclusive operation speculatively executed - LDREX"
  } ,
  {
    "EventCode": "0x6D",
    "EventName": "STREX_PASS_SPEC",
    "BriefDescription":  "Exclusive instruction speculatively executed - STREX pass",
    "PublicDescription": "Exclusive instruction speculatively executed - STREX pass"
  } ,
  {
    "EventCode": "0x6E",
    "EventName": "STREX_FAIL_SPEC",
    "BriefDescription":  "Exclusive operation speculatively executed - STREX fail",
    "PublicDescription": "Exclusive operation speculatively executed - STREX fail"
  } ,
  {
    "EventCode": "0x70",
    "EventName": "LD_SPEC",
    "BriefDescription":  "Operation speculatively executed - Load",
    "PublicDescription": "Operation speculatively executed - Load"
  } ,
  {
    "EventCode": "0x71",
    "EventName": "ST_SPEC",
    "BriefDescription":  "Operation speculatively executed - Store",
    "PublicDescription": "Operation speculatively executed - Store"
  } ,
  {
    "EventCode": "0x72",
    "EventName": "LDST_SPEC",
    "BriefDescription":  "Operation speculatively executed - Load or store",
    "PublicDescription": "Operation speculatively executed - Load or store"
  } ,
  {
    "EventCode": "0x73",
    "EventName": "DP_SPEC",
    "BriefDescription":  "Operation speculatively executed - Integer data processing",
    "PublicDescription": "Operation speculatively executed - Integer data processing"
  } ,
  {
    "EventCode": "0x74",
    "EventName": "ASE_SPEC",
    "BriefDescription":  "Operation speculatively executed - Advanced SIMD",
    "PublicDescription": "Operation speculatively executed - Advanced SIMD"
  } ,
  {
    "EventCode": "0x75",
    "EventName": "VFP_SPEC",
    "BriefDescription":  "Operation speculatively executed - VFP",
    "PublicDescription": "Operation speculatively executed - VFP"
  } ,
  {
    "EventCode": "0x76",
    "EventName": "PC_WRITE_SPEC",
    "BriefDescription":  "Operation speculatively executed - Software change of the PC",
    "PublicDescription": "Operation speculatively executed - Software change of the PC"
  } ,
  {
    "EventCode": "0x77",
    "EventName": "CRYPTO_SPEC",
    "BriefDescription":  "Operation speculatively executed, crypto data processing",
    "PublicDescription": "Operation speculatively executed, crypto data processing"
  } ,
  {
    "EventCode": "0x78",
    "EventName": "BR_IMMED_SPEC",
    "BriefDescription":  "Branch speculatively executed - Immediate branch",
    "PublicDescription": "Branch speculatively executed - Immediate branch"
  } ,
  {
    "EventCode": "0x79",
    "EventName": "BR_RETURN_SPEC",
    "BriefDescription":  "Branch speculatively executed - Procedure return",
    "PublicDescription": "Branch speculatively executed - Procedure return"
  } ,
  {
    "EventCode": "0x7A",
    "EventName": "BR_INDIRECT_SPEC",
    "BriefDescription": "Branch speculatively executed - Indirect branch",
    "PublicDescription": "Branch speculatively executed - Indirect branch"
  } ,
  {
    "EventCode": "0x7C",
    "EventName": "ISB_SPEC",
    "BriefDescription":  "Barrier speculatively executed - ISB",
    "PublicDescription": "Barrier speculatively executed - ISB"
  } ,
  {
    "EventCode": "0x7D",
    "EventName": "DSB_SPEC",
    "BriefDescription":  "Barrier speculatively executed - DSB",
    "PublicDescription": "Barrier speculatively executed - DSB"
  } ,
  {
    "EventCode": "0x7E",
    "EventName": "DMB_SPEC",
    "BriefDescription":  "Barrier speculatively executed - DMB",
    "PublicDescription": "Barrier speculatively executed - DMB"
  } ,
  {
    "EventCode": "0x81",
    "EventName": "EXC_UNDEF",
    "BriefDescription":  "Exception taken, other synchronous",
    "PublicDescription": "Exception taken, other synchronous"
  } ,
  {
    "EventCode": "0x82",
    "EventName": "EXC_SVC",
    "BriefDescription":  "Exception taken, Supervisor Call",
    "PublicDescription": "Exception taken, Supervisor Call"
  } ,
  {
    "EventCode": "0x83",
    "EventName": "EXC_PABORT",
    "BriefDescription":  "Exception taken, Instruction Abort",
    "PublicDescription": "Exception taken, Instruction Abort"
  } ,
  {
    "EventCode": "0x84",
    "EventName": "EXC_DABORT",
    "BriefDescription":  "Exception taken, Data Abort or SError",
    "PublicDescription": "Exception taken, Data Abort or SError"
  } ,
  {
    "EventCode": "0x86",
    "EventName": "EXC_IRQ",
    "BriefDescription":  "Exception taken, IRQ",
    "PublicDescription": "Exception taken, IRQ"
  } ,
  {
    "EventCode": "0x87",
    "EventName": "EXC_FIQ",
    "BriefDescription":  "Exception taken, FIQ",
    "PublicDescription": "Exception taken, FIQ"
  } ,
  {
    "EventCode": "0x88",
    "EventName": "EXC_SMC",
    "BriefDescription":  "Exception taken, Secure Monitor Call",
    "PublicDescription": "Exception taken, Secure Monitor Call"
  } ,
  {
    "EventCode": "0x8A",
    "EventName": "EXC_HVC",
    "BriefDescription":  "Exception taken, Hypervisor Call",
    "PublicDescription": "Exception taken, Hypervisor Call"
  } ,
  {
    "EventCode": "0x8B",
    "EventName": "EXC_TRAP_PABORT",
    "BriefDescription":  "Exception taken, Instruction Abort not taken locally",
    "PublicDescription": "Exception taken, Instruction Abort not taken locally"
  } ,
  {
    "EventCode": "0x8C",
    "EventName": "EXC_TRAP_DABORT",
    "BriefDescription":  "Exception taken, Data Abort, or SError not taken locally",
    "PublicDescription": "Exception taken, Data Abort, or SError not taken locally"
  } ,
  {
    "EventCode": "0x8D",
    "EventName": "EXC_TRAP_OTHER",
    "BriefDescription":  "Exception taken – Other traps not taken locally",
    "PublicDescription": "Exception taken – Other traps not taken locally"
  } ,
  {
    "EventCode": "0x8E",
    "EventName": "EXC_TRAP_IRQ",
    "BriefDescription":  "Exception taken, IRQ not taken locally",
    "PublicDescription": "Exception taken, IRQ not taken locally"
  } ,
  {
    "EventCode": "0x8F",
    "EventName": "EXC_TRAP_FIQ",
    "BriefDescription":  "Exception taken, FIQ not taken locally",
    "PublicDescription": "Exception taken, FIQ not taken locally"
  } ,
  {
    "EventCode": "0x90",
    "EventName": "RC_LD_SPEC",
    "BriefDescription":  "Release consistency instruction speculatively executed – Load-Acquire",
    "PublicDescription": "Release consistency instruction speculatively executed – Load-Acquire"
  } ,
  {
    "EventCode": "0x91",
    "EventName": "RC_ST_SPEC",
    "BriefDescription":  "Release consistency instruction speculatively executed – Store-Release",
    "PublicDescription": "Release consistency instruction speculatively executed – Store-Release"
  }
];

