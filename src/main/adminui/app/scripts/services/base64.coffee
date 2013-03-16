'use strict';

angular.module('adminuiApp').factory 'base64', ($window) ->
	{
		encode: (str) ->
			return $window.btoa str
		decode: (str) ->
			return $window.atob str
	}
