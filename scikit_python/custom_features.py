from os.path import dirname, join
import numpy as np


def __build_data(fname):
	ids, features, target = [], [], []
	with open(join(dirname(dirname(__file__)), fname)) as f:
		first = True
		for line in f:
			if first: first = False; continue
			content = line.strip().split(",")
			cls = content[-1]
			if not content or cls == "no-idea":
				continue

			ids.append(content[0])
			features.append([float(val) for val in content[3:-1]])
			target.append(cls)

	return np.array(ids), np.array(features), np.array(target), None, None


def build_demand_data():
	print "=== Custom Feature Extractor ==="
	return __build_data("n2o_data/features.csv")


def build_product_data(product_class):
	print "=== Custom Feature Extractor ==="
	return __build_data("n2o_data/features_%s.csv"%(product_class))

