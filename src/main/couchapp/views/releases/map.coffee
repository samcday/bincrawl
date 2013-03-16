(doc) ->
	groups = {}
	groups[binary.group] = null for binary in doc.binaries
	emit group, 1 for group in Object.keys groups
