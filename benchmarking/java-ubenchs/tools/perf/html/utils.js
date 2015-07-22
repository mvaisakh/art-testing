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

var Utils = {
  getURLParam : function(name) {
    if (Utils.url_params == undefined) {
      var url_params = {};
      window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi,
        function(m, key, value) {
          url_params[key] = value;
        }
      );
      Utils.url_params = url_params;
    }
    return Utils.url_params[name];
  },

  getDataURL : function() {
    return Utils.getURLParam("data");
    // Get full path, but the relative path should work.
    return window.location.origin
      + window.location.pathname.replace(/\\/g,'/').replace(/\/[^\/]*$/, '')
      + "/" + Utils.getURLParam("data");
  },

  loadData : function(url, onload) {
    var xhr = new XMLHttpRequest();
    xhr.onload = function() {
      onload(this.response);
    };
    xhr.open("get", url, true);
    xhr.send();
  }

};
