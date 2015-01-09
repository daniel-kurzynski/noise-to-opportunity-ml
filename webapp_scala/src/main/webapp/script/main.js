jQuery(function() {
	var predictionTimer = function() {
		var COLOR_ANIMATION = 300;

		var roundValue = function(d) {
			return Math.round(d * 100);
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
				window.setTimeout(predictionTimer, 2500)
			},
			success: function (data) {
				var $results = $("#results");
				$results.empty();
				console.log(data);
				if (data.demand.prob > 0.5) {
					setTextAndAnimate("demand", "DEMAND", "#43ac6a");
				} else {
					setTextAndAnimate("demand", "NO-DEMAND", "#f04124");
				}
				if (data.product[0].prob > 0.5) {
					setTextAndAnimate("product", data.product[0].cls, "#008cba");
				} else {
					setTextAndAnimate("product", "NONE", "#5bc0de");
				}
				data.product.forEach(function (el) {
					// Use templates, if this gets more complicated.
					$results.append("<tr><td class='product-category-label'><div class='result-percentage label-primary'>" +
						roundValue(el.prob) + " %</div></td><td>" + el.cls + "</td></tr>")
				});
			},
			error: function() {
				$("#demand-classification").css("visibility", "hidden");
				$("#product-classification").css("visibility", "hidden");
			}
		});
	};
	predictionTimer();
});
