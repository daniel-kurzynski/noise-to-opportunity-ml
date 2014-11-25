define([
	'jquery',
	'underscore',
	'backbone',
	'text!views/demand_view.html',
	'text!views/demand_labeled_posts.html'
	], function($, _, Backbone, DemandViewTemplate, DemandLabeledPostsTemplate) {

		return Backbone.View.extend({

			template: _.template(DemandViewTemplate),
			demandLabeledPostsTemplate: _.template(DemandLabeledPostsTemplate),

			events: {

			},

			initialize: function(options) {
				this.render();
				
			},

			render: function() {
				this.$el.html(this.template({}));
				this.loadDemandLabeledPosts();
			},

			loadDemandLabeledPosts:function(){
				var self = this;
				$.get('/damand_labeled_posts',function(result){
					posts = JSON.parse(result).posts;
					// demandPost = _.where(posts,{label:'demand'});
					// noDemandPost = _.where(posts,{label:'noDemand'});
					// noIdeaPosts = _.where(posts,{label:'noIdea'});
					self.$el.find('#demand-labeled-posts').html(self.demandLabeledPostsTemplate({posts:posts}));
				});
			}

	});
});
