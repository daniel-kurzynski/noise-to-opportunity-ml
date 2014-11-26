define([
	'jquery',
	'underscore',
	'backbone',
	'text!views/overview_view.html',
	'text!views/tagged_posts.html',
	'text!views/predicted_posts.html'
	], function($, _, Backbone, OverviewViewTemplate, TaggedPostsTemplate) {

		return Backbone.View.extend({

			template: _.template(OverviewViewTemplate),
			taggedPostsTemplate: _.template(TaggedPostsTemplate),

			events: {

			},

			initialize: function(options) {
				this.render();
				
			},

			render: function() {
				this.$el.html(this.template({}));
				this.loadTaggedPosts();
			},

			loadTaggedPosts:function(){
				var self = this;
				$.get('/all_tagged_posts',function(result){
					posts = JSON.parse(result).posts;
					self.$el.find('#tagged-posts').html(self.taggedPostsTemplate({posts:posts}));
				});
			}

	});
});
