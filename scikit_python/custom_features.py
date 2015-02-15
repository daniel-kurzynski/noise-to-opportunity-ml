from os.path import dirname, join
from scipy.sparse import issparse
import numpy as np

from constants import DEMAND_FEATURES, PRODUCT_FEATURES

def __build_data(f_path):
	ids, X, y, predict_ids, X_predict = [], [], [], [], []
	with open(f_path) as f:
		first = True
		for line in f:
			if first:
				first = False
				continue
			content = line.strip().split(",")
			cls = content[-1]
			if not content or cls == "no-idea":
				# print "class was no-idea({}) or content was empty({})!".format(cls == "no-idea", not content)
				continue
			if cls == "" or cls == "None":
				predict_ids.append(content[0])
				X_predict.append([float(val) for val in content[3:-1]])
			else:
				ids.append(content[0])
				X.append([float(val) for val in content[3:-1]])
				y.append(cls)

	X = np.array(X)
	X_predict = np.array(X_predict)
	return np.array(ids), X.todense() if issparse(X) else X, np.array(y), None, predict_ids, X_predict


def build_demand_data(printFoo = True):
	if printFoo:
		print "=== Custom Feature Extractor ==="
	return __build_data(DEMAND_FEATURES)


def build_product_data(product_class):
	print "=== Custom Feature Extractor ==="
	_, X_train, y_train, _, _, _ = __build_data(PRODUCT_FEATURES(product_class, ""))
	_, X_test, y_true, _, ids, X_predict = __build_data(PRODUCT_FEATURES(product_class, "_test"))

	if not len(X_test):
		X_test = X_predict

	return X_train, y_train, X_test, y_true, ids

