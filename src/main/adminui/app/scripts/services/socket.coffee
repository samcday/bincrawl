'use strict';

angular.module('adminuiApp').factory 'socket', ($rootScope) ->
	socket = io.connect "http://localhost:1339"

	{
		on: (eventName, callback) ->
			handler = ->
				args = arguments
				$rootScope.$apply ->
					callback.apply socket, args
			socket.on eventName, handler
			return {
				off: ->
					socket.removeListener eventName, handler
			}
		emit: (eventName, data, callback) ->
			socket.emit eventName, data, ->
				args = arguments
				$rootScope.$apply ->
					callback.apply socket, args  if callback
	}
