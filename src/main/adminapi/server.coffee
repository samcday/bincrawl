express = require "express"
http = require "http"
socketio = require "socket.io"
request = require "request"
async = require "async"

# Config
endpoint = "http://localhost:1337"
metricsEndpoint = "http://localhost:1338/metrics"

app = express()
server = http.createServer app
io = socketio.listen server

server.listen 1339

updateBandwidth = ->
	request.get
		url: metricsEndpoint
		json: true
	, (err, resp, body) ->
		stats = body["au.com.samcday.bincrawl.misc.NNTPBandwidthMonitor"]
		io.sockets.emit "bandwidth",
			read: stats.read.m1, write: stats.written.m1
	setTimeout updateBandwidth, 1000

updateBandwidth()