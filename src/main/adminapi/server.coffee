_ = require "underscore"
express = require "express"
http = require "http"
socketio = require "socket.io"
request = require "request"
async = require "async"
redis = require "redis"

# Config
endpoint = "http://localhost:1337"
metricsEndpoint = "http://localhost:1338/metrics"
metricsKeys =
	nntpBandwidth: "au.com.samcday.bincrawl.misc.NNTPBandwidthMonitor"

app = express()
server = http.createServer app
io = socketio.listen server
redisSub = redis.createClient()

redisSub.subscribe "groupupdates", "groupactivity"
redisSub.on "message", (channel, message) ->
	switch channel
		when "groupupdates" then updateGroup message
		when "groupactivity" then updateGroupActivity message

server.listen 1339

groups = {}
groupActivity = {}

updateBandwidth = ->
	async.parallel {
		bw: (cb) ->
			request.get
				url: metricsEndpoint
				json: true
			, (err, resp, body) ->
				return (cb err or "Error") if err or not body or not body[metricsKeys.nntpBandwidth]
				cb null, body[metricsKeys.nntpBandwidth]
		conns: (cb) ->
			request.get
				url: endpoint + "/_nntp/current_connections"
			, (err, resp, body) ->
				return (cb err or "Error") if err or not body
				connections = parseInt body
				cb null, connections
	}, (err, results) ->
		unless err
			io.sockets.emit "bandwidth", read: results.bw.read.m1, write: results.bw.written.m1, connections: results.conns
		setTimeout updateBandwidth, 1000

updateAllGroups = ->
	request.get
		url: "#{endpoint}/group"
		json: true
	, (err, resp, body) ->
		unless err or not body or not body.length
			groups[group.name] = group for group in body
			allGroups = {}
			allGroups[name] = (_.extend groups[name], groupActivity[name]) for name, group of groups
			(io.sockets.in "groups").emit "groupUpdate", allGroups
		setTimeout updateAllGroups, 10000

updateGroup = (group) ->
	request.get
		url: "#{endpoint}/group/#{group}"
		json: true
	, (err, resp, body) ->
		unless err or not body or not body.name
			name = body.name
			groups[name] = body
			groupUpdate = {}
			groupUpdate[name] = _.extend groups[name], groupActivity[name]
			(io.sockets.in "groups").emit "groupUpdate", groupUpdate

updateGroupActivity = (msg) ->
	[group, flag] = msg.split "\:"
	groupActivity[group] ?= updating: false, backfilling: false
	switch flag
		when "u", "!u"
			groupActivity[group].updating = flag[0] isnt "!"
		when "b", "!b"
			groupActivity[group].backfilling = flag[0] isnt "!"

	(io.sockets.in "groups").emit "groupActivity", _.pick groupActivity, group

io.on "connection", (socket) ->
	socket.on "interested", (inTopic) ->
		switch inTopic
			when "groups"
				socket.emit "groupUpdate", groups
				socket.emit "groupActivity", groupActivity
				socket.join "groups"

updateAllGroups()
updateBandwidth()