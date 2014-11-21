from os.path import join, abspath
from post import Post
import simplejson as json
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import Perceptron
import numpy as np
import collections

class active_learner(object):
	def __init__(self):
		self.load_posts()
		self.load_classification()

	def load_posts(self):
		self.posts = []
		with open(join(abspath("../n2o_data"), "linked_in_posts.csv")) as f:
			for line in f:
				id, title, text, _, _, _, _, _, category, _, _ = line.replace("\\,", "<komma>").replace("\"", "").replace("\\", "").split(",")
				title = title.replace("<komma>", ",")
				text = text.replace("<komma>", ",")
				self.posts.append(Post(id,title,text))

	def load_other_classification_files(self):
		pass

	def load_classification(self):
		with open('data/classification.json') as infile:
			self.classification = json.JSONDecoder(object_pairs_hook=collections.OrderedDict).decode(infile.read())
			# self.classification = json.OrderedDict(json.load(infile))

	def save_classification(self):
		with open('data/classification.json', 'w') as outfile:
			json.dump(self.classification, outfile, indent = 2)

	def tag_demand(self, post_id, is_demand):
		self.classification[post_id] = {"demand": is_demand}
		self.save_classification()

	def tag_category(self, post_id, category):
		self.classification[post_id]["category"] = category
		self.save_classification()

	def not_enghouh_posts_tagged(self):
		numberOfDemandPosts = sum([1 if self.classification[each]["demand"]=="demand" else 0 for each in self.classification])
		numberOfNoDemandPosts = len(self.classification)-numberOfDemandPosts
		print numberOfDemandPosts, numberOfNoDemandPosts
		if numberOfDemandPosts>0 and numberOfNoDemandPosts>0:
			return False
		return True

	def post(self, post_id):
		return [(post, 0.0) for post in self.posts if post.id == post_id]

	def uncertainty_posts(self):

		if self.not_enghouh_posts_tagged():
			print "Choosing radom posts"
			return [(post, 0.0) for post in np.random.choice(self.posts,5,False)]

		print "Choosing uncertainty posts"
		labledPosts = [post for post in self.posts if post.id in self.classification and self.classification[post.id]['demand']]
		unlabledPosts = [post for post in self.posts if not (post.id in self.classification and self.classification[post.id]['demand'])]
		X_train = [post.data for post in labledPosts]
		X_predict = [post.data for post in unlabledPosts]
		y_train = [self.classification[post.id]['demand'] for post in labledPosts ]

		vectorizer = TfidfVectorizer(sublinear_tf=True, max_df=0.5, stop_words='english')
		X_train = vectorizer.fit_transform(X_train)
		X_predict = vectorizer.transform(X_predict)
		y_train = np.array(y_train)

		classifier = Perceptron(n_iter=50)

		classifier.fit(X_train,y_train)
		# y_predict = classifier.predict(X_predict)

		confidences = np.abs(classifier.decision_function(X_predict))
		print confidences

		sorted_confidences = np.argsort(confidences)

		confidence_indices = sorted_confidences[-10:]
		# confidence_indices.extend(sorted_confidences[:2])

		return [(unlabledPosts[i], confidences[i]) for i in confidence_indices]


