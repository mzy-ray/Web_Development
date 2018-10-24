var stockApp = angular.module('stockApp', ['ngMaterial','ngAnimate']);
stockApp.controller('stockCtrl', function($scope, $window, $http, stockService) {
    $scope.panelSwitch = false;
    $scope.currentStock = [];
    $scope.priceDates = [];
    $scope.priceData = [];
    $scope.indicatorData = [];
    $scope.newsData = [];
    $scope.favoriteList = [];
    $scope.chartData = [];
    $scope.switchCurrent = true;
    $scope.switchHistory = false;
    $scope.switchNews = false;
    $scope.activePil = "cur";
    $scope.activeTab = "Price";
    
    $scope.sortBy = {
		options: [
            {id:"0", name:"Default", type:""},
            {id:"1", name:"Symbol", type:"symbol"},
            {id:"2", name:"Price", type:"price"},
            {id:"3", name:"Change", type:"change"},
            {id:"4", name:"Change Percent", type:"changePercent"},
            {id:"5", name:"Volume", type:"volume"}
		],
		model: {id:"0", name:"Default", type:""}
	};
    
    $scope.order = {
		options : [
            {id:"0", name:"Ascending", reverse:false},
            {id:"1", name:"Descending", reverse:true}
		],
		model : {id:"0", name:"Ascending", reverse:false}
	};
    
    generateFavoriteList();
    
        
    $scope.querySearch = function (query) {
        if (query && query.trim() !== "") {
            var promise = stockService.completeQuery(query);
            return promise.then(function (data) {
                var items = [];
                var jsonObj = JSON.parse(data);
                for (var key in jsonObj){
                    var value = jsonObj[key]["Symbol"];
                    var display = jsonObj[key]["Symbol"] + " - " + jsonObj[key]["Name"] + " (" + jsonObj[key]["Exchange"] + ")";
                    items.push({"value":value, "display":display});
                }
                return items;
            });
        }
        else return [];
    };
    
    
    $scope.clear = function clear(){
		$scope.searchText = "";
        queryForm.autocomplete.$touched=false;
        
        $scope.currentStock = [];
        $scope.priceDates = [];
        $scope.priceData = [];
        $scope.indicatorData = [];
        $scope.newsData = [];
        $scope.chartData = [];
        $scope.panelSwitch = false;
        generateFavoriteList()
	};
    
    
    $scope.submitQuery = function(symbol) {
        $scope.currentStock = [];
        $scope.priceDates = [];
        $scope.priceData = [];
        $scope.indicatorData = [];
        $scope.newsData = [];
        $scope.chartData = [];
        $scope.panelSwitch = true;
        
        $scope.switchCurrent = true;
        $scope.switchHistory = false;
        $scope.switchNews = false;
        $scope.activePil = "cur";
        $scope.activeTab = "Price";
        
        $scope.showTableProgress = true;
        $scope.showTableData = false;
        $scope.showTableError = false;
        $scope.showGraphProgress = true;
        $scope.showGraphData = false;
        $scope.showGraphError = false;
        $scope.showHistoryProgress = true;
        $scope.showHistoryData = false;
        $scope.showHistoryError = false;
        $scope.showNewsProgress = true;
        $scope.showNewsData = false;
        $scope.showNewsError = false;
        var promise1 = stockService.stockQuery(symbol);
        var promise2 = stockService.indicatorQuery(symbol);
        var promise3 = stockService.newsQuery(symbol);
        promise1.then(function(data) {
            parseStockData(data);
        });
        promise2.then(function(data) {
            $scope.indicatorData = data;
            $scope.showGraphProgress = false;
            $scope.showGraphData = true;
        }); 
        promise3.then(function(data) {
            parseNewsData(data);
        });
        
        var starEle = angular.element(document.querySelector('#star'));
        if ($window.localStorage[symbol]){
            starEle.attr('class', "glyphicon glyphicon-star");
            starEle.attr('style', "color: orange");
        }
        else{
            starEle.attr('class', "glyphicon glyphicon-star-empty");
			starEle.removeAttr('style');
        }
    };
    
    
    function parseStockData(data){
        try{
            var jsonObj = JSON.parse(data);
        }catch (e){
            alert("Failed to parse stock json file");
        }
        if (jsonObj != null){
            if (jsonObj["Meta Data"]){
                $scope.priceDates = Object.keys(jsonObj["Time Series (Daily)"]);
                $scope.priceData = Object.values(jsonObj["Time Series (Daily)"]);
                $scope.currentStock["symbol"] = jsonObj["Meta Data"]["2. Symbol"];                
                $scope.currentStock["lastPrice"] = parseFloat($scope.priceData[0]["4. close"]).toFixed(2);
                $scope.currentStock["change"] = (parseFloat($scope.priceData[0]["4. close"]) - parseFloat($scope.priceData[1]["4. close"])).toFixed(2);
                $scope.currentStock["change"] += " (" + ((parseFloat($scope.priceData[0]["4. close"]) - parseFloat($scope.priceData[1]["4. close"])) / parseFloat($scope.priceData[1]["4. close"]) * 100).toFixed(2) + "%)";
                if (parseFloat($scope.currentStock["change"]) >= 0){
                    $scope.currentStock["arrow"] = "/up.png";
                }
                else{
                    $scope.currentStock["arrow"] = "/down.png"
                }
                $scope.currentStock["timestamp"] = jsonObj["Meta Data"]["3. Last Refreshed"];
                if ($scope.currentStock["timestamp"].length === 10){
                    $scope.currentStock["timestamp"] += " 16:00:00 EST";
                }
                $scope.currentStock["open"] = parseFloat($scope.priceData[0]["1. open"]).toFixed(2);
                $scope.currentStock["preClose"] = parseFloat($scope.priceData[1]["4. close"]).toFixed(2);
                $scope.currentStock["daysRange"] = parseFloat($scope.priceData[0]["3. low"]).toFixed(2) + " - " + parseFloat($scope.priceData[0]["2. high"]).toFixed(2);
                $scope.currentStock["volume"] = Number($scope.priceData[0]["5. volume"]).toLocaleString();
                $scope.showTableProgress = false;
                $scope.showTableData = true;
                $scope.showHistoryProgress = false;
                $scope.showHistoryData = true;
            }
            else{
                $scope.showTableProgress = false;
                $scope.showTableError = true;
                $scope.showHistoryProgress = false;
                $scope.showHistoryError = true;
            }
        
            drawPriceChart();
            drawHistoryChart();
            
        }
        else{
            $scope.showTableProgress = false;
            $scope.showTableError = true;
            $scope.showHistoryProgress = false;
            $scope.showHistoryError = true;
        }
    }
    
    
    function drawPriceChart(){
        var dates = $scope.priceDates.slice(0, 130);
        var data = $scope.priceData.slice(0, 130);
        var xdates = [];
        var prices = [];
        var volumes = [];
        var maxPrice = Number.MIN_VALUE;
        var maxVolume = Number.MIN_VALUE;
        for (i = dates.length - 1; i >= 0; i--){
            xdates.push(dates[i].substr(5));
            var curPrice = Math.round(parseFloat(data[i]["4. close"])*100)/100;
            prices.push(curPrice);
            if (curPrice > maxPrice){
                maxPrice = curPrice;
            }
            var curVolume = Math.round(parseInt(data[i]["5. volume"])/1000000);
            volumes.push(curVolume);
            if (curVolume > maxVolume){
                maxVolume = curVolume;
            }
        }
        
        $scope.chartData["Price"] = {
            "chart": {
                "borderWidth": 2,
                "borderColor": '#cecece',
                "type": 'area',
                "type": 'column',
                "zoomType": 'x'
            },
            "title": {
                "text": $scope.currentStock["symbol"] + ' Stock Price and Volume'
            },
            "subtitle": {
                "useHTML": true,
                "text": '<a style="color:blue" href="https://www.alphavantage.co/" target="_blank">' + 'Source: Alpha Vantage</a>'
            },
            "xAxis": {
                "categories": xdates,
                "tickInterval": 5
            },
            "yAxis": [{
                "min": 0,
                "max": maxPrice,
                "title": {
                    "text": 'Stock Price'
                }
            },{
                "min": 0,
                "max": maxVolume,
                "title": {
                    "text": 'Volume'
                },
                "labels": {
                    "formatter": function () {
                        return this.value + 'M';
                    }
                },
                "opposite": true
            }],
            "series": [{
                "name": 'Price',
                "type": 'area',
                "yAxis": 0,
                "color": 'blue',
                "fillColor":'#afafff',
			    "lineWidth": 2,
                "data": prices
            },{
                "name": 'Volume',
                "type": 'column',
                "yAxis": 1,
                "color": 'red',
                "data": volumes
            }]
        };
        Highcharts.chart('curChartContainer', $scope.chartData["Price"]);
    }
    
    
    $scope.priceChart = function(){
        drawPriceChart();
    };
    $scope.switchChart = function(indicator){
        $scope.activeTab = indicator;
        for (key in $scope.indicatorData){
            var line = $scope.indicatorData[key];
            for (key in line){
                if (key === indicator){
                    if (line[indicator]["Meta Data"]){
                        var data = line[indicator];
                        drawIndicatorChart(indicator, data);
                        $scope.showGraphData = true;
                        $scope.showGraphError = false;
                    }
                    else{
                        $scope.graphError = indicator;
                        $scope.showGraphData = false;
                        $scope.showGraphError = true;
                    }
                }
            }
        }        
    };
    
    
    function drawIndicatorChart(name, jsonObj){
        var symbol = jsonObj["Meta Data"]["1: Symbol"];
        var indicator = jsonObj["Meta Data"]["2: Indicator"];
        var days = Object.keys(jsonObj["Technical Analysis: "+name]);
        days = days.slice(0, 130);
        var xdates = [];
        var prices = [];
        var minpriceL = Number.MAX_VALUE;
        
        if (name == "BBANDS"){
            var snames = ["Real Middle Band", "Real Lower Band", "Real Upper Band"];
            var prices1 = [];
            var prices2 = [];
            var prices3 = [];
            for (i = days.length - 1; i >= 0; i--){
                xdates.push(days[i].substr(5, 5));
                prices1.push(Math.round(parseFloat(jsonObj["Technical Analysis: "+name][days[i]]["Real Middle Band"])*100)/100);
                prices2.push(Math.round(parseFloat(jsonObj["Technical Analysis: "+name][days[i]]["Real Lower Band"])*100)/100);
                prices3.push(Math.round(parseFloat(jsonObj["Technical Analysis: "+name][days[i]]["Real Upper Band"])*100)/100);
                if (prices1[days.length-1-i] < minpriceL){
                    minpriceL = prices1[days.length-1-i];
                }
                if (prices2[days.length-1-i] < minpriceL){
                    minpriceL = prices2[days.length-1-i];
                }
                if (prices3[days.length-1-i] < minpriceL){
                    minpriceL = prices3[days.length-1-i];
                }
            }
            prices.push(prices1);
            prices.push(prices2);
            prices.push(prices3);
        }
        else if(name == "MACD"){
            var snames = ["MACD_Signal", "MACD_Hist", "MACD"];
            var prices1 = [];
            var prices2 = [];
            var prices3 = [];
            for (i = days.length - 1; i >= 0; i--){
                xdates.push(days[i].substr(5, 5));
                prices1.push(Math.round(parseFloat(jsonObj["Technical Analysis: "+name][days[i]]["MACD_Signal"])*100)/100);
                prices2.push(Math.round(parseFloat(jsonObj["Technical Analysis: "+name][days[i]]["MACD_Hist"])*100)/100);
                prices3.push(Math.round(parseFloat(jsonObj["Technical Analysis: "+name][days[i]]["MACD"])*100)/100);
                if (prices1[days.length-1-i] < minpriceL){
                    minpriceL = prices1[days.length-1-i];
                }
                if (prices2[days.length-1-i] < minpriceL){
                    minpriceL = prices2[days.length-1-i];
                }
                if (prices3[days.length-1-i] < minpriceL){
                    minpriceL = prices3[days.length-1-i];
                }
            }
            prices.push(prices1);
            prices.push(prices2);
            prices.push(prices3);
        }  
        else if (name == "STOCH"){
            minpriceL = 0;
            var snames = ["SlowD", "SlowK"];
            var prices1 = [];
            var prices2 = [];
            for (i = days.length - 1; i >= 0; i--){
                xdates.push(days[i].substr(5, 5));
                prices1.push(Math.round(parseFloat(jsonObj["Technical Analysis: "+name][days[i]]["SlowD"])*100)/100);
                prices2.push(Math.round(parseFloat(jsonObj["Technical Analysis: "+name][days[i]]["SlowK"])*100)/100);
            }
            prices.push(prices1);
            prices.push(prices2);
        }
        else{
            var prices1 = [];
            for (i = days.length - 1; i >= 0; i--){
                xdates.push(days[i].substr(5, 5));
                prices1.push(Math.round(parseFloat(jsonObj["Technical Analysis: "+name][days[i]][name])*100)/100);
                if (prices1[days.length-1-i] < minpriceL){
                    minpriceL = prices1[days.length-1-i];
                }
            }
            prices.push(prices1);
        }
        
        if (name == "BBANDS" || name == "MACD"){
            $scope.chartData[name] = {
                "chart": {
                    "borderWidth": 2,
                    "borderColor": '#cecece',
                    "zoomType": 'x'
                },
                "title": {
                    "text": indicator
                },
                "subtitle": {
                    "useHTML": true,
                    "text": '<a style="color:blue" href="https://www.alphavantage.co/" target="_blank">' + 'Source: Alpha Vantage</a>'
                },
                "xAxis": {
                    "categories": xdates,
                    "tickInterval": 1
                },
                "yAxis": {
                    "min": minpriceL,
                    "title": {
                        "text": name
                    }
                },
                "series": [{
                    "name": symbol + ' ' + snames[0],
                    "data": prices[0]
                },{
                    "name": symbol + ' ' + snames[1],
                    "data": prices[1]
                },{
                    "name": symbol + ' ' + snames[2],
                    "data": prices[2]
                }]
            };
            Highcharts.chart('curChartContainer', $scope.chartData[name]);
        }
        else if(name == "STOCH"){
            $scope.chartData[name] = {
                "chart": {
                    "borderWidth": 2,
                    "borderColor": '#cecece',
                    "zoomType": 'x'
                },
                "title": {
                    "text": indicator
                },
                "subtitle": {
                    "useHTML": true,
                    "text": '<a style="color:blue" href="https://www.alphavantage.co/" target="_blank">' + 'Source: Alpha Vantage</a>'
                },
                "xAxis": {
                    "categories": xdates,
                    "tickInterval": 1
                },
                "yAxis": {
                    "min": minpriceL,
                    "title": {
                        "text": name
                    }
                },
                "series": [{
                    "name": symbol + ' ' + snames[0],
                    "data": prices[0]
                },{
                    "name": symbol + ' ' + snames[1],
                    "data": prices[1]
                }]
            };
            Highcharts.chart('curChartContainer', $scope.chartData[name]);
        }
        else{
            $scope.chartData[name] = {
                "chart": {
                    "borderWidth": 2,
                    "borderColor": '#cecece',
                    "zoomType": 'x'
                },
                "title": {
                    "text": indicator
                },
                "subtitle": {
                    "useHTML": true,
                    "text": '<a style="color:blue" href="https://www.alphavantage.co/" target="_blank">' + 'Source: Alpha Vantage</a>'
                },
                "xAxis": {
                    "categories": xdates,
                    "tickInterval": 1
                },
                "yAxis": {
                    "min": minpriceL,
                    "title": {
                        "text": name
                    }
                },
                "series": [{
                    "name": symbol,
                    "data": prices[0]
                }]
            };
            Highcharts.chart('curChartContainer', $scope.chartData[name]);
        }
    }
    
    
    $scope.showCurrent = function(){
        $scope.activePil = "cur";
        $scope.switchCurrent = true;
        $scope.switchHistory = false;
        $scope.switchNews = false;
    };
    $scope.showHistory = function(){
        $scope.activePil = "his";
        $scope.switchCurrent = false;
        $scope.switchHistory = true;
        $scope.switchNews = false;
    };
    $scope.showNews = function(){
        $scope.activePil = "new";
        $scope.switchCurrent = false;
        $scope.switchHistory = false;
        $scope.switchNews = true;
    };
    
    
    function drawHistoryChart(){
        var l = $scope.priceDates.length > 1000 ? 1000 : $scope.priceDates.length;
        var dates = $scope.priceDates.slice(0, l);
        var prices = $scope.priceData.slice(0, l);
        var hisData = [];
        for (i = dates.length - 1; i >= 0; i--){
            var curTime = new Date(dates[i]).getTime();
            var curPrice = Math.round(parseFloat(prices[i]["4. close"])*100)/100;
            var curData = [];
            curData.push(curTime);
            curData.push(curPrice);
            hisData.push(curData);
        }
        
        Highcharts.stockChart('hisContainer', {
            chart: {
                zoomType: 'x'
            },
            title: {
                text: $scope.currentStock["symbol"] + ' Stock Value'
            },
            tooltip: {
                crosshairs: [false,false],
				split: false
			},
            subtitle: {
                "useHTML": true,
                text: '<a style="color:blue" href="https://www.alphavantage.co/" target="_blank">' + 'Source: Alpha Vantage</a>'
            },
            xAxis: {
                type: 'datetime'
            },
            yAxis: {
                min: 0,
                title: {
					"text": "Stock Value"
				},
				opposite: true
            },
            rangeSelector: {
                buttons: [{
                    type: 'week',
                    count: 1,
                    text: '1w'
                }, {
                    type: 'month',
                    count: 1,
                    text: '1m'
                }, {
                    type: 'month',
                    count: 3,
                    text: '3m'
                }, {
                    type: 'month',
                    count: 6,
                    text: '6m'
                }, {
                    type: 'ytd',
                    text: 'YTD'
                }, {
                    type: 'year',
                    count: 1,
                    text: '1y'
                }, {
                    type: 'all',
                    text: 'ALL'
                }],
                selected: 0
            },
            series: [{
                name: $scope.currentStock["symbol"],
                type: 'area',
                threshold: null,
                tooltip: {
					valueDecimals: 2
				},
                data: hisData
            }]
        });
    }
        
    
    function parseNewsData(data){
        if(data != null && data["rss"]){
            var news = data["rss"]["channel"]["0"]["item"];
            var nums = 0;
            for (var key in news){
                if (news[key]["link"]["0"].indexOf("article") !== -1){
                    var link = news[key]["link"]["0"];
                    var title = news[key]["title"]["0"];
                    var author = news[key]["sa:author_name"]["0"];
                    var pubDate = news[key]["pubDate"][0].split(" ").slice(0,-1).join(" ")+" EST";
                    $scope.newsData.push({"link": link, "title": title, "author": author, "pubDate": pubDate});
                    nums += 1;
                    if (nums === 5) break;
                }
            }
            $scope.showNewsProgress = false;
            $scope.showNewsData = true;
        }
        else{
            $scope.showNewsProgress = false;
            $scope.showNewsError = true;
        }
    }
    
    
    function generateFavoriteList(){
		$scope.favoriteList = [];
		for(var i = 0; i < $window.localStorage.length; i++){
            var key = $window.localStorage.key(i)
			$scope.favoriteList.push(angular.fromJson($window.localStorage.getItem(key)));
		}
	}
    
    
    $scope.switchFavorite = function(){
        generateFavoriteList();
        $scope.panelSwitch = false;
    };
    
    
    $scope.addFavorite = function(){
        var starEle = angular.element(document.querySelector('#star'));
        if(starEle.attr("class") === "glyphicon glyphicon-star-empty"){
            var color = "";
            var imgsrc = "";
            if ($scope.currentStock["change"][0] === '-'){
                color = "red";
                imgsrc = "/down.png";
            }
            else{
                color = "green";
                imgsrc = "/up.png";
            }
            var change = Math.round((parseFloat($scope.priceData[0]["4. close"]) - parseFloat($scope.priceData[1]["4. close"]))*100)/100;
            var changePercent = Math.round((parseFloat($scope.priceData[0]["4. close"]) - parseFloat($scope.priceData[1]["4. close"])) / parseFloat($scope.priceData[1]["4. close"])*10000)/100;
            var value = {
                "symbol": $scope.currentStock["symbol"],
                "price": parseFloat($scope.currentStock["lastPrice"]),
                "change": change,
                "changePercent": changePercent,
                "volume": $scope.priceData[0]["5. volume"],
                "color": color,
                "arrow": imgsrc
            };
            value = angular.toJson(value);
            $window.localStorage.setItem($scope.currentStock["symbol"], value);
            console.log($scope.currentStock["symbol"] + " added");
            starEle.attr('class', "glyphicon glyphicon-star");
            starEle.attr('style', "color: orange");
        }
        else{
            $window.localStorage.removeItem($scope.currentStock["symbol"]);
            starEle.attr('class', "glyphicon glyphicon-star-empty");
			starEle.removeAttr('style');
        }
    };
    
    
    $scope.removeFavorite = function(symbol){
        if($window.localStorage[symbol]){
			$window.localStorage.removeItem(symbol);
            console.log(symbol + " removed");
            generateFavoriteList();
        }
    };
    
    
    $scope.refresh = function(){
        console.log("refreshing");
        for(var i in $window.localStorage){
            var value = angular.fromJson($window.localStorage[i]);
            var symbol = value["symbol"];
            var promise = stockService.stockQuery(symbol);
            promise.then(function(data) {
                updateFavData(data);
            });
        }
    };
    
    
    function updateFavData(data){
        try{
            var jsonObj = JSON.parse(data);
        }catch (e){}
        if(jsonObj != null && jsonObj["Meta Data"]){
            var symbol = jsonObj["Meta Data"]["2. Symbol"];
			var priceData = Object.values(jsonObj["Time Series (Daily)"]);
            var price = Math.round(parseFloat(priceData[0]["4. close"])*100)/100;
            var change = Math.round((parseFloat(priceData[0]["4. close"]) - parseFloat(priceData[1]["4. close"]))*100)/100;
            var changePercent = Math.round((parseFloat(priceData[0]["4. close"]) - parseFloat(priceData[1]["4. close"])) / parseFloat(priceData[1]["4. close"])*10000)/100;
            var volume = priceData[0]["5. volume"];
            var color = "";
            var imgsrc = "";
            if (change < 0 ){
                color = "red";
                imgsrc = "/down.png";
            }
            else{
                color = "green";
                imgsrc = "/up.png";
            }
            var value = {
                "symbol": symbol,
                "price": price,
                "change": change,
                "changePercent": changePercent,
                "volume": volume,
                "color": color,
                "arrow": imgsrc
            };
            value = angular.toJson(value);
            $window.localStorage.setItem(symbol, value);
            console.log(symbol + " refreshed");
            generateFavoriteList();
        }
    }
    
    
    var timer;
    $('#autoRefresh').change(function() {
        if ($(this).is(':checked')) {
            timer = setInterval(function() {
              $scope.refresh();
            }, 5000);
        } else {
            clearInterval(timer);
        }
    });
    
    
    $scope.shareFacebook = function(){
		var picUrl = "";
        console.log("sharing " + $scope.activeTab);
        var picData = $scope.chartData[$scope.activeTab];
        var exportUrl = 'https://export.highcharts.com/';
		var obj = {};
		obj.options = JSON.stringify(picData);
    	obj.type = 'image/png';
    	obj.async = true;
        var promise = stockService.getChart(obj);
        promise.then(function(data) {
            picUrl = exportUrl + data;
            console.log("pic created: " + picUrl);
            FB.ui({ 
                app_id: "1627643077294461", 
                method: 'feed', 
                picture: picUrl
            },function(response){
                if(response && !response.error_message){ 
                    alert("Post Succeed");
                }else{
                    alert("Post Failed");
                }
            });
        });      
	};
});