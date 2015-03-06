define([
	'jquery',
	'underscore',
	'backbone',
	'text!views/overview_view.html',
	'text!views/tagged_posts.html',
	'text!views/predicted_posts.html',
	'text!views/statistics.html'
	], function($, _, Backbone, OverviewViewTemplate, TaggedPostsTemplate, PredictedPostsTemplate, StatisticsTemplate) {

		return Backbone.View.extend({

			template: _.template(OverviewViewTemplate),
			taggedPostsTemplate: _.template(TaggedPostsTemplate),
			predictedPostsTemplate: _.template(PredictedPostsTemplate),
			statisticsTemplate: _.template(StatisticsTemplate),

			events: {

			},

			initialize: function(options) {
				this.render();
				this.statistics = {};
				this.showStatistics();
				
			},

			render: function() {
				this.$el.html(this.template({}));
				this.loadTaggedPosts();
				this.loadUncertainPosts();
				this.loadCertainPosts();
			},

			loadTaggedPosts:function(){
				var self = this;
				$.get('/all_tagged_posts',function(result){
					posts = JSON.parse(result).posts;
					self.$el.find('#tagged-posts').html(self.taggedPostsTemplate({posts:posts}));
					self.updateTaggedPostsStatistic(posts);
				});
			},

			loadUncertainPosts:function(){
				var self = this;
				$.get('/uncertain_posts',function(result){
					posts = JSON.parse(result).posts;
					self.$el.find('#uncertain-posts').html(self.predictedPostsTemplate({posts:posts}));
				});
			},

			loadCertainPosts:function(){
				var self = this;
				$.get('/certain_posts',function(result){
					posts = JSON.parse(result).posts;
					self.$el.find('#certain-posts').html(self.predictedPostsTemplate({posts:posts}));
				});
			},

			updateTaggedPostsStatistic:function(taggedPosts){
				this.statistics.numberOfTaggedPosts = numberOfPosts = taggedPosts.length;
				this.statistics.percentageOfTaggedDemand = _.where(taggedPosts, {demand:"demand"}).length/numberOfPosts;
				this.statistics.percentageOfTaggedNoDemand = _.where(taggedPosts, {demand:"no-demand"}).length/numberOfPosts;
				this.statistics.percentageOfTaggedLVM = _.where(taggedPosts, {category:"LVM"}).length/numberOfPosts;
				this.statistics.percentageOfTaggedCRM = _.where(taggedPosts, {category:"CRM"}).length/numberOfPosts;
				this.statistics.percentageOfTaggedHCM = _.where(taggedPosts, {category:"HCM"}).length/numberOfPosts;
				this.statistics.percentageOfTaggedECOM = _.where(taggedPosts, {category:"ECOM"}).length/numberOfPosts;
				this.statistics.percentageOfTaggedNone = _.where(taggedPosts, {category:"None"}).length/numberOfPosts;
				this.showStatistics();
			},

			showStatistics:function(){
				this.$el.find("#statistics").html(this.statisticsTemplate({statistics:this.statistics}));
			}

	});
});
