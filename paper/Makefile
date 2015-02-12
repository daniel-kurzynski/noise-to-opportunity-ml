show: make
	evince paper.pdf

make:
	@pdflatex -interaction=nonstopmode -halt-on-error -output-directory .output paper.tex 1>&2 > .output/error
	@mv .output/paper.pdf .

clean:
	rm .output/*.aux .output/*.log
