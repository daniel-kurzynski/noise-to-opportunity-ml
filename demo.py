from os.path import dirname, join
from sklearn.feature_extraction.text import TfidfVectorizer
import numpy as np
from time import time
from sklearn import metrics
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.feature_extraction.text import HashingVectorizer
from sklearn.feature_selection import SelectKBest, chi2
from sklearn.linear_model import RidgeClassifier
from sklearn.svm import LinearSVC
from sklearn.linear_model import SGDClassifier
from sklearn.linear_model import Perceptron
from sklearn.linear_model import PassiveAggressiveClassifier
from sklearn.naive_bayes import BernoulliNB, MultinomialNB
from sklearn.neighbors import KNeighborsClassifier
from sklearn.neighbors import NearestCentroid
import numpy as np
import matplotlib.pyplot as plt



def category_to_idx(category, content):
  for i in range(len(content["target_names"])):
    if category == content["target_names"][i]:
      return i

def read(filename, line_extractor):
  output = {
    "data": [],
    "target": [],
    "target_names": [],
  }
  with open(join(dirname(__file__), filename)) as f:
    for line in f:
      data, category = line_extractor(line)
      output["data"].append(data)
      if category not in output["target_names"]:
        output["target_names"].append(category)
      output["target"].append(category_to_idx(category, output))

  return output


def brochure_exractor(line):
  _, data, category, _ = line.replace("\\,", "<komma>").split(",")
  return data.replace("<komma>", ",")[1:-1], category

def linked_in_extractor(line):
  _, data1, data2, _, _, _, _, _, category, _, _ = line.replace("\\,", "<komma>").split(",")
  data = data1[1:-1] + " " + data2[1:-1]
  return data.replace("<komma>", ","), category


def process(filename, line_extractor):
  content = read(filename, line_extractor)
  vectorizer = TfidfVectorizer(sublinear_tf=True, max_df=0.5, stop_words='english')

  X_train = vectorizer.fit_transform(content["data"])
  y_train = np.array(content["target"])

  X_test = vectorizer.transform(content["data"])
  y_test = np.array(content["target"])

  results = []
  for classifier, name in (
        (RidgeClassifier(tol=1e-2, solver="lsqr"), "Ridge Classifier"),
        (Perceptron(n_iter=50), "Perceptron"),
        (PassiveAggressiveClassifier(n_iter=50), "Passive-Aggressive"),
        (KNeighborsClassifier(n_neighbors=10), "kNN")):

  	results.append(benchmark(classifier,X_train,X_test,y_train,y_test))

  print_result(results)
  visualize_result(results)

def print_result(result):
	for classifier, train_time, test_time, score, confusion_matrix in result:
		print classifier
		print("train time: %0.13fs" % train_time)
		print("test time:  %0.13fs" % test_time)
		print("f1-score:   %0.13f" % score)
		print confusion_matrix

def benchmark(classifier,X_train,X_test,y_train,y_test):
	t0 = time()
	classifier.fit(X_train, y_train)
	train_time = time() - t0

	print train_time

	t0 = time()
	pred = classifier.predict(X_test)
	test_time = time() - t0

	score = metrics.f1_score(y_test, pred)

	confusion_matrix = metrics.confusion_matrix(y_test, pred)

	return classifier.__class__.__name__,train_time, test_time, score,confusion_matrix

def visualize_result(results):
	indices = np.arange(len(results))

	results = [[x[i] for x in results] for i in range(4)]

	classifier_names, training_time, test_time, score = results
	if(np.max(training_time)>0):
		training_time = np.array(training_time) / np.max(training_time)
	if(np.max(test_time)>0):
		test_time = np.array(test_time) / np.max(test_time)

	plt.figure(figsize=(12, 8))
	plt.title("Score")
	plt.barh(indices, score, .2, label="score", color='r')
	plt.barh(indices + .3, training_time, .2, label="training time", color='g')
	plt.barh(indices + .6, test_time, .2, label="test time", color='b')
	plt.yticks(())
	plt.legend(loc='best')
	plt.subplots_adjust(left=.25)
	plt.subplots_adjust(top=.95)
	plt.subplots_adjust(bottom=.05)

	for i, c in zip(indices, classifier_names):
	    plt.text(-.3, i, c)

	plt.show()

print "Brochures"
print "="*50
process("data/brochures.csv", brochure_exractor)
print "="*50


print "LinkedIn Posts"
print "="*50
process("data/linked_in_posts.csv", linked_in_extractor)
print "="*50
