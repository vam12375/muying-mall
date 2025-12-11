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

    var data = {"OkPercent": 93.69090909090909, "KoPercent": 6.3090909090909095};
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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.8404675324675325, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.955, 500, 1500, "新品列表"], "isController": false}, {"data": [0.957, 500, 1500, "分类列表"], "isController": false}, {"data": [0.28, 500, 1500, "获取收藏列表"], "isController": false}, {"data": [0.9575, 500, 1500, "所有品牌"], "isController": false}, {"data": [0.947, 500, 1500, "热门商品"], "isController": false}, {"data": [0.73475, 500, 1500, "商品搜索-高并发"], "isController": false}, {"data": [0.937, 500, 1500, "商品详情"], "isController": false}, {"data": [0.756, 500, 1500, "用户登录"], "isController": false}, {"data": [0.945, 500, 1500, "可用优惠券"], "isController": false}, {"data": [0.941, 500, 1500, "热门搜索词"], "isController": false}, {"data": [0.9075, 500, 1500, "品牌详情"], "isController": false}, {"data": [0.7855, 500, 1500, "商品搜索"], "isController": false}, {"data": [0.278, 500, 1500, "获取积分信息"], "isController": false}, {"data": [0.9365, 500, 1500, "分类商品"], "isController": false}, {"data": [0.8935, 500, 1500, "获取订单统计"], "isController": false}, {"data": [0.9245, 500, 1500, "商品详情和参数"], "isController": false}, {"data": [0.9015, 500, 1500, "获取订单列表"], "isController": false}, {"data": [0.961, 500, 1500, "品牌列表"], "isController": false}, {"data": [0.92385, 500, 1500, "商品详情-高并发"], "isController": false}, {"data": [0.2755, 500, 1500, "获取购物车总数"], "isController": false}, {"data": [0.9265, 500, 1500, "商品列表"], "isController": false}, {"data": [0.949, 500, 1500, "推荐商品"], "isController": false}, {"data": [0.9196, 500, 1500, "商品列表-高并发"], "isController": false}, {"data": [0.8655, 500, 1500, "查看商品详情"], "isController": false}, {"data": [0.8895, 500, 1500, "浏览商品列表"], "isController": false}, {"data": [0.933, 500, 1500, "热门商品-高并发"], "isController": false}, {"data": [0.276, 500, 1500, "获取收货地址列表"], "isController": false}, {"data": [0.9155, 500, 1500, "分类详情"], "isController": false}, {"data": [0.281, 500, 1500, "获取用户优惠券"], "isController": false}, {"data": [0.94125, 500, 1500, "分类列表-高并发"], "isController": false}, {"data": [0.281, 500, 1500, "获取购物车列表"], "isController": false}, {"data": [0.2685, 500, 1500, "获取用户信息"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 77000, 4858, 6.3090909090909095, 303.05858441558917, 2, 18192, 147.0, 607.0, 769.0, 1105.0, 985.7766511758907, 3625.598144687528, 207.89524232742505], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["新品列表", 1000, 0, 0.0, 164.7229999999998, 2, 16229, 10.0, 466.79999999999995, 604.8999999999999, 1378.0, 12.989037252558841, 65.46525513716423, 2.5115521250064945], "isController": false}, {"data": ["分类列表", 1000, 0, 0.0, 159.42300000000014, 2, 17048, 8.0, 471.0, 661.7999999999997, 1043.97, 12.992750045474624, 22.331289140659514, 2.3853877036613573], "isController": false}, {"data": ["获取收藏列表", 1000, 694, 69.4, 283.7550000000004, 7, 16960, 54.0, 723.9, 990.9499999999999, 1542.6700000000003, 13.291509383805625, 5.9174215136370885, 3.9662435120819817], "isController": false}, {"data": ["所有品牌", 1000, 0, 0.0, 154.56800000000004, 3, 9520, 35.0, 462.79999999999995, 667.299999999999, 1262.5400000000004, 12.999844001871978, 38.82179976340284, 2.3866901097186832], "isController": false}, {"data": ["热门商品", 1000, 0, 0.0, 213.70100000000002, 2, 15177, 17.0, 499.0, 710.9499999999999, 1207.98, 12.980101504393764, 50.842956185667376, 2.509824314326138], "isController": false}, {"data": ["商品搜索-高并发", 10000, 0, 0.0, 625.128999999999, 5, 18192, 440.0, 1327.0, 1930.949999999999, 3077.959999999999, 128.33510863566946, 1142.859234031904, 29.07592305026886], "isController": false}, {"data": ["商品详情", 1000, 0, 0.0, 246.13299999999995, 6, 16227, 44.0, 538.9, 791.9499999999999, 1451.7100000000003, 12.98886853966151, 19.654530091149383, 2.3963320855901493], "isController": false}, {"data": ["用户登录", 1000, 0, 0.0, 859.8020000000001, 7, 16743, 428.0, 1277.6, 2024.099999999996, 14967.700000000013, 13.313097425246958, 9.944467742364939, 4.6161865214873385], "isController": false}, {"data": ["可用优惠券", 1000, 0, 0.0, 187.9309999999999, 4, 16235, 36.0, 522.6999999999999, 731.5499999999994, 1266.6500000000003, 12.952528981283596, 23.021096431578265, 2.4665460462405284], "isController": false}, {"data": ["热门搜索词", 1000, 0, 0.0, 179.3420000000001, 2, 16356, 27.0, 554.0, 678.3499999999991, 1063.91, 12.955213825804194, 8.172918097138194, 2.5935730803611916], "isController": false}, {"data": ["品牌详情", 1000, 0, 0.0, 241.09300000000013, 6, 2365, 76.5, 679.8999999999997, 929.4499999999992, 1551.6600000000003, 12.997647425815927, 8.137568115796041, 2.3684404529030245], "isController": false}, {"data": ["商品搜索", 1000, 0, 0.0, 526.7320000000001, 5, 17096, 229.5, 1422.2999999999997, 1965.3999999999992, 2932.7700000000004, 12.955717358070116, 115.37420565257948, 3.0491483235301735], "isController": false}, {"data": ["获取积分信息", 1000, 694, 69.4, 277.92000000000024, 7, 17851, 53.0, 714.6999999999999, 955.7999999999997, 1651.6900000000003, 13.287977038375677, 6.547209483297013, 3.705658643496864], "isController": false}, {"data": ["分类商品", 1000, 0, 0.0, 197.72299999999993, 6, 10077, 36.5, 548.5999999999999, 775.8499999999998, 1414.99, 12.988193731897704, 50.65845829734522, 2.690243045634019], "isController": false}, {"data": ["获取订单统计", 1000, 0, 0.0, 286.4430000000007, 5, 1967, 143.5, 769.8, 982.9499999999999, 1421.7500000000002, 13.289036544850498, 7.412089908637873, 3.705954111295681], "isController": false}, {"data": ["商品详情和参数", 1000, 0, 0.0, 253.787, 7, 17014, 68.0, 603.1999999999998, 789.6499999999995, 1504.880000000001, 12.977574750830565, 38.88244882374507, 2.495661130379205], "isController": false}, {"data": ["获取订单列表", 1000, 0, 0.0, 306.2100000000005, 5, 16199, 136.0, 677.9, 996.5499999999994, 1510.8500000000001, 13.279330721731625, 7.105090340946815, 3.910736961357147], "isController": false}, {"data": ["品牌列表", 1000, 0, 0.0, 161.60200000000012, 2, 16239, 9.0, 441.69999999999993, 596.7499999999997, 1220.7500000000002, 12.990049621989556, 39.490258274661606, 2.499062280792913], "isController": false}, {"data": ["商品详情-高并发", 10000, 0, 0.0, 244.3609000000001, 5, 17067, 63.0, 594.8999999999996, 805.0, 1389.9199999999983, 128.23636526846283, 137.68008443162438, 23.727384314768983], "isController": false}, {"data": ["获取购物车总数", 1000, 694, 69.4, 287.2469999999999, 7, 13638, 75.5, 783.8, 1032.9499999999998, 1666.5500000000004, 13.31008505144348, 5.706932932145186, 3.6988258425283838], "isController": false}, {"data": ["商品列表", 1000, 0, 0.0, 421.1390000000001, 2, 16351, 26.0, 543.3999999999999, 973.8499999999984, 10138.190000000002, 12.981786553465488, 81.02206822577922, 2.524171478041308], "isController": false}, {"data": ["推荐商品", 1000, 0, 0.0, 179.63300000000004, 2, 11878, 10.0, 487.79999999999995, 691.9499999999999, 1200.92, 12.990049621989556, 80.33787525005846, 2.727402996804448], "isController": false}, {"data": ["商品列表-高并发", 10000, 0, 0.0, 287.49500000000046, 2, 16838, 37.0, 623.0, 878.8999999999978, 2239.929999999933, 128.0557298536323, 799.2228217720352, 24.956223448444764], "isController": false}, {"data": ["查看商品详情", 1000, 0, 0.0, 347.36200000000014, 9, 17180, 210.5, 818.0, 1014.9499999999999, 1544.96, 13.329778725673155, 44.23371215592509, 3.8191378132498004], "isController": false}, {"data": ["浏览商品列表", 1000, 0, 0.0, 353.82100000000014, 4, 17174, 100.0, 726.4999999999999, 990.8999999999999, 1877.4800000000014, 13.345433192761437, 83.49903912881013, 3.852008112355202], "isController": false}, {"data": ["热门商品-高并发", 10000, 0, 0.0, 213.37580000000023, 2, 17060, 37.0, 579.0, 790.0, 1227.9699999999993, 128.2643271253399, 502.41036728490076, 24.80111012775127], "isController": false}, {"data": ["获取收货地址列表", 1000, 694, 69.4, 264.34099999999984, 7, 10383, 57.0, 778.9, 1057.9499999999998, 1565.9, 13.301940753155884, 5.707415915107013, 3.748523276733575], "isController": false}, {"data": ["分类详情", 1000, 0, 0.0, 304.9990000000001, 5, 17067, 52.0, 722.8, 995.8999999999999, 1593.6900000000003, 12.989543417548873, 7.455008483795544, 2.4173616410664414], "isController": false}, {"data": ["获取用户优惠券", 1000, 694, 69.4, 286.84500000000025, 7, 17017, 56.0, 660.1999999999998, 960.9499999999999, 1900.99, 13.291332721932028, 5.702864365272406, 3.8493724414184514], "isController": false}, {"data": ["分类列表-高并发", 10000, 0, 0.0, 184.3554000000004, 2, 17780, 12.0, 536.0, 737.0, 1183.9899999999998, 128.31205491755952, 220.53634438955538, 23.55729133252069], "isController": false}, {"data": ["获取购物车列表", 1000, 694, 69.4, 305.72799999999995, 7, 9004, 111.5, 847.5999999999999, 1130.349999999999, 1557.92, 13.321787783920602, 5.715931609271965, 3.6890684323586225], "isController": false}, {"data": ["获取用户信息", 1000, 694, 69.4, 336.34700000000004, 7, 10269, 94.5, 863.0999999999998, 1118.7999999999997, 1977.0, 13.347748902147654, 6.947607582856151, 3.696257583189845], "isController": false}]}, function(index, item){
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
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": [{"data": ["403", 4858, 100.0, 6.3090909090909095], "isController": false}]}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 77000, 4858, "403", 4858, "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["获取收藏列表", 1000, 694, "403", 694, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["获取积分信息", 1000, 694, "403", 694, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["获取购物车总数", 1000, 694, "403", 694, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["获取收货地址列表", 1000, 694, "403", 694, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": ["获取用户优惠券", 1000, 694, "403", 694, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": ["获取购物车列表", 1000, 694, "403", 694, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["获取用户信息", 1000, 694, "403", 694, "", "", "", "", "", "", "", ""], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
