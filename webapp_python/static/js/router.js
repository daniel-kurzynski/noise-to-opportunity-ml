define(['jquery',
	'underscore',
	'backbone',
	'views/demand_classification_view'
	
	], function($, _, Backbone, DemandClassificationView) {


	return Backbone.Router.extend({

		routes: {
			"":    "home",
			"classify_post/:id": "classifyPost",
			"classify_tagged_post/:id": "classifyTaggedPost"
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
			var demandClassificationView = new DemandClassificationView( { postId: postId, tagged_only: false });
			this.changeContentView(demandClassificationView);
		},
		classifyTaggedPost: function(postId)	{
			var demandClassificationView = new DemandClassificationView( { postId: postId, tagged_only: true });
			this.changeContentView(demandClassificationView);
		},

		changeContentView:function(view){
			$el.children().detach();
			$el.append(view.$el);
		}

		
	});///////////////////////////////////////////////////////

});
