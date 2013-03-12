'use strict'

angular.module('adminuiApp', [])
  .config ($routeProvider) ->
    $routeProvider
      .when '/',
        templateUrl: 'views/main.html'
        controller: 'MainCtrl'
      .when '/groups',
      	templateUrl: 'views/groups.html',
      	controller: 'GroupsCtrl'
      .otherwise
        redirectTo: '/'
