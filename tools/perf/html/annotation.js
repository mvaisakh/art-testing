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

var annotation_color_table = [
  "black",
  "green",
  "orange",
  "red",
]

function onLoadData(data) {
  var content = document.getElementById("content");
  data = data.split("\n");
  var max_percentage = 0;
  var percentage = [];
  for (var i = 0; i < data.length; i++) {
    var current_percentage = data[i].split(":")[0].split("%") * 1;
    percentage[i] = isNaN(current_percentage) ? 0 : current_percentage;
    max_percentage = max_percentage > percentage[i] ? max_percentage : percentage[i];
  }
  for (var i = 0; i < data.length; i++) {
    var pre = document.createElement("pre");
    pre.textContent = data[i];
    var color_index = Math.floor(percentage[i] / max_percentage * annotation_color_table.length);
    color_index = color_index >= annotation_color_table.length ? color_index - 1 : color_index;
    pre.style.color = annotation_color_table[color_index];
    if (color_index > 0) {
      pre.style.fontWeight = "bold";
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
