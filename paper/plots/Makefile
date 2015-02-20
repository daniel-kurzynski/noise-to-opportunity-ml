gnuplot_args=-e "fontname='Verdana'; fontsize='14'; color_1='\#222222'; color_2='\#666666'; color_3='\#AAAAAA'"

run:
	cd sampling_optimization && gnuplot $(gnuplot_args) -p *.plt
	cd product_evaluation && gnuplot $(gnuplot_args) -p *.plt
	cd product_feature_selection && gnuplot $(gnuplot_args) -p *.plt
