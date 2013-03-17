'use strict'

angular.module('adminuiApp').controller 'AddGroupCtrl', ($scope, dialog, GroupResource) ->
	$scope.cancel = -> dialog.close()
	$scope.submit = ->
		GroupResource.save $scope.group, ->
			dialog.close()