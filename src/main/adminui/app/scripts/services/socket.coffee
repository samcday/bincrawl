'use strict';

angular.module('adminuiApp').factory 'socket', ($rootScope) ->
	socket = io.connect "http://localhost:1339"

	socket.on "connect", ->
		console.log arguments

	{
		on: (eventName, callback) ->
			socket.on eventName, ->
				args = arguments
				$rootScope.$apply ->
					callback.apply socket, args
		emit: (eventName, data, callback) ->
			socket.emit eventName, data, ->
				args = arguments
				$rootScope.$apply ->
					callback.apply socket, args  if callback
	}
