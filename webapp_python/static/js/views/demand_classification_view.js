define([
	'jquery',
	'underscore',
	'backbone',
	'text!views/demand_classification_view.html',
	], function($, _, Backbone, DemandClassificationViewTemplate) {

		return Backbone.View.extend({

			template: _.template(DemandClassificationViewTemplate),

			
			keyMapping: {
				121: "#btn-has-demand",
				110: "#btn-has-no-demand",
				105: "#btn-has-no-idea",
				49:  "#btn-category-1",
				50:  "#btn-category-2",
				51:  "#btn-category-3",
				52:  "#btn-category-4",
				53:  "#btn-category-5"
			},
			

			events: {
				"click .btn-tag":         			"tagPost",
				"click #btn-has-no-idea":			"showCategoryButtons",
				"click #btn-has-no-demand":			"showCategoryButtons",
				"click #btn-has-demand":			"showCategoryButtons",
				"click .btn-category":              "showNewPost",
				"keydown":                          "keyAction"
			},

			initialize: function(options) {
				this.render();
				this.unclassifiedPosts = [];

				$(document).bind('keypress', _.bind(this.keyAction,this));

				if (options.postId){
					this.showPost(options.postId)
				}
				else{
					this.showNewPost();
				}
			},

			render: function() {
			},

			keyAction: function(event) {
				code = (event.keyCode || event.which);
				this.$el.find(this.keyMapping[code]).click();
			},

			showCategoryButtons: function() {
				this.$el.find(".demand-decission").hide();
				this.$el.find(".category-decission").show();
			},

			tagPost: function(event) {
				data = $(event.target).data();
				$.post('classify_post/' + this.currentPost.id, data);
			},		

			showNewPost: function() {
				var self = this;
				if(this.unclassifiedPosts.length<=3){
					self.loadUncertaintyPosts(_.bind(self.showNewPost,self));
				}
				else{
					self.currentPost = this.unclassifiedPosts.shift();
					self.$el.html(self.template({post:self.currentPost}));
					Backbone.history.navigate("classify_post/" + self.currentPost.id, {replace: true});
				}
			},

			showPost: function(postId) {
				var self = this;
				$.get("/post/"+postId, function( data ) {
					data = JSON.parse(data);
					self.unclassifiedPosts = self.unclassifiedPosts.concat(data.posts);
					self.showNewPost();
				});
			},

			loadUncertaintyPosts: function(callback) {
				var self = this;
				$.get("/uncertainty_posts", function( data ) {
					data = JSON.parse(data);
					self.unclassifiedPosts = data.posts;
					if(callback)
						callback();
				})
			},


	});
});
