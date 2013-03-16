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
	request.get
		url: metricsEndpoint
		json: true
	, (err, resp, body) ->
		unless err or not body or not body[metricsKeys.nntpBandwidth]
			stats = body[metricsKeys.nntpBandwidth]
			io.sockets.emit "bandwidth",
				read: stats.read.m1, write: stats.written.m1
		setTimeout updateBandwidth, 1000

updateAllGroups = (group) ->
	request.get
		url: "#{endpoint}/group"
		json: true
	, (err, resp, body) ->
		unless err or not body or not body.length
			groups[group.name] = group for group in body

updateGroup = (group) ->
	request.get
		url: "#{endpoint}/group/#{group}"
		json: true
	, (err, resp, body) ->
		unless err or not body or not body.name
			groups[body.name] = body
			groupUpdate = {}
			groupUpdate[body.name] = groups[body.name]
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