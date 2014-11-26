define(['jquery',
	'underscore',
	'backbone',
	'views/demand_classification_view',
	'views/demand_view'
	
	], function($, _, Backbone, DemandClassificationView, DemandView) {


	return Backbone.Router.extend({

		routes: {
			"":    "home",
			"uncertain_posts(/)": "classifyPost",
			"uncertain_posts/:id(/)": "classifyPost",
			"tagged_posts(/)": "classifyTaggedPost",
			"tagged_posts/:id(/)": "classifyTaggedPost",
			"conflicted_posts(/)": "classifyConflictedPost",
			"conflicted_posts/:id(/)": "classifyConflictedPost",
			"demand": "displayDemandView"
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
			this.displayDemandClassificationView(postId, "uncertain_posts");
		},
		classifyTaggedPost: function(postId) {
			this.displayDemandClassificationView(postId, "tagged_posts");
		},
		classifyConflictedPost: function(postId) {
			this.displayDemandClassificationView(postId, "conflicted_posts");
		},

		displayDemandClassificationView: function(postId, route) {
			var demandClassificationView = new DemandClassificationView({ postId: postId, route: route });
			this.changeContentView(demandClassificationView);
		},

		displayDemandView: function(){
			var demandView = new DemandView();
			this.changeContentView(demandView);
		},

		changeContentView: function(view){
			this.$el.children().detach();
			this.$el.append(view.$el);
		}

		
	});///////////////////////////////////////////////////////

});
