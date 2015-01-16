from sklearn.metrics import recall_score, \
	precision_score, confusion_matrix
from sklearn.utils.multiclass import unique_labels
from sklearn.cross_validation import ShuffleSplit
from sklearn.base import clone
import numpy as np
from sklearn.lda import LDA
from sklearn.decomposition import PCA
from scipy.sparse import issparse

from sklearn.linear_model import LogisticRegression, Perceptron, SGDClassifier, RidgeClassifier
from sklearn.svm          import LinearSVC
from sklearn.tree         import DecisionTreeClassifier
from sklearn.naive_bayes  import MultinomialNB, BernoulliNB
from sklearn.base         import BaseEstimator

from sklearn.linear_model import LogisticRegression, Perceptron
import matplotlib.pyplot as plt
import sys
from os.path import join, abspath, dirname

from preprocessing import CSVReader

csv_reader = CSVReader()
csv_reader.read("../n2o_data/linked_in_posts.csv", CSVReader.linked_in_extractor)
all_posts = csv_reader.data

def score(y_true, y_pred, score_function, label_index):
	return score_function(y_true, y_pred, average=None)[label_index]

def validate(base_classifier, X_train, y_train, X_test, y_true, class_name):
	base_classifier.fit(X_train, y_train)
	y_pred = base_classifier.predict(X_test)
	labes = ["{:^7s}".format(s) for s in unique_labels(y_true, y_pred)]
	recall = ["{:7s}".format("%.4f" %p) for p in recall_score(y_true, y_pred, average = None)]
	prec = ["{:7s}".format("%.4f" %p) for p in precision_score(y_true, y_pred, average = None)]
	conf_matrix = confusion_matrix(y_true, y_pred)
	print "{:<15s}{:s}\n{:<15s}{:s}\n{:<15s}{:s}".format(
		"Labels:", "  ".join(labes),
		"Recall:", "  ".join(recall),
		"Precision:", "  ".join(prec))
	print conf_matrix

def cross_validate(ids, base_classifier, X, y, class_name):
	splitter = ShuffleSplit(X.shape[0], n_iter = 5, test_size = 0.2, random_state = 17)

	precision_scores = []
	recall_scores    = []

	overall_confusion = np.array([[0, 0], [0, 0]])

	for train_index, test_index in splitter:
		classifier = base_classifier if "AverageClassifier" in base_classifier.__class__.__name__ else clone(base_classifier)

		classifier.fit(X[train_index], y[train_index])

		y_predict = classifier.predict(X[test_index])
		y_true  = y[test_index]

		assert len(y_predict) == len(y_true)


		if len(ids) > 0 and "fps" in args:
			current_ids = ids[test_index]
			fp_posts_ids = dict([(current_ids[i], i) for i in range(len(y_true)) if y_predict[i] == "no-demand" and y_true[i] == "demand"])

			fp_posts = [(fp_posts_ids[post.id], post) for post in all_posts if post.id in fp_posts_ids.keys()]
			for i, p in fp_posts:
				print p.id
				# print y_true[i]
				print p.data, "\n"

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
	print "Precision-%s:   "%(class_name), micro_precision_class,    " (micro)"
	print "Recall-%s:      "%(class_name), micro_recall_class,       " (micro)"
	print "F1-%s:          "%(class_name), micro_f1_class,           " (micro)"

	overall_precision = (overall_tp + overall_tn) / float(overall_tp + overall_tn + overall_fp + overall_fn)
	print "Overall Precision   ", overall_precision
	# print "Precision-No%s: "%(class_name), micro_precision_no_class, " (micro)"
	# print "Recall-No%s:    "%(class_name), micro_recall_no_class,    " (micro)"
	# print "F1-No%s:        "%(class_name), micro_f1_no_demand,        " (micro)"
	print overall_confusion

def print_mosth_weighted_features(indices, vocabulary, coef):
	for index in indices:
		print "{:^20s} {:f}".format(vocabulary[index], coef[index])

