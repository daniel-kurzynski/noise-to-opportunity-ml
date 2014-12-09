
from os.path import dirname, join
from numpy import array
from collections import OrderedDict

data = OrderedDict()
data["inv-no-demand"] = None
data["no-demand"] = None
data["inv-demand"] = None
data["demand"] = None

def make_bin(val):
	if float(val): return 1.0
	else: return 0.0

with open(join(dirname(dirname(__file__)), "n2o_data/features.csv")) as f:
	first = True
	for line in f:
		content = line.strip().split(",")
		if first:
			header = content[1:]
			header = [header[-1]] + header[:-1]
			first = False
			continue
		idx, feature, cls = content[0], array([make_bin(val) for val in content[1:-1]]), content[-1]
		if cls == "no-idea":
			continue

		cls_data = data.get(cls)
		if cls_data is None:
			data[cls] = feature
		else:
			data[cls] = cls_data + feature

		cls = "inv-" + cls
		feature_inv = array(map(lambda x: 1.0 if not x else 0.0, feature))
		cls_data = data.get(cls)
		if cls_data is None:
			data[cls] = feature_inv
		else:
			data[cls] = cls_data + feature_inv

sep = "\t"

def write_numbers(f):
	c = 0
	for cls, features in data.iteritems():
		if c == 2: f.write("\n")
		f.write(cls + sep)
		f.write(sep.join(["%d" %(val) for val in features]))
		f.write("\n")
		c += 1

def write_demands_for_each_word(f):
	def mapper(x, y):
		if x > 1 * y:
			return "DEMAND"
		elif y > 1 * x:
			return "NO-DEMAND"
		else: return ""
	demand_quot = data["demand"] / (data["inv-demand"] + data["demand"])
	no_demand_quot = data["no-demand"] / (data["inv-no-demand"] + data["no-demand"])
	f.write(sep)
	f.write(sep.join(map(mapper, demand_quot, no_demand_quot)))

with open(join(dirname(dirname(__file__)), "features_stats.csv"), "w") as f:
	f.write(sep.join(header))
	f.write("\n")
	write_numbers(f)
	f.write("\n")
	write_demands_for_each_word(f)
