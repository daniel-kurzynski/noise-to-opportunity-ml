from bag_of_words import get_labeled_posts
from preprocessing import Data, CSVReader
from collections import defaultdict
from os.path import *
import numpy as np, sys

FOLDER = "D:/SMM_DATA"

test_docs = get_labeled_posts("demand", "no-idea")


demand_file = join(FOLDER, "demand.txt")
no_demand_file = join(FOLDER, "no-demand.txt")

def read(file_path, cls):
	with open(file_path) as f:
		return [Data(str(idx), "", line, cls) for idx, line in enumerate(f)]

train_docs = read(demand_file, "demand") + read(no_demand_file, "no-demand")


from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import BernoulliNB
from sklearn.metrics import recall_score, \
	precision_score, confusion_matrix
from sklearn.utils.multiclass import unique_labels
from sklearn.base import clone


vectorizer = TfidfVectorizer(sublinear_tf=True, max_df=0.5, stop_words='english')
classifier = BernoulliNB()

if "cross" not in sys.argv:

	X_train = vectorizer.fit_transform(map(lambda doc: doc.data, train_docs))
	y_train = np.array(map(lambda doc: doc.get_class("demand"), train_docs))

	X_test = vectorizer.transform(map(lambda doc: doc.data, test_docs))
	y_true = np.array(map(lambda doc: doc.get_class("demand"), test_docs))

	classifier.fit(X_train, y_train)

	y_pred = classifier.predict(X_test)

	labes = ["{:^7s}".format(s) for s in unique_labels(y_true, y_pred)]
	recall = ["{:7s}".format("%.4f" %p) for p in recall_score(y_true, y_pred, average = None)]
	prec = ["{:7s}".format("%.4f" %p) for p in precision_score(y_true, y_pred, average = None)]
	conf_matrix = confusion_matrix(y_true, y_pred)
	print "{:<15s}{:s}\n{:<15s}{:s}\n{:<15s}{:s}".format(
		"Labels:", "  ".join(labes),
		"Recall:", "  ".join(recall),
		"Precision:", "  ".join(prec))
	print conf_matrix
else:
	from sklearn.cross_validation import ShuffleSplit

	X = vectorizer.fit_transform(map(lambda doc: doc.data, train_docs))
	y = np.array(map(lambda doc: doc.get_class("demand"), train_docs))

	splitter = ShuffleSplit(X.shape[0], n_iter = 5, test_size = 0.2, random_state = 17)

	precision_scores = []
	recall_scores    = []

	overall_confusion = np.array([[0, 0], [0, 0]])

	for train_index, test_index in splitter:
		new_classifier = clone(classifier)

		new_classifier.fit(X[train_index], y[train_index])

		y_predict = new_classifier.predict(X[test_index])
		y_true  = y[test_index]

		assert len(y_predict) == len(y_true)

		overall_confusion = overall_confusion + confusion_matrix(y_true, y_predict)
		precision_scores.append(precision_score(y_true, y_predict, average = None)[0])
		recall_scores.append(recall_score(y_true, y_predict, average = None)[0])

	# print "Precision-Demand: ", sum(precision_scores) / float(len(precision_scores)),     " (macro)"
	# print "Recall-Demand:    ", sum(recall_scores)    / float(len(recall_scores)),        " (macro)"
	overall_tp = overall_confusion[0][0]
	overall_fp = overall_confusion[1][0]
	overall_fn = overall_confusion[0][1]
	overall_tn = overall_confusion[1][1]
	micro_precision_class = overall_tp / float(overall_tp + overall_fp)
	micro_recall_class    = overall_tp / float(overall_tp + overall_fn)
	micro_f1_class        = 2 * micro_precision_class * micro_recall_class / (micro_recall_class + micro_precision_class)

	micro_precision_no_class = overall_tn / float(overall_tn + overall_fn)
	micro_recall_no_class    = overall_tn / float(overall_tn + overall_fp)
	micro_f1_no_class        = 2 * micro_precision_no_class * micro_recall_no_class / (micro_recall_no_class + micro_precision_no_class)
	print "Precision-Demand:   ", micro_precision_class,    " (micro)"
	print "Recall-Demand:      ", micro_recall_class,       " (micro)"
	print "F1-Demand:          ", micro_f1_class,           " (micro)"

	overall_precision = (overall_tp + overall_tn) / float(overall_tp + overall_tn + overall_fp + overall_fn)
	print "Overall Precision   ", overall_precision
	print overall_confusion
