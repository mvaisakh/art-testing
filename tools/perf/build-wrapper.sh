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

# Build bench apk and push it to target device.
safe cd $UBENCH_ROOT
safe ./build.sh -t
safe $ADB push $UBENCH_LOCAL $UBENCH_REMOTE
safe cd -

# Clear dalvik cache.
safe $ADB shell rm -f $UBENCH_REMOTE_DIR/dalvik-cache/*/$UBENCH_REMOTE_CACHE_FILE

# Note: `test -e` is not used, because adb seems not return the correct exit value.
# adb shell ls output error message to stdout, so use grep to test if file exists.
if [ "$($ADB shell ls /system/lib*/*art-disassembler* | grep libart-disassembler)" != "" ] ; then
HAS_ART_DISASSEMBLER=true
else
HAS_ART_DISASSEMBLER=false
fi

for dalvikvm in $REMOTE_DALVIKVMS ; do
  vm=$(basename $dalvikvm)
  $HAS_ART_DISASSEMBLER && VM_CFG_FLAG="-Xcompiler-option -j1 -Xcompiler-option --dump-cfg=$UBENCH_REMOTE_DIR/bench.${vm}.cfg"
# Trigger the build with debug symbols generated.
  safe $ADB shell ANDROID_DATA=$UBENCH_REMOTE_DIR DEX_LOCATION=$UBENCH_REMOTE_DIR $vm -Xcompiler-option -g $VM_CFG_FLAG -cp $UBENCH_REMOTE org.linaro.bench.RunBench --help > /dev/null
  safe mkdir -p $CFG_FOLDER
# Pull CFG file if can be generated or create an empty one.
  if $HAS_ART_DISASSEMBLER ; then
    safe $ADB pull $UBENCH_REMOTE_DIR/bench.${vm}.cfg $CFG_FOLDER
  else
    safe touch $CFG_FOLDER/bench.${vm}.cfg
  fi
# Extract disassembly information from the CFG file.
  safe $SCRIPT_PATH/shrink-cfg.sh disassembly < $CFG_FOLDER/bench.${vm}.cfg > $CFG_FOLDER/bench.${vm}.disassembly
done

# Symbolize boot.oat.
print_info Symbolizing boot.oat
$ADB shell ls -1 $UBENCH_REMOTE_DIR/dalvik-cache/*/*boot.oat | xargs -n 1 bash $ANDROID_BUILD_TOP/art/tools/symbolize.sh

# Pull compiled benchmark.
print_info Pulling compiled benchmark
$ADB shell ls -1 $UBENCH_REMOTE_DIR/dalvik-cache/*/$UBENCH_REMOTE_CACHE_FILE | xargs -n 1 -i $ADB pull {} $ANDROID_PRODUCT_OUT/symbols/{}

# Copy files or make symbolic links into a folder structure which matches the
# debug information. It might be a bit ugly to do so. But there are issues with
# '--prefix' option of aarch64-linux-android-objdump.
# Note: Steps to identify the issue:
# 1. Take a look at how debug information is encoded in the symbol files.
#  readelf --debug-dump $ANDROID_PRODUCT_OUT/symbols/system/bin/dalvikvm64
# 2. Try to dump the content with disassebmly and source lines.
#  aarch64-linux-android-objdump --prefix=$ANDROID_BUILD_TOP --prefix-strip=3 -dS $ANDROID_PRODUCT_OUT/symbols/system/bin/dalvikvm64
#  No source code intermixed.
# 3. Try the host version.
#  objdump --prefix=$ANDROID_BUILD_TOP --prefix-strip=3 -dS $ANDROID_BUILD_TOP/out/host/linux-x86/bin/dalvikvm64
#  Source code intermixed perfectly.
safe mkdir -p $STRUCTURED_SOURCE_FOLDER
safe cp -rt $STRUCTURED_SOURCE_FOLDER $UBENCH_ROOT/benchmarks $UBENCH_ROOT/framework/*
ls -1 $ANDROID_BUILD_TOP | safe xargs -n 1 -i ln -s $ANDROID_BUILD_TOP/{} $STRUCTURED_SOURCE_FOLDER/

