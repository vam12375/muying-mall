/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 0.0, "KoPercent": 100.0};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.0, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.0, 500, 1500, "新品列表"], "isController": false}, {"data": [0.0, 500, 1500, "分类列表"], "isController": false}, {"data": [0.0, 500, 1500, "获取收藏列表"], "isController": false}, {"data": [0.0, 500, 1500, "所有品牌"], "isController": false}, {"data": [0.0, 500, 1500, "热门商品"], "isController": false}, {"data": [0.0, 500, 1500, "商品搜索-高并发"], "isController": false}, {"data": [0.0, 500, 1500, "商品详情"], "isController": false}, {"data": [0.0, 500, 1500, "用户登录"], "isController": false}, {"data": [0.0, 500, 1500, "可用优惠券"], "isController": false}, {"data": [0.0, 500, 1500, "热门搜索词"], "isController": false}, {"data": [0.0, 500, 1500, "品牌详情"], "isController": false}, {"data": [0.0, 500, 1500, "商品搜索"], "isController": false}, {"data": [0.0, 500, 1500, "获取积分信息"], "isController": false}, {"data": [0.0, 500, 1500, "分类商品"], "isController": false}, {"data": [0.0, 500, 1500, "获取订单统计"], "isController": false}, {"data": [0.0, 500, 1500, "商品详情和参数"], "isController": false}, {"data": [0.0, 500, 1500, "获取订单列表"], "isController": false}, {"data": [0.0, 500, 1500, "品牌列表"], "isController": false}, {"data": [0.0, 500, 1500, "商品详情-高并发"], "isController": false}, {"data": [0.0, 500, 1500, "获取购物车总数"], "isController": false}, {"data": [0.0, 500, 1500, "商品列表"], "isController": false}, {"data": [0.0, 500, 1500, "推荐商品"], "isController": false}, {"data": [0.0, 500, 1500, "商品列表-高并发"], "isController": false}, {"data": [0.0, 500, 1500, "查看商品详情"], "isController": false}, {"data": [0.0, 500, 1500, "浏览商品列表"], "isController": false}, {"data": [0.0, 500, 1500, "热门商品-高并发"], "isController": false}, {"data": [0.0, 500, 1500, "获取收货地址列表"], "isController": false}, {"data": [0.0, 500, 1500, "分类详情"], "isController": false}, {"data": [0.0, 500, 1500, "获取用户优惠券"], "isController": false}, {"data": [0.0, 500, 1500, "分类列表-高并发"], "isController": false}, {"data": [0.0, 500, 1500, "获取购物车列表"], "isController": false}, {"data": [0.0, 500, 1500, "获取用户信息"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 77000, 77000, 100.0, 5.474064935064957, 0, 61, 1.0, 19.0, 21.0, 22.0, 1264.4716314968387, 3306.8896769028656, 0.0], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["新品列表", 1000, 1000, 100.0, 5.315999999999992, 0, 23, 1.0, 12.0, 21.0, 22.0, 32.881757201104826, 85.99350174273313, 0.0], "isController": false}, {"data": ["分类列表", 1000, 1000, 100.0, 5.281000000000007, 0, 22, 1.0, 11.0, 20.0, 22.0, 32.89581894141254, 86.0302764893582, 0.0], "isController": false}, {"data": ["获取收藏列表", 1000, 1000, 100.0, 5.539000000000002, 0, 31, 1.0, 15.899999999999977, 21.0, 21.99000000000001, 16.733601070950467, 43.76228873828648, 0.0], "isController": false}, {"data": ["所有品牌", 1000, 1000, 100.0, 5.273000000000005, 0, 23, 1.0, 11.0, 21.0, 22.0, 32.899065666535066, 86.0387674365048, 0.0], "isController": false}, {"data": ["热门商品", 1000, 1000, 100.0, 5.142, 0, 22, 1.0, 11.0, 20.0, 21.99000000000001, 32.890409156689906, 86.01612863439021, 0.0], "isController": false}, {"data": ["商品搜索-高并发", 10000, 10000, 100.0, 5.4468999999999825, 0, 39, 1.0, 12.0, 21.0, 22.0, 164.4304130491976, 430.0240685017101, 0.0], "isController": false}, {"data": ["商品详情", 1000, 1000, 100.0, 5.799000000000001, 0, 30, 1.0, 13.899999999999977, 21.0, 22.0, 32.88608260983952, 86.00481370034201, 0.0], "isController": false}, {"data": ["用户登录", 1000, 1000, 100.0, 6.498999999999998, 0, 61, 1.0, 20.0, 21.0, 22.0, 16.69978791269351, 43.67385940448556, 0.0], "isController": false}, {"data": ["可用优惠券", 1000, 1000, 100.0, 4.802999999999999, 0, 22, 1.0, 11.0, 20.0, 22.0, 32.907726734237194, 86.0614181584836, 0.0], "isController": false}, {"data": ["热门搜索词", 1000, 1000, 100.0, 5.213999999999994, 0, 22, 1.0, 11.0, 21.0, 22.0, 32.90556103981573, 86.05575435998684, 0.0], "isController": false}, {"data": ["品牌详情", 1000, 1000, 100.0, 4.986000000000003, 0, 24, 1.0, 11.0, 21.0, 22.0, 32.899065666535066, 86.0387674365048, 0.0], "isController": false}, {"data": ["商品搜索", 1000, 1000, 100.0, 5.435999999999998, 0, 22, 1.0, 12.0, 21.0, 22.0, 32.90339563042906, 86.05009130692288, 0.0], "isController": false}, {"data": ["获取积分信息", 1000, 1000, 100.0, 5.1080000000000005, 0, 38, 1.0, 11.0, 20.0, 22.0, 16.736681785469212, 43.77034552879546, 0.0], "isController": false}, {"data": ["分类商品", 1000, 1000, 100.0, 5.007999999999994, 0, 23, 1.0, 11.0, 20.0, 22.0, 32.88716413983622, 86.007642154767, 0.0], "isController": false}, {"data": ["获取订单统计", 1000, 1000, 100.0, 5.254000000000001, 0, 25, 1.0, 12.0, 21.0, 22.0, 16.733601070950467, 43.76228873828648, 0.0], "isController": false}, {"data": ["商品详情和参数", 1000, 1000, 100.0, 5.655999999999994, 0, 22, 1.0, 12.0, 21.0, 22.0, 32.888245740972174, 86.01047079523778, 0.0], "isController": false}, {"data": ["获取订单列表", 1000, 1000, 100.0, 5.171000000000004, 0, 25, 1.0, 11.0, 21.0, 22.0, 16.737522177216885, 43.77254335018244, 0.0], "isController": false}, {"data": ["品牌列表", 1000, 1000, 100.0, 5.336, 0, 22, 1.0, 11.0, 21.0, 22.0, 32.89690111191526, 86.03310661885651, 0.0], "isController": false}, {"data": ["商品详情-高并发", 10000, 10000, 100.0, 5.556599999999998, 0, 33, 1.0, 13.0, 21.0, 22.0, 164.49803424849074, 430.20091378658026, 0.0], "isController": false}, {"data": ["获取购物车总数", 1000, 1000, 100.0, 5.862999999999994, 0, 25, 1.0, 15.899999999999977, 21.0, 22.0, 16.734161116503227, 43.763753388667624, 0.0], "isController": false}, {"data": ["商品列表", 1000, 1000, 100.0, 5.358999999999997, 0, 61, 1.0, 12.0, 21.0, 22.0, 32.81808933083916, 85.82699533983131, 0.0], "isController": false}, {"data": ["推荐商品", 1000, 1000, 100.0, 5.071000000000004, 0, 23, 1.0, 11.0, 20.0, 22.0, 32.88283844661471, 85.9963294531584, 0.0], "isController": false}, {"data": ["商品列表-高并发", 10000, 10000, 100.0, 5.628100000000018, 0, 61, 1.0, 16.0, 21.0, 22.0, 164.38986700859758, 429.9180311025628, 0.0], "isController": false}, {"data": ["查看商品详情", 1000, 1000, 100.0, 5.45600000000001, 0, 35, 1.0, 12.0, 21.0, 22.0, 16.733601070950467, 43.76228873828648, 0.0], "isController": false}, {"data": ["浏览商品列表", 1000, 1000, 100.0, 5.399999999999993, 0, 22, 1.0, 12.0, 21.0, 22.0, 16.733601070950467, 43.76228873828648, 0.0], "isController": false}, {"data": ["热门商品-高并发", 10000, 10000, 100.0, 5.406800000000015, 0, 33, 1.0, 12.0, 21.0, 22.0, 164.46015952635474, 430.10186251130665, 0.0], "isController": false}, {"data": ["获取收货地址列表", 1000, 1000, 100.0, 5.680999999999998, 0, 25, 1.0, 13.899999999999977, 21.0, 22.0, 16.734441153337684, 43.76448575062336, 0.0], "isController": false}, {"data": ["分类详情", 1000, 1000, 100.0, 5.535000000000001, 0, 23, 1.0, 12.0, 21.0, 22.0, 32.88716413983622, 86.007642154767, 0.0], "isController": false}, {"data": ["获取用户优惠券", 1000, 1000, 100.0, 5.34, 0, 31, 1.0, 11.0, 20.949999999999932, 22.0, 16.73388108904098, 43.76302105122241, 0.0], "isController": false}, {"data": ["分类列表-高并发", 10000, 10000, 100.0, 5.483400000000022, 0, 34, 1.0, 12.0, 21.0, 22.0, 164.4682740699319, 430.12308394460706, 0.0], "isController": false}, {"data": ["获取购物车列表", 1000, 1000, 100.0, 5.600000000000004, 0, 22, 1.0, 12.0, 21.0, 22.0, 16.734161116503227, 43.763753388667624, 0.0], "isController": false}, {"data": ["获取用户信息", 1000, 1000, 100.0, 6.158999999999995, 0, 34, 1.0, 20.0, 21.0, 22.0, 16.73304106288277, 43.76082418593755, 0.0], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": [{"data": ["Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 77000, 100.0, 100.0], "isController": false}]}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 77000, 77000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 77000, "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": ["新品列表", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["分类列表", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["获取收藏列表", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["所有品牌", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["热门商品", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["商品搜索-高并发", 10000, 10000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 10000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["商品详情", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["用户登录", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["可用优惠券", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["热门搜索词", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["品牌详情", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["商品搜索", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["获取积分信息", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["分类商品", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["获取订单统计", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["商品详情和参数", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["获取订单列表", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["品牌列表", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["商品详情-高并发", 10000, 10000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 10000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["获取购物车总数", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["商品列表", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["推荐商品", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["商品列表-高并发", 10000, 10000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 10000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["查看商品详情", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["浏览商品列表", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["热门商品-高并发", 10000, 10000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 10000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["获取收货地址列表", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["分类详情", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["获取用户优惠券", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["分类列表-高并发", 10000, 10000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 10000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["获取购物车列表", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["获取用户信息", 1000, 1000, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: getsockopt", 1000, "", "", "", "", "", "", "", ""], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
