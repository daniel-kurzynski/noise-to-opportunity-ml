from os.path import dirname, join
from sklearn.feature_extraction.text import TfidfVectorizer
import numpy as np
from time import time
from sklearn import metrics
from sklearn.naive_bayes import MultinomialNB


content = {
  "idx": [],
  "data": [],
  "target": [],
  "target_names": [],
}

def category_to_idx(category):
  for i in range(len(content["target_names"])):
    if category == content["target_names"][i]:
      return i

with open(join(dirname(__file__), "data/brochures.csv")) as f:
  for line in f:
    line = line.replace("\\,", "<komma>")
    idx, data, category, _ = line.split(",")
    data.replace("<komma>", ",")
    content["idx"].append(idx)
    content["data"].append(data[1:-1])
    if category not in content["target_names"]:
      content["target_names"].append(category)
    content["target"].append(category_to_idx(category))



vectorizer = TfidfVectorizer(sublinear_tf=True, max_df=0.5, stop_words='english')

X_train = vectorizer.fit_transform(content["data"])
y_train = np.array(content["target"])

X_test = vectorizer.transform(content["data"])
y_test = np.array(content["target"])

clf = MultinomialNB(alpha=.01)

t0 = time()
clf.fit(X_train, y_train)
train_time = time() - t0
print("train time: %0.13fs" % train_time)

t0 = time()
pred = clf.predict(X_test)
test_time = time() - t0
print("test time:  %0.13fs" % test_time)


score = metrics.f1_score(y_test, pred)
print("f1-score:   %0.13f" % score)

print metrics.confusion_matrix(y_test, pred)