from os.path import dirname, join
import numpy as np

def __build_data(fname):
	ids, X_train, y_train, predict_ids, X_predict = [], [], [], [], []
	with open(join(dirname(dirname(__file__)), fname)) as f:
		first = True
		for line in f:
			if first: first = False; continue
			content = line.strip().split(",")
			cls = content[-1]
			if not content or cls == "no-idea":
				continue
			if cls == "":
				predict_ids.append(content[0])
				X_predict.append([float(val) for val in content[3:-1]])
				continue

			ids.append(content[0])
			X_train.append([float(val) for val in content[3:-1]])
			y_train.append(cls)

	return np.array(ids), np.array(X_train), np.array(y_train), None, predict_ids, X_predict


def build_demand_data(printFoo = True):
	if printFoo:
		print "=== Custom Feature Extractor ==="
	return __build_data("n2o_data/features.csv")


def build_product_data(product_class):
	print "=== Custom Feature Extractor ==="
	return __build_data("n2o_data/features_%s.csv"%(product_class))

