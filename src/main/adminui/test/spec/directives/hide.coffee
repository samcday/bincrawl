'use strict'

describe 'Directive: hide', () ->
  beforeEach module 'adminuiApp'

  element = {}

  it 'should make hidden element visible', inject ($rootScope, $compile) ->
    element = angular.element '<hide></hide>'
    element = $compile(element) $rootScope
    expect(element text()).toBe 'this is the hide directive'
