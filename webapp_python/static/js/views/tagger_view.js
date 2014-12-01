define([
	'jquery',
	'underscore',
	'backbone',
	'text!views/tagger_view.html'
	], function($, _, Backbone, TaggerViewTemplate) {
		return Backbone.View.extend({
			template: _.template(TaggerViewTemplate),

			events: {
				"click .tagger-name-button": "changeTaggerName"
			},

			initialize: function(options) {
				if(!window.localStorage)
					window.localStorage = {};

				this.router = options.router;
				this.navigationView = options.navigationView;

				this.render();
			},

			render: function() {
				this.$el.html(this.template({tagger:localStorage.tagger}));
			},

			changeTaggerName:function(){
				localStorage.tagger = this.$el.find("#tagger-name-input").val();
				this.navigationView.updateTaggerName();
				Backbone.history.navigate("#", {replace: true});
				this.router.home();
			}

		});
});
