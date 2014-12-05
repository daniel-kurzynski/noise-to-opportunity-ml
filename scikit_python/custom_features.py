from os.path import dirname, join
import numpy as np

def build_demand_data():
	print "=== Custom Feature Extractor ==="
	ids, features, target = [], [], []
	with open(join(dirname(dirname(__file__)), "n2o_data/features.csv")) as f:
		first = True
		for line in f:
			line = line.replace("<br />", "")
			if first: first = False; continue
			content = line.strip().split(",")
			cls = content[-1]
			if not content or cls == "no-idea":
				continue

			ids.append(content[0])
			features.append([float(val) for val in content[3:-1]])
			target.append(cls)

	return np.array(ids), np.array(features), np.array(target), None


def build_product_data():
	return None, None, None, None

