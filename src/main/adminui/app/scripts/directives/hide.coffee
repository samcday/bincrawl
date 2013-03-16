'use strict';

angular.module('adminuiApp').directive 'hide', () ->
	restrict: 'A'
	link: (scope, element, attrs) ->
		scope.$watch attrs.hide, (value) ->
			console.log value
			element.css "visibility", if !!value then "hidden" else ""
