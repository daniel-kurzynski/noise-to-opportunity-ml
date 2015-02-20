#
# evaluation of the feature selection design decision
#

# set terminal wxt size 800,700
set terminal svg size 800,700 fname fontname fsize fontsize
set output '../product_feature_selection_with_none.svg'

# set xrange [-0.4:3.9]
set yrange [0:100]
set ylabel "Accuracy in %"
set xtics ("Multilayer Perceptron" 0.2, "SMO" 1.2, "Logistic" 2.2)
set key right top
set boxwidth 0.25
set style fill solid
set datafile separator ","
plot \
	'./data_with_none.csv' every 3::1 using 1:3 with boxes lt rgb "#222222" title "10 most occured words" , \
	'./data_with_none.csv' every 3::1 using 1:($3+2):3 with labels notitle, \
	'./data_with_none.csv' every 3::2 using 1:3 with boxes lt rgb "#666666" title "100 most occured words" , \
	'./data_with_none.csv' every 3::2 using 1:($3+2):3 with labels notitle, \
	'./data_with_none.csv' every 3 		using 1:3 with boxes lt rgb "#AAAAAA" title "1000 most occured words", \
	'./data_with_none.csv' every 3 		using 1:($3+2):3 with labels notitle
