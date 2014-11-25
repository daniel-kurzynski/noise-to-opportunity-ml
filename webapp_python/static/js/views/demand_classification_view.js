define([
	'jquery',
	'underscore',
	'backbone',
	'text!views/demand_classification_view.html'
	], function($, _, Backbone, DemandClassificationViewTemplate) {

		var Post = Backbone.Model.extend({
			defaults: {
				id: -1
			}
		});
		var Posts = Backbone.Collection.extend({
			model: Post
		});

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
				this.route = options.route;
				this.posts = new Posts;

				_.bindAll(this, "render");

				this.currentPost = new Post;
				this.posts.on("add remove", this.render);

				$(document).bind('keypress', _.bind(this.keyAction, this));

				if (options.postId)
					this.loadPost(options.postId);
				else {
					this.loadPostsFromRoute();
				}
			},

			render: function() {
				if (this.posts.size === 0)
					return;
				var firstPost = this.posts.at(0);
				if (this.posts.at(0).id === this.currentPost.id)
					return;

				this.currentPost = firstPost;
				this.$el.html(this.template({ post: firstPost.attributes }));
				Backbone.history.navigate(this.route + "/" + firstPost.id, {replace: true});
				console.log("Rendering next post: " + firstPost.id + ".");

				if (this.posts.size() <= 5)
					self.loadPostsFromRoute();
			},

			keyAction: function(event) {
				var code = (event.keyCode || event.which);
				this.$el.find(this.keyMapping[code]).click();
			},

			showCategoryButtons: function() {
				this.$el.find(".demand-decision").hide();
				this.$el.find(".category-decision").show();
			},

			tagPost: function(event) {
				var data = $(event.target).data();
				$.post('classify_post/' + this.currentPost.id, data);
			},

			showNewPost: function() {
				// remove first post from collection, automatically triggers rerendering
				this.posts.shift();
			},

			loadPost: function(postId) {
				var self = this;
				$.get("/post/" + postId, function(data) {
					data = JSON.parse(data);
					self.posts = self.posts.add(data.posts);
				});
			},

			loadPostsFromRoute: function() {
				var self = this;
				$.get("/" + this.route, function(data) {
					console.log("Loading more posts.");
					data = JSON.parse(data);
					if (data.posts.length === 0) {
						console.log("No more posts.");
						return;
					}
					self.posts.add(data.posts);
				})
			}


	});
});
