from jsonmerge import merge
import simplejson as json
from os import sys, listdir
from os.path import abspath, join

folder = abspath(sys.argv[1])

files = [json.load(open(join(folder, fname))) for fname in listdir(folder) if fname.endswith(".json")]

with open(join(folder, "merge.json"), "w") as out:
	json.dump(reduce(lambda a,b: merge(a,b), files), out, indent = 2)
