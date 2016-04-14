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

// Fill hotspot table for hotspot.html.

var hotspot_name;
var bench_name;
var events;
var cycles_rate;
var events_table = [];
var unknown_event = {
  EventCode: "UnknownEvent",
  EventName: "UnknownEvent",
  BriefDescription: "UnknownEvent",
  PublicDescription: "UnknownEvent",
}

function getEventDescription(event_code) {
  event_code = event_code.toUpperCase();
  if (event_code.search(/^R[0-9|A-F]*$/) == 0) {
    event_code = event_code.replace(/^R/,"0X");
  }
  for (var i = 0; i < events_table.length; i++) {
    if (events_table[i].EventCode.toUpperCase() == event_code) {
      return events_table[i];
    }
  }
  return unknown_event;
}

function resetTable() {
  var threshold = document.getElementById("threshold").value;
  var html = "";
  var file_prefix = "../perf-out/" + bench_name + "/";
  for (var i = 0; i < events.length; i++) {
    html += "<tr>";
    var event_record = getEventDescription(events[i].name);
    event_name = event_record.EventCode == event_record.EventName ? event_record.EventName
               : event_record.EventName + " (" + event_record.EventCode + ")";
    var event_description = event_record.PublicDescription;
    var html_event_name = event_name;
    if (event_name == "cycles") {
      html_event_name ='<font color="green"><b>REFERENCE EVENT:<br>' + event_name + "</b></font>";
    } else if (events[i].rate > Math.pow(cycles_rate/100, 1/threshold) * 100) {
      html_event_name ='<font color="red"><b>' + event_name + "</b></font>";
    }
    html += '<td class="benchmarkTableData">' + html_event_name + "</td>";
    html += '<td class="benchmarkTableData"><a href="annotation.html?'
          + 'event=' + events[i].name + '&hotspot=' + hotspot_name + '&bench=' + bench_name + '&data=' + file_prefix + events[i].file
          + '" title="' + events[i].file + '">'  + events[i].rate + "%</a></td>";
    html += '<td class="benchmarkTableData">' + event_description + "</td>";
    html += '<td class="benchmarkTableData">' + events[i].total + "</td>";

    html += "</tr>";
  }
  document.getElementById("benchs").innerHTML = html;
}

function compare_events(a, b) {
  return b.rate - a.rate;
}

function init() {
  // Initialize events map.
  events_table = events_table.concat(events_generic);
  events_table = events_table.concat(events_pmu);

  // Set document title.
  var bench_index = Utils.getURLParam("bench_index");
  var hotspot_index = Utils.getURLParam("hotspot_index");
  hotspot_name = bench_result[bench_index].hotspots[hotspot_index].name;
  bench_name = bench_result[bench_index].name;
  var title = hotspot_name + " in " + bench_name;
  document.getElementsByTagName("title")[0].innerHTML = title;
  document.getElementById("title").innerHTML = title;

  // Sort events.
  events = bench_result[bench_index].hotspots[hotspot_index].events;
  cycles_rate = events[0].rate;
  events.sort(compare_events);

  // Fill table.
  document.getElementById("threshold").value = Utils.getURLParam("threshold") ? Utils.getURLParam("threshold") : 2;
  resetTable();
}
