#
# evaluation of the feature selection design decision
#

# set terminal wxt size 800,700
set terminal svg size width,height fname fontname fsize fontsize
set output '../product_feature_selection_without_none.svg'

# set xrange [-0.4:3.9]
set yrange [0:110]
set ylabel "Overall Precision in %"
set xtics ("Perceptron" 0.2, "SVM" 1.2, "Logistic" 2.2)
set key right top
set boxwidth 0.25
set style fill solid
set datafile separator ","
plot \
	'data_without_none.csv' every 3::1 using 1:3 with boxes lt rgb color_1 title "10 most occurring words" , \
	'data_without_none.csv' every 3::1 using 1:($3+6):3 with labels rotate by 90 notitle, \
	'data_without_none.csv' every 3::2 using 1:3 with boxes lt rgb color_2 title "100 most occurring words" , \
	'data_without_none.csv' every 3::2 using 1:($3+6):3 with labels rotate by 90 notitle, \
	'data_without_none.csv' every 3    using 1:3 with boxes lt rgb color_3 title "1,000 most occurring words", \
	'data_without_none.csv' every 3    using 1:($3+6):3 with labels rotate by 90 notitle
