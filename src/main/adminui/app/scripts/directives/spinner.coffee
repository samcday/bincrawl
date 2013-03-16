'use strict';

angular.module('adminuiApp').directive('spinner', () ->
	template: '<div class="spinnerContainer"></div>'
	restrict: 'E'
	replace: true
	link: (scope, element, attrs) ->
		radius = (parseInt attrs.radius) || 7
		tickWidth = (parseInt attrs.width) || 4

		dimension = radius*2 + tickWidth*2

		spinner = new Spinner {
			lines: 17
			length: 0
			width: tickWidth
			radius: radius
			corners: 0
			rotate: 90
			color: '#000'
			speed: 1
			trail: 100
			shadow: false
			hwaccel: true
			className: 'spinner'
			zIndex: 2e9
			left: "50%"
			top: "50%"
		}
		spinner.spin()
		spinnerEl = angular.element spinner.el
		spinnerEl.css left: "50%", top: "50%"

		element.append spinner.el
		element.css
			position: "relative"
			display: "inline-block"
			width: "#{dimension}px"
			height: "#{dimension}px"

		scope.$on "$destroy", -> spinner.stop()
	)
