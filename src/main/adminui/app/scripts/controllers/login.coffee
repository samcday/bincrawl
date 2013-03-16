'use strict'

angular.module('adminuiApp').controller 'LoginCtrl', ($scope, $http, base64, dialog, authService, $rootScope) ->
	$scope.username = $scope.password = "admin"

	$scope.cancel = ->
		$scope.invalidLogin = false
		return if $scope.loggingIn
		dialog.close()
	$scope.submit = ->
		return if $scope.loggingIn
		$scope.invalidLogin = false
		$scope.loggingIn = true
		$rootScope.auth.user = $scope.username
		$rootScope.auth.pass = $scope.password

		# TODO: base url in config somewhere..
		($http.get "http://localhost:1337/", loginAttempt: true).then ->
			$scope.loggingIn = false
			authService.loginConfirmed()
			dialog.close()
		, ->
			$scope.loggingIn = false
			$scope.invalidLogin = true
