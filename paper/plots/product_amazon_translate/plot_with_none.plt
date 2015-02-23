#
# basic product classifier evaluation
#

# set terminal wxt size 800,700
set terminal svg size 800,700 fname fontname fsize fontsize
set output '../product_translate_amazon_with_none.svg'

set xrange [-0.4:4.4]
set yrange [0:100]
# set xlabel "Group size or window size"
set ylabel "Overall Precision in %"
set xtics ("Perceptron" 0.45, "SVM" 1.85, "Logistic" 3.45)
set key right top
set boxwidth 0.3
set style fill solid
set datafile separator ","
plot \
	'data_with_none.csv' every 4::1 using 1:3 with boxes lt rgb color_1 title "Translated and Amazon" , \
	'data_with_none.csv' every 4::1 using 1:($3+2):3 with labels notitle, \
	'data_with_none.csv' every 4::2 using 1:3 with boxes lt rgb color_2 title "Translated only" , \
	'data_with_none.csv' every 4::2 using 1:($3+2):3 with labels notitle, \
	'data_with_none.csv' every 4::3 using 1:3 with boxes lt rgb color_3 title "Amazon only" , \
	'data_with_none.csv' every 4::3 using 1:($3+2):3 with labels notitle, \
	'data_with_none.csv' every 4    using 1:3 with boxes lt rgb color_4 title "Original brochures", \
	'data_with_none.csv' every 4    using 1:($3+2):3 with labels notitle
