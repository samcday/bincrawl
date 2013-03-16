'use strict'

describe 'Controller: AddgroupCtrl', () ->

  # load the controller's module
  beforeEach module 'adminuiApp'

  AddgroupCtrl = {}
  scope = {}

  # Initialize the controller and a mock scope
  beforeEach inject ($controller) ->
    scope = {}
    AddgroupCtrl = $controller 'AddgroupCtrl', {
      $scope: scope
    }

  it 'should attach a list of awesomeThings to the scope', () ->
    expect(scope.awesomeThings.length).toBe 3;
