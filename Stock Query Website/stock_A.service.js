stockApp.service('stockService', ['$http', '$q', function ($http, $q) {
    return{
        completeQuery: function(symbol) {
            var deferred = $q.defer();
            var queryURL = "/complete";
            $http.post(queryURL, symbol).then(function (result) {
                deferred.resolve(result.data);
            }).catch(function (result) {
                deferred.reject(result); 
            }); 
            return deferred.promise; 
        },
        
        
        stockQuery: function(symbol) {
            var deferred = $q.defer();
            var queryURL = "/queryStock";
            $http.get(queryURL, {  
                params: {  
                    "symbol": symbol,   
                }  
            }).then(function (result) {
                deferred.resolve(result.data);
            }).catch(function (result) {
                deferred.reject(result); 
            }); 
            return deferred.promise; 
        },
        
        
        indicatorQuery: function(symbol) {
            var deferred = $q.defer();
            var queryURL = "/queryIndicator";
            $http.get(queryURL, {  
                params: {  
                    "symbol": symbol,   
                }  
            }).then(function (result) {
                deferred.resolve(result.data);
            }).catch(function (result) {
                deferred.reject(result); 
            }); 
            return deferred.promise; 
        },
        
        
        newsQuery: function(symbol) {
            var deferred = $q.defer();
            var queryURL = "/queryNews";
            $http.get(queryURL, {  
                params: {  
                    "symbol": symbol,   
                }  
            }).then(function (result) {
                deferred.resolve(result.data);
            }).catch(function (result) {
                deferred.reject(result); 
            }); 
            return deferred.promise; 
        },
        
        
        getChart: function(obj) {
            var deferred = $q.defer();
            var queryURL = "/getChart";
            $http.post(queryURL, obj).then(function (result) {
                deferred.resolve(result.data);
            }).catch(function (result) {
                deferred.reject(result); 
            }); 
            return deferred.promise;
        }
    };
}]);