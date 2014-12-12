from os.path import *


out = open(join(dirname(dirname(__file__)), "n2o_data/brochures_with_lang.csv"), "w")
with open(join(dirname(dirname(__file__)), "n2o_data/brochures.csv")) as f:
	for line in f:
		line = line.replace("\\,", "<komma>")
		idx, text, cls, foo = line.split(",")
		text = text.replace("<komma>", "\\,")
		lang = raw_input(text)

		out.write(",".join([idx, text, cls, foo, lang]))
