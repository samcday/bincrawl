'use strict'

describe 'Controller: BandwidthCtrl', () ->

  # load the controller's module
  beforeEach module 'adminuiApp'

  BandwidthCtrl = {}
  scope = {}

  # Initialize the controller and a mock scope
  beforeEach inject ($controller) ->
    scope = {}
    BandwidthCtrl = $controller 'BandwidthCtrl', {
      $scope: scope
    }

  it 'should attach a list of awesomeThings to the scope', () ->
    expect(scope.awesomeThings.length).toBe 3;
