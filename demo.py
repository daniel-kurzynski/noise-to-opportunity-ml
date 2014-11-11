from os.path import dirname, join
from sklearn.feature_extraction.text import TfidfVectorizer
import numpy as np
from time import time
from sklearn import metrics
from sklearn.naive_bayes import MultinomialNB



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


print "Brochures"
print "="*50
process("data/brochures.csv", brochure_exractor)
print "="*50


print "LinkedIn Posts"
print "="*50
process("data/linked_in_posts.csv", linked_in_extractor)
print "="*50
