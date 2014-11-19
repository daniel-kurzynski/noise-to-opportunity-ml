define([
	'jquery',
	'underscore',
	'backbone',
	'text!views/demand_classification_view.html',
	], function($, _, Backbone, DemandClassificationViewTemplate) {

		return Backbone.View.extend({

			template: _.template(DemandClassificationViewTemplate),

			events: {
				"click #btn-has-no-demand":			"markPostAsNoDemand",
				"click #btn-has-demand":			"markPostAsDemand",
				'keydown': 'keyAction'
			},

			initialize: function(options){
				this.render();
				this.unclassifiedPosts = [];

				$(document).bind('keypress', _.bind(this.keyAction,this));

				if(options.postId){
					this.showPost(options.postId)
				}
				else{
					this.showNewPost();
				}
			},

			render: function(){
			},

			keyAction: function(event){
				switch(event.keyCode || event.which) {
				    case 121: //y
				        this.markPostAsDemand();
				        break;
				    case 110: //n
				        this.markPostAsNoDemand();
				        break;
				}
			},

			markPostAsDemand:function(){
				$.post('post/'+this.currentPost.id+"/demand",{demand:true});
				this.showNewPost();
			},
			
			markPostAsNoDemand:function(){
				$.post('post/'+this.currentPost.id+"/demand",{demand:false});
				this.showNewPost();
			},			

			showNewPost:function(){
				var self = this;
				if(this.unclassifiedPosts.length<=5){
					self.loadUncertaintyPosts(_.bind(self.showNewPost,self));
				}
				else{
					self.currentPost = this.unclassifiedPosts.shift();
					self.$el.html(self.template({post:self.currentPost}));
					Backbone.history.navigate("classify_post/" + self.currentPost.id, {replace: true});
				}
			},

			showPost:function(postId){
				var self = this;
				$.get("/post/"+postId, function( data ) {
					data = JSON.parse(data);
					self.unclassifiedPosts = self.unclassifiedPosts.concat(data.posts);
					self.showNewPost();
				});
			},

			loadUncertaintyPosts:function(callback){
				var self = this;
				$.get("/uncertainty_posts", function( data ) {
					data = JSON.parse(data);
					self.unclassifiedPosts = self.unclassifiedPosts.concat(data.posts);
					if(callback)
						callback();
				})
			},


	});
});
