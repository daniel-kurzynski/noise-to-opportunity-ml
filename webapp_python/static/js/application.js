require.config({
	paths: {
		jquery:                   './../libs/js/jquery',
		underscore:               './../libs/js/underscore',
		backbone:                 './../libs/js/backbone',
		text: 					  './../libs/js/text',
		bootstrap: 				  './../libs/js/bootstrap',  
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
		},
	}
});

define([
	'router',
	'bootstrap'
	], function(Router){
	new Router({$el: $(".content")});
	Backbone.history.start();

});
