'use strict'

angular.module('adminuiApp').controller 'BandwidthCtrl', ($scope, socket) ->
	$scope.upload = $scope.download = 0
	socket.on "bandwidth", (info) ->
		{read, write} = info
		$scope.upload = write
		$scope.download = read
