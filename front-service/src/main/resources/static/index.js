(function () {
    angular
        .module('market-front', ['ngRoute', 'ngStorage'])
        .config(config)
        .run(run);

    function config($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl: 'welcome/welcome.html',
                controller: 'welcomeController'
            })
            .when('/store', {
                templateUrl: 'store/store.html',
                controller: 'storeController'
            })
            .when('/cart', {
                templateUrl: 'cart/cart.html',
                controller: 'cartController'
            })
            .when('/orders', {
                templateUrl: 'order/orders.html',
                controller: 'ordersController'
            })
            .when('/order_pay/:orderId', {
                templateUrl: 'order_pay/order_pay.html',
                controller: 'orderPayController'
            })
            .when('/registration', {
                templateUrl: 'registration/registration.html',
                controller: 'registrationController'
            })
            .when('/admin', {
                templateUrl: 'admin/admin.html',
                controller: 'adminController'
            })
            .when('/product_form/\\w+', {
                templateUrl: 'product/product_form.html',
                controller: 'productFormController'
            })
            .otherwise({
                redirectTo: '/'
            });
    }

    function run($rootScope, $http, $localStorage) {
        if ($localStorage.springWebUser) {
            try {
                let jwt = $localStorage.springWebUser.token;
                let payload = JSON.parse(atob(jwt.split('.')[1]));
                let currentTime = parseInt(new Date().getTime() / 1000);
                if (currentTime > payload.exp) {
                    console.log("Token is expired!!!");
                    delete $localStorage.springWebUser;
                    $http.defaults.headers.common.Authorization = '';
                }
            } catch (e) {
            }

            if ($localStorage.springWebUser) {
                $http.defaults.headers.common.Authorization = 'Bearer ' + $localStorage.springWebUser.token;
            }
        }
        if (!$localStorage.springWebGuestCartId) {
            $http.get('http://localhost:5555/cart/api/v1/carts/generate')
                .then(function successCallback(response) {
                    $localStorage.springWebGuestCartId = response.data.value;
                });
        }
    }
})();

angular.module('market-front').controller('indexController', function ($rootScope, $scope, $http, $location, $localStorage) {

    $rootScope.listRoles = new Set();

        $scope.tryToAuth = function () {
        $http.post('http://localhost:5555/auth/auth', $scope.user)
            .then(function successCallback(response) {
                if (response.data.token) {
                    $http.defaults.headers.common.Authorization = 'Bearer ' + response.data.token;
                    $localStorage.springWebUser = {username: $scope.user.username, token: response.data.token, listRoles: response.data.list};

                    $scope.user.username = null;
                    $scope.user.password = null;

                    $http.get('http://localhost:5555/cart/api/v1/carts/' + $localStorage.springWebGuestCartId + '/merge')
                        .then(function successCallback(response) {
                        });

                    $location.path('/');
                }
            }, function errorCallback(response) {
                alert("Error: " + response.data.message);
            });
    };

    $rootScope.tryToLogout = function () {
        $scope.listRoles.clear();
        $scope.clearUser();
        $scope.user = null;
        $location.path('/');
    };

    $scope.clearUser = function () {
        delete $localStorage.springWebUser;
        $http.defaults.headers.common.Authorization = '';
    };

    $rootScope.isUserLoggedIn = function () {
        return !!$localStorage.springWebUser;
    };

    $rootScope.isUserHasAdminRole = function () {
        if (!$rootScope.isUserLoggedIn()){
            return false;
        }
        $localStorage.springWebUser.listRoles.forEach($scope.listRoles.add, $scope.listRoles)
        return $rootScope.listRoles.has('ROLE_ADMIN');
    };
});