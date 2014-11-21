define(['jquery',
	'underscore',
	'backbone',
	'views/demand_classification_view'
	
	], function($, _, Backbone, DemandClassificationView) {


	return Backbone.Router.extend({

		routes: {
			"":    "home",
			"classify_post/:id":             "classifyPost"
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

		changeContentView:function(view){
		$el.children().detach();
		$el.append(view.$el);
	}

		
	});///////////////////////////////////////////////////////

});
