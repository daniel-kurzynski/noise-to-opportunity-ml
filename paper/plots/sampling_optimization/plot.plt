#
# Optimization for brochure resampling
#

set terminal wxt size 800,700
set xlabel "Group size or window size"
set ylabel "Demand precision"
set key right bottom
set datafile separator ","
plot 'data.csv' using 1:2 title 'Grouping' pt 7, \
     'data.csv' using 1:3 title 'Sliding'
