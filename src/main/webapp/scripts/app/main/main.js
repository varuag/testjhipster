'use strict';

angular.module('jhipsterApp')
  .config(function ($stateProvider) {
    $stateProvider
      .state('home', {
        parent: 'site',
        url: '/',
        data: {
          authorities: []
        },
        views: {
          'content@': {
            templateUrl: 'scripts/app/main/main.html',
            controller: 'MainController'
          }
        },
        resolve: {}
      });
  });
