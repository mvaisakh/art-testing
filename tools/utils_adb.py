# Copyright (C) 2015 Linaro Limited. All rights received.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import os
import subprocess

import utils

default_remote_copy_path = '/data/local/tmp'

def pull(f, local_path, target = '<default>'):
    command = ['adb'] + (['-s', target] if target != '<default>' else []) + \
              ['pull', f, local_path]
    utils.VerbosePrint(' '.join(command))
    p = subprocess.Popen(command, stdout = subprocess.PIPE, stderr = subprocess.PIPE)
    return p.communicate()

def push(f, target_path = default_remote_copy_path, target = None):
    command = ['adb', 'push', f, target_path]
    if target != '<default>':
        command = ['adb', '-s', target, 'push', f, target_path]
    utils.VerbosePrint(' '.join(command))
    p = subprocess.Popen(command, stdout = subprocess.PIPE, stderr = subprocess.PIPE)
    return p.communicate()


# `command` is expected to be a string, not a list.
def shell(command_arg, target):
    # We need to quote the actual command in the text printed so it can be
    # copy-pasted and executed.
    command = ['adb', 'shell', command_arg]
    if target != '<default>':
        command = ['adb', '-s', target, 'shell', command_arg]
    utils.VerbosePrint(' '.join(command))
    p = subprocess.Popen(command, stdout = subprocess.PIPE, stderr = subprocess.PIPE)
    rc = p.wait()
    out, err = p.communicate()
    return rc, out, err
