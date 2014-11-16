
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

from preprocessing import CSVReader

def classifiers():
	for clf in [
		RidgeClassifier(tol=1e-2, solver="lsqr"),
		Perceptron(n_iter=50),
		PassiveAggressiveClassifier(n_iter=50),
		KNeighborsClassifier(n_neighbors=10),
		LinearSVC(loss='l2', penalty="l1", dual=False, tol=1e-3),
		LinearSVC(loss='l2', penalty="l2", dual=False, tol=1e-3),
		SGDClassifier(alpha=.0001, n_iter=50, penalty="l1"),
		SGDClassifier(alpha=.0001, n_iter=50, penalty="l2"),
		SGDClassifier(alpha=.0001, n_iter=50, penalty="elasticnet"),
		NearestCentroid(),
		MultinomialNB(alpha=.01),
		BernoulliNB(alpha=.01),
		]:
		yield clf


def process(filename, extractor, print_results = True, visualize_results = True):

	csv_reader = CSVReader()
	content = csv_reader.read(filename, extractor)
	vectorizer = TfidfVectorizer(sublinear_tf=True, max_df=0.5, stop_words='english')

	results = []
	clf_args = [vectorizer.fit_transform(csv_reader.data), np.array(csv_reader.target)]

	results = [benchmark(classifier, *clf_args) for classifier in classifiers()]

	if print_results:
		print_(results)

	if visualize_results:
		visualize(results)


def print_(result):
	for classifier, train_time, score in result:
		print classifier
		print("time: %0.13fs" % train_time)
		# print("test time:  %0.13fs" % test_time)
		print("f1-score:   %0.13f" % score)
		# print confusion_matrix


def benchmark(classifier, X, y):
	t0 = time()
	score = cross_val_score(classifier, X, y, cv=5, scoring='f1')
	t = time() - t0
	return classifier.__class__.__name__, t, score.mean()

	# t0 = time()
	# classifier.fit(X, y)
	# train_time = time() - t0

	# print train_time

	# t0 = time()
	# pred = classifier.predict(X_test)
	# test_time = time() - t0

	# score = metrics.f1_score(y_test, pred)

	# confusion_matrix = metrics.confusion_matrix(y_test, pred)

	# return classifier.__class__.__name__, train_time, test_time, score, confusion_matrix


def visualize(results):
	def normalize(l):
		if (np.max(l) > 0):
			l = np.array(l) / np.max(l)
		return l

	indices = np.arange(len(results))

	classifier_names, training_time, score = zip(*results)
	# training_time = normalize(training_time)
	# test_time = normalize(test_time)

	plt.figure(figsize=(12, 8))
	plt.title("Score")
	plt.barh(indices, score, .6, label="score", color="#df9800")
	# plt.barh(indices + .3, training_time, .2, label="training time", color='g')
	# plt.barh(indices + .6, test_time, .2, label="test time", color='b')
	plt.yticks(())
	plt.legend(loc='best')
	plt.subplots_adjust(left=.25)
	plt.subplots_adjust(top=.95)
	plt.subplots_adjust(bottom=.05)

	for idx, clf_name in zip(indices, classifier_names):
		plt.text(-.3, idx, clf_name)

	plt.show()


print "Brochures"
print "=" * 50
process("brochures.csv", CSVReader.brochure_extractor)
print "=" * 50


# print "LinkedIn Posts"
# print "="*50
# process("linked_in_posts.csv", CSVReader.linked_in_extractor)
# print "="*50
