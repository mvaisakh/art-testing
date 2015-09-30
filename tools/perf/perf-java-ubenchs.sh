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

src_folder=../../benchmarks
out_folder=ubenchs-out
dex_file_name=ubenchs.jar
js_file_name=ubenchs.js

top=$(pwd)
src_folder=$top/$src_folder
out_folder=$top/$out_folder
plan_src_folder=$out_folder/plan_src
class_folder=$out_folder/class
cfg_folder=$out_folder/cfg
perf_folder=$out_folder/perf
remote_tmp_folder=/data/local/tmp
android_symbols_folder=$ANDROID_PRODUCT_OUT/symbols
kernel_symbol_file=$top/vmlinux
symbol_file=data@local@tmp@$dex_file_name@classes.dex

# Perf flags.
symbol_flag="-k $kernel_symbol_file --symfs $android_symbols_folder"
# aarch64 version supports both aarch32 and aarch64.
binutils_flag=--objdump=aarch64-linux-android-objdump

# Print warning message if vmlinux does not exist.
if [ ! -f $kernel_symbol_file ] ; then
  echo WARNING : Kernel symbol file not found! Kernel symbols will be missing.
fi

# Create output folders.
mkdir -p $out_folder $plan_src_folder $class_folder $cfg_folder $perf_folder

# Build dex file.
javac -g -cp $src_folder $src_folder/*.java -d $class_folder
dx --dex --debug --output=$out_folder/$dex_file_name $class_folder

# Symbolize boot.oat on target.
./symbolize.sh /data/dalvik-cache/arm64/system@framework@boot.oat
./symbolize.sh /data/dalvik-cache/arm/system@framework@boot.oat

# Push to target.
adb push $out_folder/$dex_file_name $remote_tmp_folder/$dex_file_name

# Get file names.
cd $src_folder
names=$(ls -1 *.java)
cd -

# Create js file which contains all benchmark names.
echo "ubenchs = [" > $perf_folder/$js_file_name
for name in $names ; do
  echo '  "'${name%.java}'",' >> $perf_folder/$js_file_name
done
echo "];" >> $perf_folder/$js_file_name

# Copy java source file to plan_src_folder. The oat files do not contain source path. We need to annotate from the plan_src_folder to interleave source code with disassembly.
find $src_folder -name "*.java" -exec cp -f {} $plan_src_folder/ \;

# Execute each benchmark
for arch in 32 64 ; do
  if [ "$arch" = "32" ] ; then
    remote_cache_folder=/data/dalvik-cache/arm
  else
    remote_cache_folder=/data/dalvik-cache/arm64
  fi
# Remove existing oat file.
  adb shell rm -f $remote_cache_folder/$symbol_file
# Execute dalvikvm with compiler option -j1 -g , --dump-cfg=$remote_tmp_folder/${dex_file_name}.${arch}.cfg and the other default settings.
  adb shell dalvikvm$arch -Xcompiler-option -j1 -Xcompiler-option -g -Xcompiler-option --dump-cfg=$remote_tmp_folder/${dex_file_name}.${arch}.cfg -cp $remote_tmp_folder/$dex_file_name
# Pull the symbol file.
  adb pull $remote_cache_folder/$symbol_file $android_symbols_folder/$remote_cache_folder/$symbol_file
# Pull optimizing compiler generated CFG file.
  adb pull $remote_tmp_folder/${dex_file_name}.${arch}.cfg $cfg_folder/${dex_file_name}.${arch}.cfg
  for name in $names ; do
    bench_name=${name%.java}
    echo ===== $bench_name =====
# Execute the benchmark with perf.
    adb shell perf record -g -o $remote_tmp_folder/${bench_name}.perf${arch}.data dalvikvm$arch -Xcompiler-option -g -cp $remote_tmp_folder/$dex_file_name $bench_name
# Pull the perf data.
    adb pull $remote_tmp_folder/${bench_name}.perf${arch}.data $perf_folder/${bench_name}.perf${arch}.data
# Generate report.
    perf report $binutils_flag $symbol_flag -i $perf_folder/${bench_name}.perf${arch}.data > $perf_folder/${bench_name}.perf${arch}.report 2>/dev/null
# Annotate the hotspot.
    # Get the first record in report file, which should be the hotest function.
    hotspot=$(grep -E '\[.\]' $perf_folder/${bench_name}.perf${arch}.report | head -n 1 | sed 's/.*\[.\]//')
    # Remove leading whitespace.
    hotspot="${hotspot#"${hotspot%%[![:space:]]*}"}"
    # Remove tailing whitespace.
    hotspot="${hotspot%"${hotspot##*[![:space:]]}"}"
    echo hotspot '"'$hotspot'"'
    # Annotate.
    cd $plan_src_folder
    perf annotate $binutils_flag $symbol_flag -i $perf_folder/${bench_name}.perf${arch}.data "$hotspot" > $perf_folder/${bench_name}.perf${arch}.annotate
    cd -
# Generate script.
    perf script $symbol_flag -i $perf_folder/${bench_name}.perf${arch}.data > $perf_folder/${bench_name}.perf${arch}.script
# Stack collapse.
    ./FlameGraph/stackcollapse-perf.pl $perf_folder/${bench_name}.perf${arch}.script > $perf_folder/${bench_name}.perf${arch}.stackcollapse 2>/dev/null
# Flame graph.
    ./FlameGraph/flamegraph.pl $perf_folder/${bench_name}.perf${arch}.stackcollapse > $perf_folder/${bench_name}.perf${arch}.html
  done
done
