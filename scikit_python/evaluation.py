from sklearn.metrics import recall_score, \
	precision_score, confusion_matrix
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

from sklearn.linear_model import LogisticRegression, Perceptron
import matplotlib.pyplot as plt

def score(y_true, y_pred, score_function, label_index):
	return score_function(y_true, y_pred, average=None)[label_index]

def validate(base_classifier, X_train, y_train, X_test, y_true):
	base_classifier.fit(X_train, y_train)
	y_pred = base_classifier.predict(X_test)

	recall = recall_score(y_true, y_pred)
	prec = precision_score(y_true, y_pred)
	conf_matrix = confusion_matrix(y_true, y_pred)

	print "{:<20s}{:f}\n{:<20s}{:f}".format("Recall:", recall, "Precision:", prec)
	print conf_matrix

def cross_validate(base_classifier, X, y):
	splitter = ShuffleSplit(X.shape[0], n_iter = 5, test_size = 0.2, random_state = 17)

	precision_scores = []
	recall_scores    = []

	overall_confusion = np.array([[0, 0], [0, 0]])

	for train_index, test_index in splitter:
		classifier = clone(base_classifier)

		classifier.fit(X[train_index], y[train_index])

		y_predict = classifier.predict(X[test_index])
		y_true  = y[test_index]

		overall_confusion = overall_confusion + confusion_matrix(y_true, y_predict)
		precision_scores.append(precision_score(y_true, y_predict, average = None)[0])
		recall_scores.append(recall_score(y_true, y_predict, average = None)[0])

	print "Precision-Demand: ", sum(precision_scores) / float(len(precision_scores)),     " (macro)"
	print "Recall-Demand:    ", sum(recall_scores)    / float(len(recall_scores)),        " (macro)"

	overall_tp = overall_confusion[0][0]
	print "Precision-Demand: ", overall_tp / float(overall_tp + overall_confusion[1][0]), " (micro)"
	print "Recall-Demand:    ", overall_tp / float(overall_tp + overall_confusion[0][1]), " (micro)"
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

def reduce_dimensonality(method, X,y):
	X_new = method.fit_transform(X,y)
	if(X_new.shape[1]<2):
		X_new = [[x,0] for x in X_new]

	X_new = np.array(X_new)
	XY = np.array(zip(X_new,y))


	x0_demand = [x[0][0] for x in XY if x[1]=="demand"]
	x1_demand = [x[0][1] for x in XY if x[1]=="demand"]

	x0_no_demand = [x[0][0] for x in XY if x[1]=="no-demand"]
	x1_no_demand = [x[0][1] for x in XY if x[1]=="no-demand"]

	return x0_demand, x1_demand, x0_no_demand, x1_no_demand

def visualize_posts(X,y):
	lda = LDA(n_components=2)
	pca = PCA(n_components=2)

	for method in [pca, lda]:
		x0_demand, x1_demand, x0_no_demand, x1_no_demand = reduce_dimensonality(method,X,y)
		plt.title("Reduction: " + str(method))
		plt.scatter(x0_demand,x1_demand, c="g", marker="^", s=100)
		plt.scatter(x0_no_demand,x1_no_demand, c="r", marker="v", s=100)
		plt.show()

def run_demand(classifier):
	t = "===== Demand Evalutation of %s =====" %classifier.__class__.__name__
	print t
	from bag_of_words    import build_demand_data as bow
	from custom_features import build_demand_data as custom_features
	for build_data in [bow, custom_features]:
		X, y, vectorizer = build_data()
		if vectorizer:
			most_weighted_features(classifier, X, y, vectorizer)
		X = X.todense() if issparse(X) else X
		cross_validate(classifier, X, y)
		# visualize_posts(X,y)
	print "=" * len(t)

def run_product(classifier):
	t = "===== Product Evalutation of %s =====" %classifier.__class__.__name__
	print t
	from bag_of_words    import build_product_data as bow
	from custom_features import build_product_data as custom_features

	X_train, y_train, X_test, y_true = bow()

	validate(classifier, X_train, y_train, X_test, y_true)

	print "=" * len(t)


if __name__ == "__main__":
	classifier = [
		LogisticRegression(),
		Perceptron(n_iter = 50),
		MultinomialNB(),
		DecisionTreeClassifier(),
		SGDClassifier(),
		RidgeClassifier(),
		LinearSVC(),
		BernoulliNB()]

	LOG_REGRESSION, \
	PERCEPTRON, \
	MULTI_NB, \
	DTC, \
	SGD, \
	RIDGE, \
	LIN_SVC, \
	BERNOULLI_NB = range(len(classifier))

	run_demand(classifier[BERNOULLI_NB])
	# run_product(classifier[BERNOULLI_NB])
