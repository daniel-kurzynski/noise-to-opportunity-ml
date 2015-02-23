#
# Optimization for brochure resampling
#

#set terminal wxt size 800,700
set terminal svg size 800,700 fname fontname fsize fontsize+5
set output '../sampling_optimization.svg'

set xrange [0:16]
set yrange [45:70]
set xlabel "Group size or window size"
set ylabel "Overall Precision in %"
set key right bottom
set datafile separator ","
set style line 1 lc rgb color_1 lt 1 lw 1 pt 5 pi -1 ps .5
set style line 2 lc rgb color_2 lt 1 lw 1 pt 7 pi -1 ps .5
set pointintervalbox .9
plot './data.csv' using 1:2 title 'Grouping' with linespoints ls 1, \
     './data.csv' using 1:3 title 'Sliding' with linespoints ls 2
