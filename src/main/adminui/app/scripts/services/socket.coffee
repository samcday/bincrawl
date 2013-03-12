'use strict';

angular.module('adminuiApp').factory 'socket', () ->
	socket = io.connect "http://localhost:1339", resource: "test"

	{
		on: (eventName, callback) ->
			socket.on eventName, ->
				args = arguments_
				$rootScope.$apply ->
					callback.apply socket, args
		emit: (eventName, data, callback) ->
			socket.emit eventName, data, ->
				args = arguments_
				$rootScope.$apply ->
					callback.apply socket, args  if callback
	}
