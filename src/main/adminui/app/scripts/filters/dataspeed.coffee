'use strict';

angular.module('adminuiApp').filter 'dataspeed', () ->
	(val) ->
		suffix = "b/s"
		if val >= 1000
			val /= 1000
			suffix = "KB/s"
		if val >= 900
			val /= 1000
			suffix = "MB/s"
		if val >= 900
			val /= 1000
			suffix = "GB/s"
		places = Math.pow 10, (if suffix is "b/s" then 0 else 2)
		val = (Math.round val * places) / places
		return "#{val} #{suffix}"