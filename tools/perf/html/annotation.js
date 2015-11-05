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

var annotation = undefined;
var disassembly = undefined;

var showNonInstruction;
var showInstruction;
var showHIR;
var showHIRDisassembly;

function setStyle(selector, styleName, styleValue) {
  var nodes = document.querySelectorAll(selector);
  for (var i = 0; i < nodes.length; ++i) {
    nodes[i].style[styleName] = styleValue;
  }
}

function toggleNonInstruction() {
  showNonInstruction = ! showNonInstruction;
  setStyle(".non-instruction", "display", showNonInstruction ? "" : "none");
}

function toggleInstruction() {
  showInstruction = ! showInstruction;
  setStyle(".instruction", "display", showInstruction ? "" : "none");
}

function toggleHIR() {
  showHIR = ! showHIR;
  setStyle(".HIR", "display", showHIR ? "" : "none");
}

function toggleHIRDisassembly() {
  showHIRDisassembly = ! showHIRDisassembly;
  setStyle(".HIR-disassembly", "display", showHIRDisassembly ? "" : "none");
}

function resetShowHide() {
  showNonInstruction = true;
  showInstruction = true;
  showHIR = true;
  showHIRDisassembly = false;
  setStyle(".non-instruction", "display", showNonInstruction ? "" : "none");
  setStyle(".instruction", "display", showInstruction ? "" : "none");
  setStyle(".HIR", "display", showHIR ? "" : "none");
  setStyle(".HIR-disassembly", "display", showHIRDisassembly ? "" : "none");
}

function onLoadAnnotation(data) {
  annotation = data;
  if (annotation != undefined && disassembly != undefined) {
    createContent();
  }
}

function onLoadDisassembly(data) {
  disassembly = data;
  if (annotation != undefined && disassembly != undefined) {
    createContent();
  }
}

function getDisassemblyLines() {
  var disassembly_lines = disassembly.split("\n");
  var start_line = disassembly_lines.indexOf('method "' + Utils.getURLParam("hotspot") + '"');
  if (start_line < 0) {
    return [];
  }
  for (++start_line; start_line < disassembly_lines.length; ++start_line) {
    if (disassembly_lines[start_line].search(/^\s*name "disassembly/) >= 0) {
      ++start_line;
      break;
    }
  }
  if (start_line >= disassembly_lines.length) {
    return [];
  }
  var end_line = start_line;
  for (; end_line < disassembly_lines.length; ++end_line) {
    if (disassembly_lines[end_line].search(/^\s*method "/) >= 0) {
      break;
    }
  }
  return disassembly_lines.slice(start_line, end_line);
}

function createContent() {
  var content = document.getElementById("content");
  var annotation_lines = annotation.split("\n");
  var disassembly_lines = getDisassemblyLines();
  var max_percentage = 0;
  var percentage = [];
  for (var i = 0; i < annotation_lines.length; i++) {
    var current_percentage = annotation_lines[i].split(":")[0].split("%")[0];
    if (current_percentage.search(/^\s*[0-9|\.]+\s*$/) < 0) {
      percentage[i] = NaN;
    } else {
      percentage[i] = current_percentage * 1;
    }
    // max_percentage < percentage[i] is false if percentage[i] is NaN.
    max_percentage = max_percentage < percentage[i] ? percentage[i] : max_percentage;
  }
  disassembly_index = 0;
  for (var i = 0; i < annotation_lines.length; i++) {
    var pre = document.createElement("pre");
    pre.textContent = annotation_lines[i];
    if (isNaN(percentage[i])) {
      pre.className = "non-instruction";
    } else {
      // HIR.
      for (; disassembly_index < disassembly_lines.length; ++disassembly_index) {
        if (disassembly_lines[disassembly_index].search(/^\s*0x[0-9|a-f|A-F]+:/) < 0) {
          var HIR = document.createElement("pre");
          HIR.className = "HIR";
          HIR.textContent = disassembly_lines[disassembly_index];
          content.appendChild(HIR);
        } else {
          var HIR = document.createElement("pre");
          HIR.className = "HIR-disassembly";
          HIR.textContent = disassembly_lines[disassembly_index];
          content.appendChild(HIR);
          ++disassembly_index;
          break;
        }
      }
      // Annotation line.
      pre.className = "instruction";
      var color_index = Math.floor(percentage[i] / max_percentage * instruction_color_table.length);
      color_index = color_index >= instruction_color_table.length ? color_index - 1 : color_index;
      pre.style.color = instruction_color_table[color_index];
      if (color_index > 0) {
        pre.style.fontWeight = "bold";
      }
    }
    content.appendChild(pre);
  }
  resetShowHide();
}

function init() {
  var title = 'Annotation of "' + Utils.getURLParam("event") + '" in "' + Utils.getURLParam("hotspot") + '" of "' + Utils.getURLParam("bench") + '"';
  document.getElementsByTagName("title")[0].innerHTML = title;
  document.getElementById("title").innerHTML = title;
  Utils.loadData(Utils.getURLParam("data"), onLoadAnnotation);

  var disassembly_file = "../perf-out/cfg/bench." + Utils.getURLParam("bench").split("_")[1] + ".disassembly";
  Utils.loadData(disassembly_file, onLoadDisassembly);
}
