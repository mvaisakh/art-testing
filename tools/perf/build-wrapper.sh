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
safe adb push $UBENCH_LOCAL $UBENCH_REMOTE
safe cd -

# Clear dalvik cache.
safe adb shell rm -f $UBENCH_REMOTE_DIR/dalvik-cache/*/$UBENCH_REMOTE_CACHE_FILE

for dalvikvm in $REMOTE_DALVIKVMS ; do
  vm=$(basename $dalvikvm)
# Trigger the build with debug symbols generated.
  safe adb shell ANDROID_DATA=$UBENCH_REMOTE_DIR DEX_LOCATION=$UBENCH_REMOTE_DIR $vm -Xcompiler-option -g -Xcompiler-option -j1 -Xcompiler-option --dump-cfg=$UBENCH_REMOTE_DIR/bench.${vm}.cfg -cp $UBENCH_REMOTE org.linaro.bench.RunBench --help > /dev/null
# Pull CFG file.
  safe mkdir -p $CFG_FOLDER
  safe adb pull $UBENCH_REMOTE_DIR/bench.${vm}.cfg $CFG_FOLDER
# Extract disassembly information from the CFG file.
  safe $SCRIPT_PATH/shrink-cfg.sh disassembly < $CFG_FOLDER/bench.${vm}.cfg > $CFG_FOLDER/bench.${vm}.disassembly
done

# Symbolize boot.oat.
print_info Symbolizing boot.oat
adb shell ls -1 $UBENCH_REMOTE_DIR/dalvik-cache/*/*boot.oat | xargs -n 1 bash $ANDROID_BUILD_TOP/art/tools/symbolize.sh

# Pull compiled benchmark.
print_info Pulling compiled benchmark
adb shell ls -1 $UBENCH_REMOTE_DIR/dalvik-cache/*/$UBENCH_REMOTE_CACHE_FILE | xargs -n 1 -i adb pull {} $ANDROID_PRODUCT_OUT/symbols/{}

# Copy files into a folder structure which matches the debug information.
safe mkdir -p $STRUCTURED_SOURCE_FOLDER
safe cp -rt $STRUCTURED_SOURCE_FOLDER $UBENCH_ROOT/benchmarks $UBENCH_ROOT/framework/*

