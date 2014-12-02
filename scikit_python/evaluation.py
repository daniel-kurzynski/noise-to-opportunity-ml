from sklearn.metrics import f1_score, recall_score, \
	precision_score, make_scorer, \
	confusion_matrix
from sklearn.cross_validation import cross_val_score, train_test_split, ShuffleSplit
from sklearn.base import clone


from bag_of_words import build_data as bow
from custom_features import build_data as custom_features

from sklearn.linear_model import Perceptron

def score(y_true, y_pred, score_function, label_index):
	return score_function(y_true, y_pred, average=None)[label_index]

def evaluate_classifier(base_classifier, X, y):
	# f1_demand_scorer           = make_scorer(score, greater_is_better=True, score_function=f1_score, label_index=0)
	# f1_no_demand_scorer        = make_scorer(score, greater_is_better=True, score_function=f1_score, label_index=1)
	recall_demand_scorer       = make_scorer(score, greater_is_better=True, score_function=recall_score, label_index=0)
	recall_no_demand_scorer    = make_scorer(score, greater_is_better=True, score_function=recall_score, label_index=1)
	precision_demand_scorer    = make_scorer(score, greater_is_better=True, score_function=precision_score, label_index=0)
	precision_no_demand_scorer = make_scorer(score, greater_is_better=True, score_function=precision_score, label_index=1)


	# print "F1-demand          ", cross_val_score(classifier, X, y, cv=5, scoring=f1_demand_scorer).mean()
	# print "F1-no-demand       ", cross_val_score(classifier, X, y, cv=5, scoring=f1_no_demand_scorer).mean()

	splitter = ShuffleSplit(X.shape[0], n_iter = 5, test_size = 0.2)


	for train_index, test_index in splitter:
		classifier = clone(base_classifier)
		classifier.fit(X[train_index], y[train_index])

		y_predict = classifier.predict(X[test_index])
		y_true  = y[test_index]

		print confusion_matrix(y_true, y_predict)
		# print precision_score(y_true, y_predict, average = None)
		print recall_score(y_true, y_predict, average = None)





	# print "Recall-demand      ", cross_val_score(base_classifier, X, y, cv=5, scoring=recall_demand_scorer).mean()
	# print "Recall-no-demand   ", cross_val_score(base_classifier, X, y, cv=5, scoring=recall_no_demand_scorer).mean()
	#
	# print "Precision-demand   ", cross_val_score(base_classifier, X, y, cv=5, scoring=precision_demand_scorer).mean()
	# print "Precision-no-demand", cross_val_score(base_classifier, X, y, cv=5, scoring=precision_no_demand_scorer).mean()

def conf_matrix(classifier, X, y):
	X_train, X_test, y_train, y_true = train_test_split(X, y, random_state = 0)
	y_pred = classifier.fit(X_train, y_train).predict(X_test)
	return confusion_matrix(y_true, y_pred)


def most_weighted_features(X,y, vectorizer):
	classifier.fit(X,y)
	indices = np.argsort(classifier.coef_[0])
	#print classifier.coef_[0][indices][:10]

if __name__ == "__main__":
	classifier = Perceptron(n_iter = 50)

	for build_data in [bow]:
		X, y, vectorizer = build_data()
		if vectorizer:
			most_weighted_features(X,y, vectorizer)
		# evaluate_classifier(classifier, X, y)
		# cm = conf_matrix(classifier, X, y)
		# print cm
		# Show confusion matrix in a separate window
		# plt.matshow(cm)
		# plt.title('Confusion matrix')
		# plt.colorbar()
		# plt.ylabel('Actual demand')
		# plt.xlabel('Predicted demand')
		# plt.show()
