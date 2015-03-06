define([
	'jquery',
	'underscore',
	'backbone',
	'text!views/analyze_post_view.html',
	'text!views/analyze_post_result.html'
	], function($, _, Backbone, AnalyzePostViewTemplate, AnalyzePostResultTemplate) {

		return Backbone.View.extend({

			template: _.template(AnalyzePostViewTemplate),
			resultTemplate: _.template(AnalyzePostResultTemplate),

			events: {
				"change #post-text": "postChanged",
				"keyup #post-text": "postChanged",
				"paste #post-text": "postChanged"
			},

			initialize: function(options) {
				this.render();
				_.bindAll(this, "postChanged")
				
			},

			render: function() {
				this.$el.html(this.template({}));
			},

			postChanged: function(event){
				var self = this;
				var post = this.$el.find("#post-text").val();
				if(!post|| post==""){
					this.$el.find("#results").html("");
					return;
				}
				$.get("/analyze_post",{post:post}, function(data) {
					results = JSON.parse(data).results;
					self.$el.find("#results").html(self.resultTemplate({results:results}));
				});
			}

	});
});
