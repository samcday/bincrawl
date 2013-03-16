'use strict'

config = ($routeProvider) ->
	$routeProvider
		.when '/',
			templateUrl: 'views/main.html'
			controller: 'MainCtrl'
		.when '/groups',
			templateUrl: 'views/groups.html',
			controller: 'GroupsCtrl'
		.otherwise
			redirectTo: '/'

run = ($rootScope, $dialog, $http, base64, $q) ->
	$rootScope.auth = 
		user: "admin"
		pass: "admin"
		loggedIn: true
		showLogin: ->
			d = $dialog.dialog
				templateUrl: "views/login.html"
				controller: "LoginCtrl"
				backdrop: true
				keyboard: false
				backdropClick: false
			d.open()

	$rootScope.$on "event:auth-loginRequired", ->
		$rootScope.auth.showLogin()

	$rootScope.$on "event:auth-loginConfirmed", ->
		$rootScope.auth.loggedIn = true

	$http.defaults.transformRequest.push (data, headers) ->
		{user, pass} = $rootScope.auth
		if user and pass
			authString = base64.encode "#{user}:#{pass}"
			headers().Authorization = "Basic #{authString}"
		return data

angular.module('adminuiApp', ["ngResource", "ui.bootstrap", 'http-auth-interceptor']).config(config).run(run)
	.factory "GroupResource", ($resource, $rootScope) ->
		GroupsResource = $resource "http://localhost\\:1337/group"
		return GroupsResource
