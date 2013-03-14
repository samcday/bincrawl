'use strict'

describe 'Filter: dataspeed', () ->

  # load the filter's module
  beforeEach module 'adminuiApp'

  # initialize a new instance of the filter before each test
  dataspeed = {}
  beforeEach inject ($filter) ->
    dataspeed = $filter 'dataspeed'

  it 'should return the input prefixed with "dataspeed filter:"', () ->
    text = 'angularjs'
    expect(dataspeed text).toBe ('dataspeed filter: ' + text);
