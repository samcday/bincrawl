'use strict'

angular.module('adminuiApp').controller 'GroupsCtrl', ($scope, socket, $dialog) ->
	inst = this
	$scope.groups = {}

	socket.emit "interested", "groups"
	socketReg = socket.on "groupUpdate", (data) ->
		for groupName of data
			$scope.groups[groupName] = group = data[groupName]
			group.firstPostDate = new Date(group.firstPostDate) if group.firstPostDate
			group.lastPostDate = new Date(group.lastPostDate) if group.lastPostDate

	socketReg = socket.on "groupActivity", (data) ->
		for group, flags of data
			$scope.groups[group].updating = flags.updating
			$scope.groups[group].backfilling = flags.backfilling

	$scope.$on "$destroy", ->
		socket.leave "groups"
		socketReg.off()

	$scope.addGroup = ->
		d = $dialog.dialog
			templateUrl: "views/addgroup.html"
			controller: "AddGroupCtrl"
			backdrop: true
			keyboard: false
			backdropClick: false
		d.open().then ->
			console.log arguments
