import simplejson as json
from os import sys, listdir
from os.path import abspath, join

folder = abspath(sys.argv[1])

files = [set(json.load(open(join(folder, fname))).keys()) for fname in listdir(folder) if fname.endswith(".json")]

print reduce(lambda a,b: a.intersection(b), files)
