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

    var data = {"OkPercent": 100.0, "KoPercent": 0.0};
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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.9871883116883117, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.999, 500, 1500, "新品列表"], "isController": false}, {"data": [0.9965, 500, 1500, "分类列表"], "isController": false}, {"data": [0.9965, 500, 1500, "获取收藏列表"], "isController": false}, {"data": [0.9995, 500, 1500, "所有品牌"], "isController": false}, {"data": [0.999, 500, 1500, "热门商品"], "isController": false}, {"data": [0.92935, 500, 1500, "商品搜索-高并发"], "isController": false}, {"data": [0.997, 500, 1500, "商品详情"], "isController": false}, {"data": [0.9405, 500, 1500, "用户登录"], "isController": false}, {"data": [0.998, 500, 1500, "可用优惠券"], "isController": false}, {"data": [1.0, 500, 1500, "热门搜索词"], "isController": false}, {"data": [0.996, 500, 1500, "品牌详情"], "isController": false}, {"data": [0.8885, 500, 1500, "商品搜索"], "isController": false}, {"data": [0.9975, 500, 1500, "获取积分信息"], "isController": false}, {"data": [0.9965, 500, 1500, "分类商品"], "isController": false}, {"data": [0.996, 500, 1500, "获取订单统计"], "isController": false}, {"data": [0.999, 500, 1500, "商品详情和参数"], "isController": false}, {"data": [0.997, 500, 1500, "获取订单列表"], "isController": false}, {"data": [0.998, 500, 1500, "品牌列表"], "isController": false}, {"data": [0.99845, 500, 1500, "商品详情-高并发"], "isController": false}, {"data": [0.9985, 500, 1500, "获取购物车总数"], "isController": false}, {"data": [0.999, 500, 1500, "商品列表"], "isController": false}, {"data": [0.9985, 500, 1500, "推荐商品"], "isController": false}, {"data": [0.99905, 500, 1500, "商品列表-高并发"], "isController": false}, {"data": [0.9915, 500, 1500, "查看商品详情"], "isController": false}, {"data": [0.998, 500, 1500, "浏览商品列表"], "isController": false}, {"data": [0.9989, 500, 1500, "热门商品-高并发"], "isController": false}, {"data": [0.997, 500, 1500, "获取收货地址列表"], "isController": false}, {"data": [0.999, 500, 1500, "分类详情"], "isController": false}, {"data": [0.9995, 500, 1500, "获取用户优惠券"], "isController": false}, {"data": [0.99885, 500, 1500, "分类列表-高并发"], "isController": false}, {"data": [0.9945, 500, 1500, "获取购物车列表"], "isController": false}, {"data": [0.9975, 500, 1500, "获取用户信息"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 77000, 0, 0.0, 101.37293506493386, 2, 1751, 40.0, 208.0, 282.9500000000007, 461.9800000000032, 1239.4166693493867, 4166.633478485256, 262.5621563184094], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["新品列表", 1000, 0, 0.0, 50.97200000000006, 2, 659, 8.0, 157.0, 202.89999999999986, 357.84000000000015, 21.537798836958864, 132.4658760499677, 4.18556832866681], "isController": false}, {"data": ["分类列表", 1000, 0, 0.0, 49.098000000000035, 2, 820, 7.0, 150.0, 205.0, 473.0, 21.520649062775732, 36.98861557664579, 3.9510566638689824], "isController": false}, {"data": ["获取收藏列表", 1000, 0, 0.0, 91.22299999999991, 5, 610, 57.5, 224.0, 283.0, 460.6900000000003, 16.202466015327534, 8.851229969701388, 4.868398001425817], "isController": false}, {"data": ["所有品牌", 1000, 0, 0.0, 59.77599999999996, 3, 716, 35.0, 145.0, 197.0, 314.96000000000004, 21.47397354406459, 64.12833115014602, 3.9424873303556094], "isController": false}, {"data": ["热门商品", 1000, 0, 0.0, 64.36000000000001, 2, 654, 11.0, 184.79999999999995, 232.89999999999986, 405.0, 21.56799309824221, 134.48401946511376, 4.191436158740429], "isController": false}, {"data": ["商品搜索-高并发", 10000, 0, 0.0, 256.6934999999983, 5, 1751, 202.0, 567.0, 682.0, 940.9899999999998, 161.19152777330024, 1003.018185426674, 36.99219631516168], "isController": false}, {"data": ["商品详情", 1000, 0, 0.0, 82.9080000000001, 6, 659, 36.5, 209.89999999999998, 287.94999999999993, 421.95000000000005, 21.683977708870916, 33.54096306676497, 4.000736238805647], "isController": false}, {"data": ["用户登录", 1000, 0, 0.0, 230.74799999999976, 5, 1292, 165.0, 525.3999999999999, 646.8999999999999, 1014.7600000000002, 16.19747967216301, 12.526884019971492, 5.5958179626000195], "isController": false}, {"data": ["可用优惠券", 1000, 0, 0.0, 80.61000000000001, 4, 662, 30.0, 205.89999999999998, 292.94999999999993, 438.99, 21.223311685555412, 37.72112037862388, 4.041548611995416], "isController": false}, {"data": ["热门搜索词", 1000, 0, 0.0, 84.72200000000002, 2, 481, 46.0, 209.89999999999998, 294.8499999999998, 402.96000000000004, 21.298800877510594, 13.270151327980235, 4.284719707780452], "isController": false}, {"data": ["品牌详情", 1000, 0, 0.0, 100.84499999999996, 6, 711, 65.5, 241.0, 316.7999999999997, 494.93000000000006, 21.445881318492784, 13.494569500739884, 3.907632254069356], "isController": false}, {"data": ["商品搜索", 1000, 0, 0.0, 308.94600000000014, 5, 1350, 269.0, 669.8, 852.9499999999999, 978.8700000000001, 21.366151742409674, 11.876763542134052, 5.0911533448710555], "isController": false}, {"data": ["获取积分信息", 1000, 0, 0.0, 91.54400000000003, 5, 601, 52.0, 220.79999999999995, 290.94999999999993, 462.85000000000014, 16.195118791196332, 9.651721439908012, 4.518248356195443], "isController": false}, {"data": ["分类商品", 1000, 0, 0.0, 51.638999999999974, 6, 702, 14.0, 152.89999999999998, 196.94999999999993, 407.9000000000001, 21.49890355591865, 133.98240566684223, 4.492934922818936], "isController": false}, {"data": ["获取订单统计", 1000, 0, 0.0, 101.97000000000001, 5, 797, 68.0, 249.0, 315.89999999999986, 475.85000000000014, 16.194594244441205, 9.050532447893893, 4.518102013797794], "isController": false}, {"data": ["商品详情和参数", 1000, 0, 0.0, 89.223, 6, 568, 54.5, 212.0, 293.0, 411.7600000000002, 21.60713899872518, 64.3151051795013, 4.155111911475552], "isController": false}, {"data": ["获取订单列表", 1000, 0, 0.0, 86.9169999999999, 5, 694, 50.0, 211.89999999999998, 290.94999999999993, 425.99, 16.19564337193295, 8.67368435703296, 4.803083751720787], "isController": false}, {"data": ["品牌列表", 1000, 0, 0.0, 36.65399999999999, 2, 685, 7.0, 126.0, 173.94999999999993, 302.8700000000001, 21.500290253918426, 65.36172222700006, 4.17827906301735], "isController": false}, {"data": ["商品详情-高并发", 10000, 0, 0.0, 77.77879999999999, 5, 784, 36.0, 195.0, 260.0, 387.9899999999998, 161.1707442864971, 246.31809841690037, 29.735105179020405], "isController": false}, {"data": ["获取购物车总数", 1000, 0, 0.0, 96.515, 5, 728, 61.5, 242.0, 306.0, 455.95000000000005, 16.21139661181811, 8.582699653481397, 4.506958235389479], "isController": false}, {"data": ["商品列表", 1000, 0, 0.0, 66.11799999999997, 2, 621, 14.0, 186.89999999999998, 262.89999999999986, 393.8700000000001, 21.654395842355996, 133.57294147899523, 4.252910655316154], "isController": false}, {"data": ["推荐商品", 1000, 0, 0.0, 49.89999999999999, 2, 693, 7.0, 155.89999999999998, 206.89999999999986, 410.73000000000025, 21.537334970170793, 133.47258566474986, 4.564064148952209], "isController": false}, {"data": ["商品列表-高并发", 10000, 0, 0.0, 76.19769999999994, 2, 865, 40.0, 195.0, 265.0, 388.0, 161.01763143064164, 623.9767203727558, 31.69349035906932], "isController": false}, {"data": ["查看商品详情", 1000, 0, 0.0, 118.67800000000011, 8, 759, 81.0, 290.9, 366.94999999999993, 555.8000000000002, 16.21323648626739, 53.70780251872629, 4.646849742817537], "isController": false}, {"data": ["浏览商品列表", 1000, 0, 0.0, 86.08800000000025, 4, 670, 47.0, 213.89999999999998, 274.94999999999993, 408.8700000000001, 16.219284729543425, 101.48659881599221, 4.715060112724029], "isController": false}, {"data": ["热门商品-高并发", 10000, 0, 0.0, 64.51809999999995, 2, 695, 16.0, 176.0, 237.9499999999989, 389.9899999999998, 161.19152777330024, 1005.0858445630098, 31.32530666688159], "isController": false}, {"data": ["获取收货地址列表", 1000, 0, 0.0, 90.112, 5, 714, 56.0, 213.0, 289.94999999999993, 456.95000000000005, 16.203778721197786, 11.212097082914736, 4.568136383154552], "isController": false}, {"data": ["分类详情", 1000, 0, 0.0, 47.39299999999998, 5, 509, 10.0, 150.0, 200.8499999999998, 391.71000000000026, 21.500290253918426, 15.250613598127323, 3.9893116682075207], "isController": false}, {"data": ["获取用户优惠券", 1000, 0, 0.0, 95.30799999999999, 5, 502, 59.0, 237.0, 306.89999999999986, 387.97, 16.19747967216301, 8.580298499303508, 4.708721229226732], "isController": false}, {"data": ["分类列表-高并发", 10000, 0, 0.0, 52.910700000000205, 2, 858, 8.0, 160.0, 207.0, 374.0, 161.1967244825585, 277.05687020439746, 29.594711135469726], "isController": false}, {"data": ["获取购物车列表", 1000, 0, 0.0, 111.63999999999996, 5, 960, 74.0, 270.0, 340.94999999999993, 513.97, 16.2119222476209, 8.587949171570774, 4.491272412982507], "isController": false}, {"data": ["获取用户信息", 1000, 0, 0.0, 100.82100000000001, 5, 679, 69.5, 235.89999999999998, 296.94999999999993, 425.93000000000006, 16.222442126437716, 10.503714432295638, 4.494186789254254], "isController": false}]}, function(index, item){
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
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": []}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 77000, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
