'use strict'

describe 'Controller: GroupsCtrl', () ->

  # load the controller's module
  beforeEach module 'adminuiApp'

  GroupsCtrl = {}
  scope = {}

  # Initialize the controller and a mock scope
  beforeEach inject ($controller) ->
    scope = {}
    GroupsCtrl = $controller 'GroupsCtrl', {
      $scope: scope
    }

  it 'should attach a list of awesomeThings to the scope', () ->
    expect(scope.awesomeThings.length).toBe 3;
