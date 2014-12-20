from os.path import dirname, join
from scipy.sparse import issparse
import numpy as np

def __build_data(fname):
	ids, X, y, predict_ids, X_predict = [], [], [], [], []
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
			X.append([float(val) for val in content[3:-1]])
			y.append(cls)

	X = np.array(X)
	return np.array(ids), X.todense() if issparse(X) else X, np.array(y), None, predict_ids, X_predict


def build_demand_data(printFoo = True):
	if printFoo:
		print "=== Custom Feature Extractor ==="
	return __build_data("n2o_data/features.csv")


def build_product_data(product_class):
	print "=== Custom Feature Extractor ==="
	_, X_train, y_train, _, _, _ = __build_data("n2o_data/features_%s.csv"%(product_class))
	_, X_test, y_true, _, _, _ = __build_data("n2o_data/features_test_%s.csv"%(product_class))

	return X_train, y_train, X_test, y_true

