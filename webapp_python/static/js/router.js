define(['jquery',
	'underscore',
	'backbone',
	'views/demand_classification_view',
	'views/overview_view',
	'views/tagger_view'	,
	'views/analyze_post_view'	
	], function($, _, Backbone, DemandClassificationView, DemandView, TaggerView, AnalyzePostView) {


	return Backbone.Router.extend({

		routes: {
			"":    "home",
			"posts(/)": "classifyPost",
			"posts/:id(/)": "classifyPost",
			"uncertain_posts(/)": "classifyUncertainPost",
			"uncertain_posts/:id(/)": "classifyUncertainPost",
			"certain_posts(/)": "classifyCertainPost",
			"certain_posts/:id(/)": "classifyCertainPost",
			"tagged_by_others_posts(/)": "classifyTaggedPost",
			"tagged_by_others_posts/:id(/)": "classifyTaggedPost",
			"conflicted_posts(/)": "classifyConflictedPost",
			"conflicted_posts/:id(/)": "classifyConflictedPost",
			"demand": "displayDemandView",
			"tagger": "displayTaggerView",
			"analyze_post(/)": "displayAnalyzePostView"
		},

		initialize: function(options){
			options = options || {};
			if(!options.$el)
				console.error("Router constructor called without $el");
			this.$el = options.$el;
			this.navigationView = options.navigationView;
		},

		////////////// Routes //////////////////

		home: function() {
			console.log("Routing home…");
			this.classifyPost();
		},

		isTaggerNameSet:function(){
			return(localStorage.tagger && localStorage.tagger!="")
		},

		classifyPost: function(postId) {
			this.displayDemandClassificationView(postId, "posts");
		},
		classifyUncertainPost: function(postId) {
			this.displayDemandClassificationView(postId, "uncertain_posts");
		},
		classifyCertainPost: function(postId) {
			this.displayDemandClassificationView(postId, "certain_posts");
		},
		classifyTaggedPost: function(postId) {
			this.displayDemandClassificationView(postId, "tagged_by_others_posts");
		},
		classifyConflictedPost: function(postId) {
			this.displayDemandClassificationView(postId, "conflicted_posts");
		},

		displayDemandClassificationView: function(postId, route) {
			if(this.isTaggerNameSet()){
				var demandClassificationView = new DemandClassificationView({ postId: postId, route: route });
				this.changeContentView(demandClassificationView);
			}
			else{
				this.displayTaggerView();
			}
		},

		displayTaggerView: function() {
			Backbone.history.navigate("#tagger", {replace: true});
			var taggerView  = new TaggerView({
				router:this,
				navigationView: this.navigationView
			});
			this.changeContentView(taggerView);
		},

		displayDemandView: function(){
			var demandView = new DemandView();
			this.changeContentView(demandView);
		},

		displayAnalyzePostView: function(){
			var analyzePostView = new AnalyzePostView();
			this.changeContentView(analyzePostView);
		},

		changeContentView: function(view){
			if(this.currentView)
				this.currentView.undelegateEvents()
			this.currentView = view;
			this.$el.children().detach();
			this.$el.append(view.$el);
		},

		
	});///////////////////////////////////////////////////////

});
