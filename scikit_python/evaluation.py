from sklearn.metrics import recall_score, \
	precision_score, confusion_matrix
from sklearn.cross_validation import ShuffleSplit
from sklearn.base import clone
import numpy as np
from scipy.sparse import issparse

from bag_of_words    import build_data as bow
from custom_features import build_data as custom_features

from sklearn.linear_model import LogisticRegression, Perceptron, SGDClassifier, RidgeClassifier
from sklearn.svm          import LinearSVC
from sklearn.tree         import DecisionTreeClassifier
from sklearn.naive_bayes  import MultinomialNB, BernoulliNB

def score(y_true, y_pred, score_function, label_index):
	return score_function(y_true, y_pred, average=None)[label_index]

def evaluate_classifier(base_classifier, X, y):
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
		print "%20s %.12f" %(vocabulary[index],coef[index] )

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



if __name__ == "__main__":
	classifier = LogisticRegression()
	classifier = Perceptron(n_iter = 50)
	classifier = MultinomialNB()
	classifier = DecisionTreeClassifier()
	classifier = SGDClassifier()
	classifier = RidgeClassifier()
	classifier = LinearSVC()
	classifier = BernoulliNB()

	print classifier.__class__

	for build_data in [bow, custom_features]:
		X, y, vectorizer = build_data()
		if vectorizer:
			most_weighted_features(classifier, X, y, vectorizer)
		X = X.todense() if issparse(X) else X
		evaluate_classifier(classifier, X, y)
		# Show confusion matrix in a separate window
		# plt.matshow(cm)
		# plt.title('Confusion matrix')
		# plt.colorbar()
		# plt.ylabel('Actual demand')
		# plt.xlabel('Predicted demand')
		# plt.show()
