(doc, req) ->
  {classification, binary, group} = JSON.parse req.body

  if doc is null
    doc =
      _id: req.id
      name: classification.name
      count: 0
      crawledDate: Date.now()

  doc.count = Math.max(doc.count, classification.totalParts)

  binaryHash = binary.binaryHash
  return [null, "exists"] if doc?.binaries?.some (bin) -> bin.binaryHash is binaryHash

  newBinary =
    binaryHash: binaryHash
    group: binary.group
    subject: binary.subject
    date: new Date parseInt binary.date

  newBinary.binarySegments = (binary.parts.sort (a, b) -> a.partNum -  b.partNum)
  delete part.partNum for part in newBinary.binarySegments

  doc.date = newBinary.date if newBinary.date and (not doc.date or (new Date(doc.date) < newBinary.date))

  doc.binaries ?= []
  doc.binaries.push newBinary
  return [doc, "ok"]
