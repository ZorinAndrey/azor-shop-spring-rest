angular.module('market-front').controller('storeController', function ($scope, $http, $location, $localStorage) {
    const contextPath = 'http://localhost:5555/core/';

    $scope.loadProducts = function (pageIndex = 1) {
        $http({
            url: contextPath + 'api/v1/products',
            method: 'GET',
            params: {
                p: pageIndex,
                title_part: $scope.filter ? $scope.filter.title_part : null,
                category_title: $scope.filter ? $scope.filter.category_title : null,
                min_price: $scope.filter ? $scope.filter.min_price : null,
                max_price: $scope.filter ? $scope.filter.max_price : null
            }
        }).then(function (response) {

            $scope.ProductsPage = response.data;
            $scope.paginationArray = $scope.generatePagesIndexes(1, $scope.ProductsPage.totalPages);
        });
    };

    $scope.loadDailyRecommendations = function () {
        $http({
            url: 'http://localhost:5555/recom/api/v1/recom/daily',
            method: 'GET'
        }).then(function (response) {
            $scope.dailyRecomList = response.data;
        });
    };

    $scope.loadMonthlyRecommendations = function () {
        $http({
            url: 'http://localhost:5555/recom/api/v1/recom/monthly',
            method: 'GET'
        }).then(function (response) {
            $scope.monthlyRecomList = response.data;
        });
    };

    $scope.generatePagesIndexes = function (startPage, endPage) {
        let arr = [];
        for (let i = startPage; i < endPage + 1; i++) {
            arr.push(i);
        }
        return arr;
    }

    $scope.addToCart = function (productId) {
        $http.get('http://localhost:5555/cart/api/v1/carts/' + $localStorage.springWebGuestCartId + '/add/' + productId)
            .then(function (response) {
            });
    }

    $scope.goToProductForm = function (productId) {
        $location.path('/product_form/' + productId);
    }

    $scope.deleteProduct = function (productId) {
        if(confirm('The product ' + productId + ' wil be removed. Are you sure?')){
            $http.delete(contextPath + 'api/v1/products/' + productId)
                .then(function (response) {
                    $scope.loadProducts();
                });
        }
    }

    $scope.loadDailyRecommendations();
    $scope.loadMonthlyRecommendations();
    $scope.loadProducts();
});