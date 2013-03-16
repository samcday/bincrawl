'use strict'

angular.module('adminuiApp').controller 'AddGroupCtrl', ($scope, dialog, GroupResource) ->
	$scope.cancel = -> dialog.close()
	$scope.submit = ->
		console.log GroupResource.save
		GroupResource.save $scope.group