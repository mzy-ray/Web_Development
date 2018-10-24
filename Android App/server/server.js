var express = require('express');
var app = express();
var http = require('http'); 
var https = require('https');
var async = require('async');
var xml2js = require('xml2js');
var parser = new xml2js.Parser();

app.use (function(req, res, next) {
    let data='';
    req.setEncoding('utf8');
    req.on('data', function(chunk) { 
       data += chunk;
    });
    req.on('end', function() {
        req.body = data;
        next();
    });
});

 
app.get('/queryStock', function (req, res) {
    var url = 'https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol='  + req.query.symbol + '&outputsize=full&apikey=XN5FGV31OE9T01TP';

    https.get(url, function(response){
        var data = '';
        if (response.statusCode === 200){
            response.on('data', function(chunk) {
                data += chunk; 
            });
            response.on('end', function() {
                data = JSON.parse(data)
                res.json(data);
            });
        }
    }).on('error', function(err) {
        res.json(err.message);
    });
    
//    var testUrl = 'http://127.0.0.1:8080/test.json';
//    http.get(testUrl, function(response){
//        var data = '';
//        if (response.statusCode === 200){
//            response.on('data', function(chunk) {
//                data += chunk; 
//            });
//            response.on('end', function() {
//                res.json(data);
//            });
//        }
//    }).on('error', function(err) {
//        res.json(err.message);
//    });
});


app.get('/queryLatest', function (req, res) {
    var url = 'https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol='  + req.query.symbol + '&outputsize=compact&apikey=XN5FGV31OE9T01TP';

    https.get(url, function(response){
        var data = '';
        if (response.statusCode === 200){
            response.on('data', function(chunk) {
                data += chunk; 
            });
            response.on('end', function() {
                data = JSON.parse(data)
                res.json(data);
            });
        }
    }).on('error', function(err) {
        res.json(err.message);
    });
});



app.get('/queryIndicator', function (req, res) {
    var queryData = {};
    var queries = [];
    queries.push({'name': 'SMA', 'url': 'https://www.alphavantage.co/query?function=SMA&symbol=' + req.query.symbol + '&interval=daily&time_period=10&series_type=close&apikey=XN5FGV31OE9T01TP'});
    queries.push({'name': 'EMA', 'url': 'https://www.alphavantage.co/query?function=EMA&symbol=' + req.query.symbol + '&interval=daily&time_period=10&series_type=close&apikey=XN5FGV31OE9T01TP'});
    queries.push({'name': 'RSI', 'url': 'https://www.alphavantage.co/query?function=RSI&symbol=' + req.query.symbol + '&interval=daily&time_period=10&series_type=close&apikey=XN5FGV31OE9T01TP'});
    queries.push({'name': 'ADX', 'url': 'https://www.alphavantage.co/query?function=ADX&symbol=' + req.query.symbol + '&interval=daily&time_period=10&series_type=close&apikey=XN5FGV31OE9T01TP'});
    queries.push({'name': 'CCI', 'url': 'https://www.alphavantage.co/query?function=CCI&symbol=' + req.query.symbol + '&interval=daily&time_period=10&series_type=close&apikey=XN5FGV31OE9T01TP'});
    queries.push({'name': 'MACD', 'url': 'https://www.alphavantage.co/query?function=MACD&symbol=' + req.query.symbol + '&interval=daily&time_period=10&series_type=close&apikey=XN5FGV31OE9T01TP'});
    queries.push({'name': 'STOCH', 'url': 'https://www.alphavantage.co/query?function=STOCH&symbol=' + req.query.symbol + '&interval=daily&slowkmatype=1&slowdmatype=1&time_period=10&series_type=close&apikey=XN5FGV31OE9T01TP'});
    queries.push({'name': 'BBANDS', 'url': 'https://www.alphavantage.co/query?function=BBANDS&symbol=' + req.query.symbol + '&interval=daily&nbdevup=3&nbdevdn=3&time_period=5&series_type=close&apikey=XN5FGV31OE9T01TP'});

    async.each(queries, function(query, callback) {
        https.get(query.url, function(response){
            var data = '';
            if (response.statusCode === 200){
                response.on('data', function(chunk) {
                    data += chunk; 
                });
                response.on('end', function() {
                    queryData[query.name] = JSON.parse(data);
                    callback(null, response);
                });
            }
        }).on('error', function(err) {
            queryData[query.name] = err.message;
        });
    }, function(err) {
        res.json(queryData);
    });    
});


app.get('/queryNews', function (req, res) {
    var url = 'https://seekingalpha.com/api/sa/combined/'  + req.query.symbol + '.xml';

    https.get(url, function(response){
        var data = '';
        if (response.statusCode === 200){
            response.on('data', function(chunk) {
                data += chunk; 
            });
            response.on('end', function() {
                parser.parseString(data, function(err, result) {
                    if (err) {
                      res.json(err.message);
                    } else {
                      res.json(result);
                    }
                });
            });
        }
    }).on('error', function(err) {
        res.json(err.message);
    });
});


app.get('/complete', function (req, res) {
    let url = 'http://dev.markitondemand.com/MODApis/Api/v2/Lookup/json?input=' + req.query.symbol;
    http.get(url, function(response){
        symbolData = '';
        if (response.statusCode === 200){
            response.on('data', function(chunk) {
                symbolData += chunk; 
            });
            response.on('end', function() {
                symbolData = JSON.parse(symbolData)
                res.json(symbolData);
            });
        }
    }).on('error', function(err) {
        res.json(err.message);
    });
});


app.post('/getChart', function (req, res) {
    let url = 'https://export.highcharts.com/';
    
    res.end();
});
 

app.use(express.static(__dirname)).listen(8081);