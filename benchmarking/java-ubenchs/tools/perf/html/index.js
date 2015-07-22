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

function init() {
  var ubenchs_table = document.getElementById("ubenchs");
  var html = "";
  for (var i = 0; i < ubenchs.length; i++) {
    html += "<tr>";
    var bench_name = ubenchs[i];
    html += '<td class="benchmarkTableData">' + bench_name + "</td>";
    var perf_output_prefix = "../ubenchs-out/perf/" + bench_name + ".perf";
    var flamegraph_32 = perf_output_prefix + "32.html";
    html += '<td class="benchmarkTableData"><a href="flamegraph.html?data=' + flamegraph_32 + '" title="' + flamegraph_32 + '">view</a></td>';
    var report_32 = perf_output_prefix + "32.report";
    html += '<td class="benchmarkTableData"><a href="report.html?data=' + report_32 + '" title="' + report_32 + '">view</a>';
    html += '(<a href="rawview.html?data=' + report_32 + '" title="' + report_32 + '">raw</a>)</td>';
    var annotate_32 = perf_output_prefix + "32.annotate";
    html += '<td class="benchmarkTableData"><a href="annotation.html?data=' + annotate_32 + '" title="' + report_32 + '">view</a>';
    html += '(<a href="rawview.html?data=' + annotate_32 + '" title="' + report_32 + '">raw</a>)</td>';
    var flamegraph_64 = perf_output_prefix + "64.html";
    html += '<td class="benchmarkTableData"><a href="flamegraph.html?data=' + flamegraph_64 + '" title="' + flamegraph_64 + '">view</a></td>';
    var report_64 = perf_output_prefix + "64.report";
    html += '<td class="benchmarkTableData"><a href="report.html?data=' + report_64 + '" title="' + report_64 + '">view</a>';
    html += '(<a href="rawview.html?data=' + report_64 + '" title="' + report_64 + '">raw</a>)</td>';
    var annotate_64 = perf_output_prefix + "64.annotate";
    html += '<td class="benchmarkTableData"><a href="annotation.html?data=' + annotate_64 + '" title="' + report_64 + '">view</a>';
    html += '(<a href="rawview.html?data=' + annotate_64 + '" title="' + report_64 + '">raw</a>)</td>';
    html += "</tr>";
  }
  ubenchs_table.innerHTML = html;
}
