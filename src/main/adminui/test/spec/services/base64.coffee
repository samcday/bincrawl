'use strict'

describe 'Service: base64', () ->

  # load the service's module
  beforeEach module 'adminuiApp'

  # instantiate service
  base64 = {}
  beforeEach inject (_base64_) ->
    base64 = _base64_

  it 'should do something', () ->
    expect(!!base64).toBe true;
