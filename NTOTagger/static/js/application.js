require.config({
	paths: {
		jquery:                   './../libs/jquery/dist/jquery.min',
		underscore:               './../libs/underscore/underscore-min',
		backbone:                 './../libs/backbone/backbone',
		text: 					  './../libs/requirejs-text/text',
		bootstrap: 				  './../libs/bootstrap/dist/js/bootstrap.min',  
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
