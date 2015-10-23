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

// Load annotation result and format the lines with different color.

var instruction_color_table = [
  "black",
  "green",
  "orange",
  "red",
]
var non_instruction_color = "blue";

function onLoadData(data) {
  var content = document.getElementById("content");
  data = data.split("\n");
  var max_percentage = 0;
  var percentage = [];
  for (var i = 0; i < data.length; i++) {
    var current_percentage = data[i].split(":")[0].split("%")[0];
    if (current_percentage.search(/^\s*[0-9|\.]+\s*$/) < 0) {
      percentage[i] = NaN;
    } else {
      percentage[i] = current_percentage * 1;
    }
    // max_percentage < percentage[i] is false if percentage[i] is NaN.
    max_percentage = max_percentage < percentage[i] ? percentage[i] : max_percentage;
  }
  for (var i = 0; i < data.length; i++) {
    var pre = document.createElement("pre");
    pre.textContent = data[i];
    if (isNaN(percentage[i])) {
      pre.style.color = non_instruction_color;
    } else {
      var color_index = Math.floor(percentage[i] / max_percentage * instruction_color_table.length);
      color_index = color_index >= instruction_color_table.length ? color_index - 1 : color_index;
      pre.style.color = instruction_color_table[color_index];
      if (color_index > 0) {
        pre.style.fontWeight = "bold";
      }
    }
    content.appendChild(pre);
  }
}

function init() {
  var title = 'Annotation of "' + Utils.getURLParam("event") + '" in "' + Utils.getURLParam("hotspot") + '" of "' + Utils.getURLParam("bench") + '"';
  document.getElementsByTagName("title")[0].innerHTML = title;
  document.getElementById("title").innerHTML = title;

  Utils.loadData(Utils.getURLParam("data"), onLoadData);
}
