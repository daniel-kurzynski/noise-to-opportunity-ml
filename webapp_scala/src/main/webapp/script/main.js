jQuery(function() {
	var predictionTimer = function() {
		jQuery.getJSON("/predictions", { text: "ABC" }, function(data) {
			var $results = $("#results")
			$results.empty();
			console.log(data);
			data.forEach(function(el) {
				// Use templates, if this gets more complicated.
				$results.append("<tr><td><div class='result-percentage label-primary'>" +
					el.percentage + " %</div></td><td>" + el.text + "</td></tr>")


			});
			window.setTimeout(predictionTimer, 2500)
		});
	};
	predictionTimer();
});
