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
				
			},

			render: function() {
				this.$el.html(this.template({}));
			},

			postChanged: function(event){
				if(!event.currentTarget.value || event.currentTarget.value==""){
					this.$el.find("#results").html("");
					return;
				}
				var results = [event.currentTarget.value]
				this.$el.find("#results").html(this.resultTemplate({results:results}));
			}

	});
});
