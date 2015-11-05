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

// Load and display perf report.

function isComment(line) {
  return line.search(/^\s*#/) == 0;
}

function isSymbol(line) {
  return line.search(/^\s*\d+\.+\d+%/) == 0;
}
function isDetail(line) {
  return !isComment(line) && !isSymbol(line);
}

function toggleDetail(hotspot_index) {
  var detail = document.getElementById("hotspot_" + hotspot_index);
  var switcher = document.getElementById("toggleDetail_" + hotspot_index);
  if (detail.style.display == "none") {
    detail.style.display = "";
    switcher.innerHTML = "Hide";
  } else {
    detail.style.display = "none";
    switcher.innerHTML = "Show";
  }
}

function onLoadData(data) {
  var content = document.getElementById("content");
  content.innerHTML = "";
  data = data.split("\n");
  var num_of_hotspots = Utils.getURLParam("num_of_hotspots");
  var bench_index = Utils.getURLParam("bench_index");

  var kStatusReadLine = 0;
  var kStatusReadDetail = 1;

  var status = 0;
  var hotspot_index = 0;
  var detail = null;

  for (var i = 0; i < data.length; i++) {
    var line = data[i];
    var pre = document.createElement("pre");
    pre.textContent = line;
    switch (status) {
    case kStatusReadLine:
      if (isSymbol(line)) {
        status = kStatusReadDetail;
        hotspot_index++;
        if (hotspot_index <= num_of_hotspots) {
          pre.innerHTML = ' <a href="hotspot.html?bench_index=' + bench_index
            + '&hotspot_index=' + hotspot_index + '">Annotate</a>' + pre.innerHTML;
        }
        pre.innerHTML = '<a id="toggleDetail_' + hotspot_index
          + '" href="javascript:toggleDetail(' + hotspot_index
          + ');">Show</a>' + pre.innerHTML;
        detail = document.createElement("div");
        detail.id = "hotspot_" + hotspot_index;
        detail.style.display = "none";
      }
      content.appendChild(pre);
      break;
    case kStatusReadDetail:
      if (isDetail(line)) {
        detail.appendChild(pre);
      } else {
        status = kStatusReadLine;
        content.appendChild(detail);
        detail = null;
        i--;
      }
      break;
    default:
      break;
    }
  }
}

function init() {
  var title = "Report of " + Utils.getURLParam("title");
  document.getElementsByTagName("title")[0].innerHTML = title;
  document.getElementById("title").innerHTML = title;

  Utils.loadData(Utils.getURLParam("data"), onLoadData);
}
