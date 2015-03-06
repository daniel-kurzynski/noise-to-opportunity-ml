define([
	'jquery',
	'underscore',
	'backbone',
	'text!views/tagger_view.html'
	], function($, _, Backbone, TaggerViewTemplate) {
		return Backbone.View.extend({
			template: _.template(TaggerViewTemplate),

			events: {
				"click .tagger-name-button": "changeTaggerName",
				"keydown #tagger-name-input": "keyAction"
			},

			initialize: function(options) {
				_.bindAll(this, "keyAction");

				if(!window.localStorage)
					window.localStorage = {};

				this.router = options.router;
				this.navigationView = options.navigationView;

				this.render();
			},

			render: function() {
				this.$el.html(this.template({tagger:localStorage.tagger}));
			},

			keyAction:function(){
				var code = (event.keyCode || event.which);
				if(code==13){
					this.changeTaggerName();
				}
			},

			changeTaggerName:function(){
				localStorage.tagger = this.$el.find("#tagger-name-input").val();
				this.navigationView.updateTaggerName();
				Backbone.history.navigate("#", {replace: true});
				this.router.home();
			}

		});
});
