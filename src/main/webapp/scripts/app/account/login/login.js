'use strict';

angular.module('jhipsterApp')
  .config(function ($stateProvider) {
    $stateProvider
      .state('login', {
        parent: 'account',
        url: '/login',
        data: {
          authorities: [],
          pageTitle: 'Authentication'
        },
        views: {
          'content@': {
            templateUrl: 'scripts/app/account/login/login.html',
            controller: 'LoginController'
          }
        },
        resolve: {}
      });
  });
