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

// Load and display console output of micro-benchmark.

function onLoadData(data) {
  var pre = document.createElement("pre");
  pre.textContent = data;
  document.body.appendChild(pre);
}

function init() {
  var title = "Console output of " + Utils.getURLParam("title");
  document.getElementsByTagName("title")[0].innerHTML = title;
  document.getElementById("title").innerHTML = title;

  Utils.loadData(Utils.getURLParam("data"), onLoadData);
}
