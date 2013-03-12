(doc, req) ->
  q = JSON.parse req.body
  binaryHash = q.hash
  return [doc, "exists"] if doc?.binaries?.some (bin) -> bin.binaryHash is binaryHash
  newBinary = {}
  newBinary.binaryHash = binaryHash
  newBinary.group = q.group
  newBinary.subject = q.name
  newBinary.date = new Date parseInt q.date
  newBinary.binarySegments = ({size: q["part#{i}_size"], messageId: q["part#{i}_id"]} for i in [1..q.num])

  doc.date = newBinary.date if newBinary.date and (not doc.date or (new Date(doc.date) < newBinary.date))

  doc.binaries ?= []
  doc.binaries.push newBinary
  return [doc, "ok"]
