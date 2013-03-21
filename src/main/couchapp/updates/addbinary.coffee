(doc, req) ->
  q = JSON.parse req.body
  binaryHash = q.binaryHash
  return [doc, "exists"] if doc?.binaries?.some (bin) -> bin.binaryHash is binaryHash
  newBinary = {}
  newBinary.binaryHash = binaryHash
  newBinary.group = q.group
  newBinary.subject = q.subject
  newBinary.date = new Date parseInt q.date

  newBinary.binarySegments = (q.parts.sort (a, b) -> a.partNum - b.partNum)
  delete part.partNum for part in newBinary.binarySegments

  doc.date = newBinary.date if newBinary.date and (not doc.date or (new Date(doc.date) < newBinary.date))

  doc.binaries ?= []
  doc.binaries.push newBinary
  return [doc, "ok"]
