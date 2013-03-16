'use strict'

describe 'Directive: spinner', () ->
  beforeEach module 'adminuiApp'

  element = {}

  it 'should make hidden element visible', inject ($rootScope, $compile) ->
    element = angular.element '<spinner></spinner>'
    element = $compile(element) $rootScope
    expect(element text()).toBe 'this is the spinner directive'
