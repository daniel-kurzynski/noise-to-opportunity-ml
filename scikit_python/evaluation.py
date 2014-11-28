# from active_learner import active_learner
import matplotlib.pyplot as plt
from sklearn.metrics import f1_score, recall_score, \
	precision_score, make_scorer, \
	confusion_matrix
from sklearn.cross_validation import cross_val_score, train_test_split

from bag_of_words import build_data as bow
from custom_features import build_data as custom_features

from sklearn.linear_model import Perceptron

def score(y_true, y_pred, score_function, label_index):
	return score_function(y_true, y_pred, average=None)[label_index]

def evaluate_classifier(classifier, X, y):
	f1_demand_scorer = make_scorer(score, greater_is_better=True, score_function=f1_score, label_index=0)
	f1_no_demand_scorer = make_scorer(score, greater_is_better=True, score_function=f1_score, label_index=1)

	recall_demand_scorer = make_scorer(score, greater_is_better=True, score_function=recall_score, label_index=0)
	recall_no_demand_scorer = make_scorer(score, greater_is_better=True, score_function=recall_score, label_index=1)


	precision_demand_scorer = make_scorer(score, greater_is_better=True, score_function=precision_score, label_index=0)
	precision_no_demand_scorer = make_scorer(score, greater_is_better=True, score_function=precision_score, label_index=1)


	print "f1-demand          ", cross_val_score(classifier, X, y, cv=5, scoring=f1_demand_scorer).mean()
	print "f1-no-demand       ", cross_val_score(classifier, X, y, cv=5, scoring=f1_no_demand_scorer).mean()

	print "recall-demand      ", cross_val_score(classifier, X, y, cv=5, scoring=recall_demand_scorer).mean()
	print "recall-no-demand   ", cross_val_score(classifier, X, y, cv=5, scoring=recall_no_demand_scorer).mean()

	print "precision-demand   ", cross_val_score(classifier, X, y, cv=5, scoring=precision_demand_scorer).mean()
	print "precision-no-demand", cross_val_score(classifier, X, y, cv=5, scoring=precision_no_demand_scorer).mean()

def conf_matrix(classifier, X, y):
	X_train, X_test, y_train, y_test = train_test_split(X, y, random_state = 0)
	y_pred = classifier.fit(X_train, y_train).predict(X_test)
	return confusion_matrix(y_test, y_pred)

if __name__ == "__main__":
	classifier = Perceptron(n_iter = 50)
	for build_data in [bow, custom_features]:
		print
		X, y = build_data(class_names = {"demand": 0, "no-demand": 1})
		evaluate_classifier(classifier, X, y)
		cm = conf_matrix(classifier, X, y)
		print cm
		# Show confusion matrix in a separate window
		# plt.matshow(cm)
		# plt.title('Confusion matrix')
		# plt.colorbar()
		# plt.ylabel('Actual demand')
		# plt.xlabel('Predicted demand')
		# plt.show()
