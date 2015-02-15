require.config({
	paths: {
		jquery:							'./jquery.min',
		underscore:					'./underscore-min',
		backbone:						'./backbone',
		text:								'./text',
		bootstrap:					'./bootstrap.min',
	}
});

requirejs.config({
	shim: {
		'backbone': {
			deps: ['underscore', 'jquery'],
			exports: 'Backbone'
		},
		'underscore': {
			exports: '_'
		},
		'bootstrap': {
			deps: ['jquery'],
			exports: 'Bootstrap'
		}
	}
});

define([
	'router',
	'views/navigation_view',
	'bootstrap'
	], function(Router, NavigationView){
		var navigationView = new NavigationView({$el: $(".navigation-view")});

		new Router({
			$el: $(".content"),
			navigationView:navigationView
		});

		Backbone.history.start();

});
