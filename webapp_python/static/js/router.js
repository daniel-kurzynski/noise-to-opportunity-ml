define(['jquery',
	'underscore',
	'backbone',
	'views/demand_classification_view'
	
	], function($, _, Backbone, DemandClassificationView) {


	return Backbone.Router.extend({

		routes: {
			"":    "home",
			"uncertain_posts(/)": "classifyPost",
			"uncertain_posts/:id(/)": "classifyPost",
			"tagged_posts(/)": "classifyTaggedPost",
			"tagged_posts/:id(/)": "classifyTaggedPost",
			"conflicted_posts(/)": "classifyConflictedPost",
			"conflicted_posts/:id(/)": "classifyConflictedPost"
		},

		initialize: function(options){
			options = options || {};
			if(!options.$el)
				console.error("Router constructor called without $el");
			this.$el = options.$el;
		},

		////////////// Routes //////////////////

		home: function() {
			console.log("Routing home…");
			this.classifyPost();
		},

		classifyPost: function(postId) {
			this.displayDemandView(postId, "uncertain_posts");
		},
		classifyTaggedPost: function(postId) {
			this.displayDemandView(postId, "tagged_posts");
		},
		classifyConflictedPost: function(postId) {
			this.displayDemandView(postId, "conflicted_posts");
		},

		displayDemandView: function(postId, route) {
			var demandClassificationView = new DemandClassificationView( { postId: postId, route: route });
			this.changeContentView(demandClassificationView);
		},

		changeContentView: function(view){
			this.$el.children().detach();
			this.$el.append(view.$el);
		}

		
	});///////////////////////////////////////////////////////

});