def most_weighted_features(classifier, X, y, vectorizer):
	classifier.fit(X, y)
	if not hasattr(classifier, "coef_"):
		return
	indices = np.argsort(classifier.coef_[0])
	demand_indices = indices[:10]
	no_demand_indices = indices[-10:]

	inverted_vocabulary = dict([[v,k] for k,v in vectorizer.vocabulary_.items()])

	print "=== demand words ==="
	print_mosth_weighted_features(demand_indices,inverted_vocabulary,classifier.coef_[0])

	print "=== no demand words ==="
	print_mosth_weighted_features(no_demand_indices,inverted_vocabulary,classifier.coef_[0])

def reduce_dimensonality(method, X,y,X_unlabeled):
	X_new = method.fit_transform(X,y)

	x0_unlabeled = []
	x1_unlabeled = []

	if X_unlabeled is not None:
		X_unlabeled_new = method.transform(X_unlabeled)

		if(X_unlabeled_new.shape[1]<2):
			X_unlabeled_new = [[x,0] for x in X_unlabeled_new]

		X_unlabeled_new = np.array(X_unlabeled_new)

		x0_unlabeled = [x[0] for x in X_unlabeled_new]
		x1_unlabeled = [x[1] for x in X_unlabeled_new]

	if(X_new.shape[1]<2):
		X_new = [[x,0] for x in X_new]



	X_new = np.array(X_new)
	XY = np.array(zip(X_new,y))


	x0_demand = [x[0][0] for x in XY if x[1]=="demand"]
	x1_demand = [x[0][1] for x in XY if x[1]=="demand"]

	x0_no_demand = [x[0][0] for x in XY if x[1]=="no-demand"]
	x1_no_demand = [x[0][1] for x in XY if x[1]=="no-demand"]



	return x0_demand, x1_demand, x0_no_demand, x1_no_demand, x0_unlabeled, x1_unlabeled

def visualize_posts(X,y,X_unlabeled):
	lda = LDA(n_components=2)
	pca = PCA(n_components=2)

	for method in [pca, lda]:
		x0_demand, x1_demand, x0_no_demand, x1_no_demand, x0_unlabeled, x1_unlabeled = reduce_dimensonality(method, X, y, X_unlabeled)
		plt.title("Reduction: " + str(method))
		plt.scatter(x0_unlabeled, x1_unlabeled, c="b", marker=",", s=10)
		plt.scatter(x0_no_demand, x1_no_demand, c="r", marker="v", s=100)
		plt.scatter(x0_demand, x1_demand, c="g", marker="^", s=100)

		plt.show()

def write_out_demand_posts(ids, fname):
	with open(join(dirname(dirname(__file__)), "n2o_data", "linked_in_posts.csv")) as fp:
		with open(join(dirname(dirname(__file__)), "n2o_data", fname), "w") as out:
			for line in fp:
				if line.split(",")[0][1:-1] in ids:
					out.write(line)

def run_demand(classifierDict):
	from bag_of_words    import build_demand_data as bow
	from custom_features import build_demand_data as custom_features

	classifier = classifierDict.get("all",classifierDict.get("demand"))

	t = "===== Demand Evaluation of %s =====" %classifier.__class__.__name__
	print t

	build_datas = []
	if "bow" in args:
		build_datas.append(bow)
	if "custom" in args:
		build_datas.append(custom_features)
	for build_data in build_datas:
		ids, X_train, y_train, vectorizer, predict_ids, X_predict = build_data()
		if vectorizer and "most" in args:
			most_weighted_features(classifier, X_train, y_train, vectorizer)
		X_train = X_train.todense() if issparse(X_train) else X_train
		X_predict = X_predict.todense() if issparse(X_predict) else X_predict
		if "writeout" in args:
			classifier.fit(X_train, y_train)
			y_predict = classifier.predict(X_predict)
			write_out_demand_posts(
				set([predict_ids[i] for i, cls in enumerate(y_predict) if cls == "demand"]),
				"demand_linked_in_posts.csv")
		elif "single" in args:
			print len(X_predict)
			classifier.fit(X_train, y_train)
			y_predict = classifier.predict(X_predict)
			print(y_predict)
		else:
			cross_validate(ids, classifier, X_train, y_train, "Demand")
		if "vis" in args:
			visualize_posts(X_train,y_train , X_predict)
	print "=" * len(t)

