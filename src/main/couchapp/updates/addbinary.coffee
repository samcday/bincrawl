(doc, req) ->
  q = req.query
  doc.binaryHash = q.hash
  doc.group = q.group
  doc.subject = q.name
  doc.date = new Date parseInt q.date
  doc.binarySegments = (size: q["part#{i}_size"], messageId: q["part#{i}_id"] for i in [1..q.num])