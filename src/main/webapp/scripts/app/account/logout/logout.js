'use strict';

angular.module('jhipsterApp')
  .config(function ($stateProvider) {
    $stateProvider
      .state('logout', {
        parent: 'account',
        url: '/logout',
        data: {
          authorities: []
        },
        views: {
          'content@': {
            templateUrl: 'scripts/app/main/main.html',
            controller: 'LogoutController'
          }
        }
      });
  });