def run_product(classifierDict):
	from bag_of_words import build_product_data as bow
	from custom_features import build_product_data as custom_features

	product_classes = [
		"CRM",
		"ECOM",
		"HCM",
		"LVM",
	]

	for class_name in product_classes:

		classifier = classifierDict.get("all",classifierDict.get(class_name))
	
		t = "===== Product Evaluation of %s for %s =====" %(classifier.__class__.__name__,class_name)
		print t


		build_datas = []
		if "bow" in args:
			build_datas.append(bow)
		if "custom" in args:
			build_datas.append(custom_features)
		for build_data in build_datas:
			X_train, y_train, X_test_or_predict, y_true, predict_ids = build_data(class_name)
			if "writeout" in args:
				classifier.fit(X_train, y_train)
				y_predict = classifier.predict(X_test_or_predict)
				write_out_demand_posts(
					set([predict_ids[i] for i, cls in enumerate(y_predict) if cls == class_name]),
					"{}_linked_in_posts.csv".format(class_name))
			else:
				validate(classifier, X_train, y_train, X_test_or_predict, y_true, class_name)
				# if "vis" in args:
				# 	visualize_posts(X, y, X_unlabeled)
		print "=" * len(t)

	return





	X_train, y_train, X_test, y_true, categories = bow()
	X_train = X_train.todense() if issparse(X_train) else X_train
	X_test = X_test.todense() if issparse(X_test) else X_test


	for category in categories:
		y_train_category = [category if y == category else "not-" + category for y in y_train]
		y_true_category =  [category if y == category else "not-" + category for y in y_true]

		validate(classifier, X_train, y_train_category, X_test, y_true_category)

	validate(classifier, X_train, y_train, X_test, y_true)

	print "=" * len(t)


class VotingClassifier(BaseEstimator):
	def __init__(self, classifiers):
		self.classifiers = classifiers


	def fit(self, X, y):
		tmp = [clone(classifier) for classifier in self.classifiers]
		self.classifiers = tmp

		for classifier in self.classifiers:
			classifier.fit(X, y)

	def predict(self, X):
		results = []
		for classifier in self.classifiers:
			results.append(classifier.predict(X))

		y_predict = []
		for i in range(len(results[0])):
			votes = [vote[i] for vote in results]
			demand_count = votes.count("demand")
			no_demand_count = votes.count("no-demand")
			assert demand_count + no_demand_count == len(self.classifiers)
			if demand_count > no_demand_count - 1:
				y_predict.append("demand")
			else:
				y_predict.append("no-demand")
			# print votes, demand_count, no_demand_count

		return y_predict

class Classifiers(object):
	CLASSIFIERS = [
		{"all": LogisticRegression()},
		{"all": Perceptron(n_iter = 50)},
		{"all": MultinomialNB()},
		{"all": DecisionTreeClassifier()},
		{"all": SGDClassifier()},
		{"all": RidgeClassifier()},
		{"all": LinearSVC()},
		{
			"demand": BernoulliNB(class_prior = [0.6, 0.4]),
			"HCM": BernoulliNB(class_prior = [0.6, 0.4]),
			"ECOM": BernoulliNB(class_prior = [0.6, 0.4]),
			"LVM": BernoulliNB(class_prior = [0.6, 0.4]),
			"CRM": BernoulliNB(class_prior = [0.1, 0.2])
		}]
	# average_classifier = VotingClassifier(CLASSIFIERS[:])
	# CLASSIFIERS.append(average_classifier)

	LOG_REGRESSION, \
	PERCEPTRON, \
	MULTI_NB, \
	DTC, \
	SGD, \
	RIDGE, \
	LIN_SVC, \
	BERNOULLI_NB = range(len(CLASSIFIERS))

if __name__ == "__main__":
	args = sys.argv
	if len(args) == 1:
		print "Possible args: vis, fps, most"
		print "Possible datasets: bow, custom"
		print "Possible classifications: all, demand, product"
		print "Writeout positive classified posts(demand and product): writeout"
		sys.exit(0)

	classifierDict = Classifiers.CLASSIFIERS[Classifiers.BERNOULLI_NB]

	if "all" in args:
		for cld in Classifiers.CLASSIFIERS:
			if "demand" in args:
				run_demand(cld)
			if "product" in args:
				run_product(cld)
	else:
		if "demand" in args:
			run_demand(classifierDict)
		if "product" in args:
			run_product(classifierDict)

