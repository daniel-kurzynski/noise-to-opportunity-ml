from os.path import dirname, join

from time import time
from sklearn import metrics
from sklearn.cross_validation import cross_val_score
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import RidgeClassifier
from sklearn.svm import LinearSVC
from sklearn.linear_model import SGDClassifier, Perceptron, PassiveAggressiveClassifier
from sklearn.naive_bayes import BernoulliNB, MultinomialNB
from sklearn.neighbors import KNeighborsClassifier, NearestCentroid

import numpy as np
import matplotlib.pyplot as plt


def process(filename, line_extractor):
	content = read(filename, line_extractor)
	vectorizer = TfidfVectorizer(sublinear_tf=True, max_df=0.5, stop_words='english')

	X_train = vectorizer.fit_transform(content["data"])
	y_train = np.array(content["target"])

	# X_test = vectorizer.transform(content["data"])
	# y_test = np.array(content["target"])

	results = []
	# data = [X_train, X_test, y_train, y_test]
	data = [X_train, y_train]
	for classifier, name in (
			(RidgeClassifier(tol=1e-2, solver="lsqr"), "Ridge Classifier"),
			(Perceptron(n_iter=50), "Perceptron"),
			(PassiveAggressiveClassifier(n_iter=50), "Passive-Aggressive"),
			(KNeighborsClassifier(n_neighbors=10), "kNN")):
		results.append(benchmark(classifier, *data))
	for penalty in ["l2", "l1"]:
		# Train Liblinear model
		results.append(benchmark(
			LinearSVC(loss='l2', penalty=penalty, dual=False, tol=1e-3), *data))
		# Train SGD model
		results.append(benchmark(SGDClassifier(alpha=.0001, n_iter=50,
											   penalty=penalty), *data))
	# Train SGD with Elastic Net penalty
	results.append(benchmark(SGDClassifier(alpha=.0001, n_iter=50,
										   penalty="elasticnet"), *data))

	# Train NearestCentroid without threshold
	results.append(benchmark(NearestCentroid(), *data))

	# Train sparse Naive Bayes classifiers
	results.append(benchmark(MultinomialNB(alpha=.01), *data))
	results.append(benchmark(BernoulliNB(alpha=.01), *data))

	print_result(results)
	visualize_result(results)


def print_result(result):
	for classifier, train_time, score in result:
		print classifier
		print("time: %0.13fs" % train_time)
		# print("test time:  %0.13fs" % test_time)
		print("f1-score:   %0.13f" % score)
		# print confusion_matrix


def benchmark(classifier, X_train, y_train):
	t0 = time()
	score = cross_val_score(classifier, X_train, y_train, cv=5, scoring='f1')
	t = time() - t0
	return classifier.__class__.__name__, t, score.mean()

	# t0 = time()
	# classifier.fit(X_train, y_train)
	# train_time = time() - t0

	# print train_time

	# t0 = time()
	# pred = classifier.predict(X_test)
	# test_time = time() - t0

	# score = metrics.f1_score(y_test, pred)

	# confusion_matrix = metrics.confusion_matrix(y_test, pred)

	# return classifier.__class__.__name__, train_time, test_time, score, confusion_matrix


def visualize_result(results):
	indices = np.arange(len(results))

	results = [[x[i] for x in results] for i in range(3)]

	classifier_names, training_time, score = results
	# if (np.max(training_time) > 0):
	# 	training_time = np.array(training_time) / np.max(training_time)
	# if (np.max(test_time) > 0):
	# 	test_time = np.array(test_time) / np.max(test_time)

	plt.figure(figsize=(12, 8))
	plt.title("Score")
	plt.barh(indices, score, .2, label="score", color='r')
	# plt.barh(indices + .3, training_time, .2, label="training time", color='g')
	# plt.barh(indices + .6, test_time, .2, label="test time", color='b')
	plt.yticks(())
	plt.legend(loc='best')
	plt.subplots_adjust(left=.25)
	plt.subplots_adjust(top=.95)
	plt.subplots_adjust(bottom=.05)

	for i, c in zip(indices, classifier_names):
		plt.text(-.3, i, c)

	plt.show()


print "Brochures"
print "=" * 50
process("../data/brochures.csv", brochure_exractor)
print "=" * 50


# print "LinkedIn Posts"
# print "="*50
# process("data/linked_in_posts.csv", linked_in_extractor)
# print "="*50
