'use strict'

angular.module('adminuiApp').controller 'GroupsCtrl', ($scope, socket) ->
	$scope.groups = [
		{
			name: "alt.binaries.hdtv"
			start: new Date(2001, 10, 10)
			low: "12345"
			high: "54321"
		}
		{
			name: "alt.binaries.teevee"
			low: "12345"
			high: "54321"
		}
	]