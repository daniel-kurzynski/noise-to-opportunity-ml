#
# basic product classifier evaluation
#

# set terminal wxt size 800,700
set terminal svg size 800,700 fname fontname fsize fontsize
set output '../product_translate_amazon_with_none.svg'

set xrange [-0.4:2.9]
set yrange [0:100]
# set xlabel "Group size or window size"
set ylabel "Accuracy in %"
set xtics ("Perceptron" 0.3, "SVM" 1.3, "Logistic" 2.3)
set key right top
set boxwidth 0.2
set style fill solid
set datafile separator ","
plot \
	'data_with_none.csv' every 4::1 using 1:3 with boxes lt rgb color_1 title "Translated and with Amazon book descriptions" , \
	'data_with_none.csv' every 4::1 using 1:($3+2):3 with labels notitle, \
	'data_with_none.csv' every 4::2 using 1:3 with boxes lt rgb color_2 title "Translated and without Amazon book descriptions" , \
	'data_with_none.csv' every 4::2 using 1:($3+2):3 with labels notitle, \
	'data_with_none.csv' every 4::3 using 1:3 with boxes lt rgb color_3 title "Not translated and with Amazon book descriptions" , \
	'data_with_none.csv' every 4::3 using 1:($3+2):3 with labels notitle, \
	'data_with_none.csv' every 4    using 1:3 with boxes lt rgb color_4 title "Not translated and without Amazon book descriptions", \
	'data_with_none.csv' every 4    using 1:($3+2):3 with labels notitle
