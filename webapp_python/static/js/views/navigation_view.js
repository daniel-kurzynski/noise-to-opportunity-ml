define([
	'jquery',
	'underscore',
	'backbone',
	'text!views/navigation_view.html'
	], function($, _, Backbone, NavigationViewTemplate) {
		return Backbone.View.extend({
			template: _.template(NavigationViewTemplate),

			initialize: function(options) {
				this.$el = options.$el;
				this.render();
			},

			render: function() {
				this.$el.html(this.template({tagger:localStorage.tagger}));
			},

			updateTaggerName: function(){
				this.$el.find("#tagger-name").text(localStorage.tagger);
			},



		});
});
