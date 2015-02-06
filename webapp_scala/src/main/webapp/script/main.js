jQuery(function() {
	var doPrediction = function() {
		var COLOR_ANIMATION = 300;

		var roundValue = function(d, places) {
			if (!places) places = 0;
			var power = Math.pow(10, places);
			return Math.round(d * 100 * power) / power;
		};
		var setTextAndAnimate = function(type, text, color) {
			$("#" + type + "-classification").css("visibility", "visible").text(text).animate({
				backgroundColor: color
			}, COLOR_ANIMATION);
		};

		var blogPost = $("#blog-post").val();
		jQuery.ajax({
			dataType: "json",
			url: "/predictions",
			data: {
				text: blogPost
			},
			complete: function() {
				window.setTimeout(doPrediction, 2500)
			},
			success: function (data) {
				console.log(data);
				var $productResults = $("#product-results");
				var $demandResults  = $("#demand-results");
				$productResults.empty();
				$demandResults.empty();
				console.log(new Date(), data.demand.classificationOutput);
				var demandProb = data.demand.classificationOutput.prob;
				if (demandProb > 0.5) {
					setTextAndAnimate("demand", "DEMAND (" + roundValue(demandProb) + ")", "#43ac6a");
				} else {
					setTextAndAnimate("demand", "NO-DEMAND (" + roundValue(1 - demandProb) + ")", "#f04124");
				}
				if (data.product[0].prob >= 0.45) {
					setTextAndAnimate("product", data.product[0].product, "#008cba");
				} else {
					setTextAndAnimate("product", "NONE", "#5bc0de");
				}
				data.demand.classificationOutput.relevantFeatures.forEach(function(el) {
					$demandResults.append("<tr><td>" + el[0] + "</td></tr>");
				});
				data.product.forEach(function (el) {
					// Use templates, if this gets more complicated.
					$productResults.append("<tr><td class='product-category-label'><div class='result-percentage label-primary'>" +
						roundValue(el.prob, 6) + " %</div></td><td>" + el.product + "</td></tr>")
				});
			},
			error: function() {
				$("#demand-classification").css("visibility", "hidden");
				$("#product-classification").css("visibility", "hidden");
			}
		});
	};
	doPrediction();
	$(document).keydown(function(event){
		if(event.keyCode === 18){
			console.log("Trigger prediction!")
			doPrediction();
		}
	});
});


