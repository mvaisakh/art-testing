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

usage() {
  echo "Usage: $(basename "$0") \"<regex to match passname> ...\""
  echo "  Read CFG from stdin and write HIRs in matched passes to stdout."
  echo "Example:"
  echo "  $(basename "$0") disassebmly < perf-out/cfg/bench.dalvikvm64.cfg > perf-out/cfg/bench.dalvikvm64.disassembly"
}

if [ $# -lt 1 ] ; then
  usage
  exit
fi

tmp_prefix=/tmp/
tmp_suffix=.tmp
sort_file=${tmp_prefix}cfg_sort${tmp_suffix}

handle_compilation() {
  local line
  while read line ; do
    if [ "$line" = "end_compilation" ] ; then
      break
    elif [[ "$line" =~ ^method ]] ; then
      echo $line
    fi
  done
}

handle_HIR() {
  local line
  while read line ; do
    if [ "$line" = "end_HIR" ] ; then
      break
    elif [ ! "$line" = "<|@" ] ; then
      if [[ "$line" =~ ^0x[0-9|a-f]+: ]] ; then
        echo "        "${line%%<|@}
      else
        echo "      "${line%%<|@}
      fi
    fi
  done
}

handle_block() {
  local line
  while read line ; do
    if [ "$line" = "end_block" ] ; then
      break
    elif [ "$line" = "begin_HIR" ] ; then
      handle_HIR
    elif [[ "$line" =~ ^name ]] ; then
      echo "    "block $line
    fi
  done
}

handle_cfg() {
  local pass_name
  read pass_name
  if [[ ! "$pass_name" =~ ^name ]] ; then
    echo Un-recoginized CFG file format >&2
    exit 1
  fi

  local dump_pass=false
  local pass
  for pass in $passes ; do
    if [[ "$pass_name" =~ $pass ]] ; then
      echo "  "$pass_name
      dump_pass=true
    fi
  done

# Blocks in CFG file are not necessarily sorted in code generation order. And
# it is difficult for us to reproduce the code generation order. Just try to
# sort it by assembly offsets, so that the assembly will be in the right order
# and the HIRs will be almost in the right order.
  echo > $sort_file
  local block_index=0
  local last_offset=0x00000000
  while read line ; do
    if [ "$line" = "end_cfg" ] ; then
      break
    elif $dump_pass ; then
      if [ "$line" = "begin_block" ] ; then
        local block_file=${tmp_prefix}block_${block_index}${tmp_suffix}
        handle_block > $block_file
        local offset=$(grep -m 1 -o -E "^\s*0x[0-9|a-f]+" $block_file)
        # Set offset to last block's offset if there is no assembly found in
        # current block. And the block will be put right after the previous one
        # with stable sort.
        if [ "$offset" = "" ] ; then
          offset=$last_offset
        fi
        last_offset=$offset
        echo $offset $block_file >> $sort_file
        block_index=$(($block_index + 1))
      fi
    fi
  done

# Append sorted blocks to the output.
  for block in $(sort -s -k 1 $sort_file | cut -d " " -f 2) ; do
    cat $block
  done
}

main() {
  local line
  while read line ; do
    if [ "$line" = "begin_compilation" ] ; then
      handle_compilation
    elif [ "$line" = "begin_cfg" ] ; then
      handle_cfg
    fi
  done
}

passes="$@"
main
