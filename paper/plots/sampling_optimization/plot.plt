#
# Optimization for brochure resampling
#

#set terminal wxt size 800,700
set terminal svg size 800,700 fname fontname fsize fontsize
set output '../sampling_optimization.svg'

set xrange [0:16]
set yrange [30:80]
set xlabel "Group size or window size"
set ylabel "Accuracy"
set key right bottom
set datafile separator ","
plot './data.csv' using 1:2 title 'Grouping' linetype 7 with lines, \
     './data.csv' using 1:3 title 'Sliding' with lines
