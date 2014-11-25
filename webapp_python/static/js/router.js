define(['jquery',
	'underscore',
	'backbone',
	'views/demand_classification_view',
	'views/demand_view'
	
	], function($, _, Backbone, DemandClassificationView, DemandView) {


	return Backbone.Router.extend({

		routes: {
			"":    							"home",
			"classify_post/:id": 			"classifyPost",
			"demand": 						"showDemandView"
		},

		initialize: function(options){
			options = options || {};
			if(!options.$el)
				console.error("Router constructor called without $el");
			$el = options.$el;
		},

		////////////// Routes //////////////////

		home: function(){
			console.log("Routing home…");
			this.classifyPost();
			
		},

		classifyPost: function(postId)	{
			demandClassificationView = new DemandClassificationView({postId:postId});
			this.changeContentView(demandClassificationView);
		},

		showDemandView:function(){
			demandView = new DemandView();
			this.changeContentView(demandView);
		},

		changeContentView:function(view){
			$el.children().detach();
			$el.append(view.$el);
		}

		
	});///////////////////////////////////////////////////////

});
