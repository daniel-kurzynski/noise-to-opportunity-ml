from os.path import *


out = open(join(dirname(dirname(__file__)), "n2o_data/brochures_with_lang.csv"), "w")
with open(join(dirname(dirname(__file__)), "n2o_data/brochures_original.csv")) as f:
	for line in f:
		line = line.replace("\\,", "<komma>")
		idx, text, cls, foo = line.strip().split(",")
		text = text.replace("<komma>", "\\,")
		print "\n"
		lang = raw_input(text[20:150] + "\nen or de(default):") or "de"
		print "\n"
		out.write(",".join([idx, text, cls, foo, lang]))
		out.write("\n")
