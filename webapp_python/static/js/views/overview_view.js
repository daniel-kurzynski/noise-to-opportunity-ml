define([
	'jquery',
	'underscore',
	'backbone',
	'text!views/overview_view.html',
	'text!views/tagged_posts.html',
	'text!views/predicted_posts.html'
	], function($, _, Backbone, OverviewViewTemplate, TaggedPostsTemplate, PredictedPostsTemplate) {

		return Backbone.View.extend({

			template: _.template(OverviewViewTemplate),
			taggedPostsTemplate: _.template(TaggedPostsTemplate),
			predictedPostsTemplate: _.template(PredictedPostsTemplate),

			events: {

			},

			initialize: function(options) {
				this.render();
				
			},

			render: function() {
				this.$el.html(this.template({}));
				this.loadTaggedPosts();
				this.loadCertainPosts();
				this.loadUncertainPosts();
			},

			loadTaggedPosts:function(){
				var self = this;
				$.get('/all_tagged_posts',function(result){
					posts = JSON.parse(result).posts;
					self.$el.find('#tagged-posts').html(self.taggedPostsTemplate({posts:posts}));
				});
			},

			loadCertainPosts:function(){
				var self = this;
				$.get('/certain_posts',function(result){
					posts = JSON.parse(result).posts;
					self.$el.find('#certain-posts').html(self.predictedPostsTemplate({posts:posts}));
				});
			},

			loadUncertainPosts:function(){
				var self = this;
				$.get('/uncertain_posts',function(result){
					posts = JSON.parse(result).posts;
					self.$el.find('#uncertain-posts').html(self.predictedPostsTemplate({posts:posts}));
				});
			}

	});
});
