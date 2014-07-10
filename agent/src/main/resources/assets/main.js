var brokerApp = angular.module('brokerApp', [
  'ngRoute'
]);


brokerApp.filter('escape', function() {
  return  encodeURIComponent;
});





brokerApp.filter('encode64', function() {
  return   function encode64(input) {

                var keyStr = "ABCDEFGHIJKLMNOP" +

                 "QRSTUVWXYZabcdef" +

                 "ghijklmnopqrstuv" +

                 "wxyz0123456789+/" +

                 "=";
               input = escape(input);
               var output = "";
               var chr1, chr2, chr3 = "";
               var enc1, enc2, enc3, enc4 = "";
               var i = 0;

               do {
                  chr1 = input.charCodeAt(i++);
                  chr2 = input.charCodeAt(i++);
                  chr3 = input.charCodeAt(i++);

                  enc1 = chr1 >> 2;
                  enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
                  enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
                  enc4 = chr3 & 63;

                  if (isNaN(chr2)) {
                     enc3 = enc4 = 64;
                  } else if (isNaN(chr3)) {
                     enc4 = 64;
                  }

                  output = output +
                     keyStr.charAt(enc1) +
                     keyStr.charAt(enc2) +
                     keyStr.charAt(enc3) +
                     keyStr.charAt(enc4);
                  chr1 = chr2 = chr3 = "";
                  enc1 = enc2 = enc3 = enc4 = "";
               } while (i < input.length);

               return output;
    };
});

var decode64 =  function(input) {
                     var keyStr = "ABCDEFGHIJKLMNOP" +

                                   "QRSTUVWXYZabcdef" +

                                   "ghijklmnopqrstuv" +

                                   "wxyz0123456789+/" +

                                   "=";
                    var output = "";
                    var chr1, chr2, chr3 = "";
                    var enc1, enc2, enc3, enc4 = "";
                    var i = 0;

                    // remove all characters that are not A-Z, a-z, 0-9, +, /, or =
                    var base64test = /[^A-Za-z0-9\+\/\=]/g;
                    if (base64test.exec(input)) {
                       alert("There were invalid base64 characters in the input text.\n" +
                             "Valid base64 characters are A-Z, a-z, 0-9, '+', '/',and '='\n" +
                             "Expect errors in decoding.");
                    }
                    input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");

                    do {
                       enc1 = keyStr.indexOf(input.charAt(i++));
                       enc2 = keyStr.indexOf(input.charAt(i++));
                       enc3 = keyStr.indexOf(input.charAt(i++));
                       enc4 = keyStr.indexOf(input.charAt(i++));

                       chr1 = (enc1 << 2) | (enc2 >> 4);
                       chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
                       chr3 = ((enc3 & 3) << 6) | enc4;

                       output = output + String.fromCharCode(chr1);

                       if (enc3 != 64) {
                          output = output + String.fromCharCode(chr2);
                       }
                       if (enc4 != 64) {
                          output = output + String.fromCharCode(chr3);
                       }

                       chr1 = chr2 = chr3 = "";
                       enc1 = enc2 = enc3 = enc4 = "";

                    } while (i < input.length);

                    return unescape(output);
                 }

brokerApp.filter('decode64', function() {
  return decode64;
});



brokerApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
      when('/', {
        templateUrl: 'assets/app/pages/home.html',
      }).
      when('/queues', {
         templateUrl: 'assets/app/pages/queues.html',
      }).
      when('/queues/:name/messages', {
               templateUrl: 'assets/app/pages/messages.html',
               controller: 'MessagesCtrl'
      }).
      when('/network', {
               templateUrl: 'assets/app/pages/agents.html',
      }).
       when('/agent', {
                     templateUrl: 'assets/app/pages/agent.html',
      }).
      otherwise({
         templateUrl: 'assets/app/pages/notfound.html',
      });
  }]);

brokerApp.controller('MessagesCtrl', ['$scope', '$routeParams','$sce',
    function($scope, $routeParams,$sce) {

      var queueName = decode64($routeParams.name);

      $scope.queueName = queueName;

      $scope.broker_url= "/broker/queues/"+encodeURIComponent(queueName)+"/messages";

      $scope.showModal = function(message){

            $scope.payload = decode64(message.action.notificationMessage.message.payload);
            message.showmodal = true;

      };



    }]);

brokerApp.controller('queueController', function($scope,$http) {


        $scope.deleteQueue = function(queue, dataBroker){


            $scope.showModal(function(){

                var deletePromise = $http.delete("/broker/queues/".concat(encodeURIComponent(queue.name)).concat("?force=true"));

                deletePromise.success(function(data, status, headers, config) {

                    var index = dataBroker.indexOf(queue);

                    console.log(index);

                    if( index > -1 ){
                        dataBroker.splice(index, 1);
                    }

                });

            });

        };


});


brokerApp.controller('mainController', function($scope,$http) {


        $scope.shutdown = function(){

                $scope.showModal(function(){

                    var deletePromise = $http.post("/broker/agents/self/shutdown");

                    deletePromise.success(function(data, status, headers, config) {

                    });

                });

        };



})




brokerApp.directive('broker', ['$http','$timeout', function($http,$timeout) {

          return {
            scope : true,
            restrict: 'E',
            link: function (scope, element, attrs) {
                var source  = element.attr('src');

                if(!source){
                      var source = scope.broker_url;
                }


                var timeout = null;

                var request = function() {
                    var responsePromise = $http.get(source);

                     responsePromise.success(function(data, status, headers, config) {
                        scope.data = data;
                        if(attrs.poll!="false"){
                            timeout = $timeout(request, 3000)
                        }


                     }).error(function(data, status, headers, config) {

                        if(attrs.poll!="false"){
                            timeout = $timeout(request, 3000)
                        }
                     });

                }

                request();

                scope.$on('$destroy', function() {
                        $timeout.cancel(timeout);
                });

            },
          };
}]);


brokerApp.directive('yesNoModal', function() {

        var link = function (scope, element, attrs) {

            Ink.requireModules( ['Ink.Dom.Selector_1','Ink.UI.Modal_1','Ink.Dom.Event_1'], function( Selector, Modal , Event ){

                  var modalElement = element.children(1).children(0);

                  var modalObj = new Modal( modalElement , { autoDisplay : false} );

                  scope.showModal = function(yesFunc,noFunc){

                      scope.no= function(){
                         if(noFunc){
                          noFunc();
                         }
                      };

                      scope.yes= function(){
                        if(yesFunc){
                            yesFunc();
                        }
                      };

                      modalObj.open();
                   };


            });



        };

        return {
            restrict: 'E',
            link: link,
            templateUrl: 'assets/app/parts/yesnomodal.html'
        }

});

brokerApp.directive('modal', function() {


        var link = function (scope, element, attrs) {

            Ink.requireModules( ['Ink.Dom.Selector_1','Ink.UI.Modal_1','Ink.Dom.Event_1'], function( Selector, Modal , Event ){

                  var modalElement = element.children(1).children(0);

                  var modalObj = new Modal( modalElement , { autoDisplay : false} );

                  scope.$watch('show', function(showmodal) {

                        if(showmodal){


                            modalObj.open();

                            scope.show = false;
                        }
                  });


            });



        };

        return {
            restrict: 'E',
            scope : {
                show : "=",
                title : "="
            },
            link: link,
            transclude: true,
            templateUrl: 'assets/app/parts/modal.html',
        }

});





