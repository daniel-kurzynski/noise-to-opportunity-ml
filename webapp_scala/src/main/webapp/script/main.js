jQuery(function() {
	var predictionTimer = function() {
		var COLOR_ANIMATION = 300;

		var roundValue = function(d) {
			return Math.round(d * 100);
		};

		jQuery.getJSON("/predictions", { text: $("#blog-post").val() }, function(data) {
			var $results = $("#results");
			$results.empty();
			console.log(data);
			if (data.demand.prob > 0.5) {
				$("#demand-classification").text("DEMAND").animate({
					backgroundColor: "#43ac6a"
				}, COLOR_ANIMATION);
			} else {
				$("#demand-classification").text("NO-DEMAND").animate({
					backgroundColor: "#f04124"
				}, COLOR_ANIMATION);
			}
			if (data.product[0].prob > 0.5) {
				$("#product-classification").text(data.product[0].cls).animate({
					backgroundColor: "#008cba"
				}, COLOR_ANIMATION);
			} else {
				$("#product-classification").text("NONE").animate({
					backgroundColor: "#5bc0de"
				}, COLOR_ANIMATION);

			}
			data.product.forEach(function(el) {
				// Use templates, if this gets more complicated.
				$results.append("<tr><td class='product-category-label'><div class='result-percentage label-primary'>" +
					roundValue(el.prob) + " %</div></td><td>" + el.cls + "</td></tr>")
			});
			window.setTimeout(predictionTimer, 2500)
		});
	};
	predictionTimer();
});
