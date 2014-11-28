from os.path import dirname, join
import numpy as np
from sklearn.cross_validation import cross_val_score
from sklearn.linear_model import Perceptron

ids, features, class_names = [], [], []

with open(join(dirname(dirname(__file__)), "n2o_data/features.csv")) as f:
	for line in f:
		content = line.strip().split(",")
		if not content: continue
		ids.append(content[0])
		features.append([float(val) for val in content[1:-1]])
		class_names.append(content[-1])

def convert_class_names(names):
	unique_names = {}
	target = []

	for name in names:
		unique_names[name] = unique_names.get(name, len(unique_names))
		target.append(unique_names[name])

	return target

X = np.array(features)
y = np.array(convert_class_names(class_names))

print X.shape, y.shape
classifier = Perceptron(n_iter = 50)

print "f1", cross_val_score(classifier, X, y, cv=3, scoring='f1').mean()
print "precision", cross_val_score(classifier, X, y, cv=3, scoring='precision').mean()
print "recall", cross_val_score(classifier, X, y, cv=3, scoring='recall').mean()


