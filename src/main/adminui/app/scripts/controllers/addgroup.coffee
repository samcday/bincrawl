'use strict'

angular.module('adminuiApp').controller 'AddGroupCtrl', ($scope, dialog, GroupResource) ->
	$scope.cancel = ->
		$scope.submitting = false
		dialog.close()
	$scope.submit = ->
		$scope.submitting = true
		GroupResource.save $scope.group, ->
			$scope.submitting = false
			dialog.close()