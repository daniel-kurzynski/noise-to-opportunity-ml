import simplejson as json
import numpy as np
import collections, sys
from os.path import join, abspath
from post import Post, Prediction
from collections import Counter
from math import exp

sys.path.append("../scikit_python")
from evaluation import Classifiers
from custom_features import build_demand_data
from constants import LINKED_IN_POSTS, CLASSIFICATION

ids, X_train, y_train, _, predict_ids, X_predict = build_demand_data(False)

class active_learner(object):
	def __init__(self):
		self.classification = collections.OrderedDict()
		self.posts = []
		self.load_posts()
		self.load_classification()

	def load_posts(self):
		"""Called once in the initialization to load all posts."""
		with open(LINKED_IN_POSTS) as f:
			for line in f:
				id, title, text, _, _, _, _, _, category, _, _ = line.replace("\\,", "").replace("\"", "").replace("\\", "").split(",")
				self.posts.append(Post(id, title, text))

	def load_classification(self):
		"""Called once in the initialization to load the already existing classification file."""
		with open(CLASSIFICATION) as infile:
			self.classification = json.JSONDecoder(object_pairs_hook=collections.OrderedDict).decode(infile.read())

	def save_classification(self):
		"""Writes the classification back to disk."""
		with open(CLASSIFICATION, 'w') as outfile:
			json.dump(self.classification, outfile, indent = 2)

	def tag_post(self, tagger, post_id, key, value):
		"""Tag a post with respect some key (e.g. 'demand' or 'category')."""
		if not post_id in self.classification:
			self.classification[post_id] = {}
		if not key in self.classification[post_id]:
			self.classification[post_id][key] = {}

		self.classification[post_id][key][tagger] = value
		self.save_classification()

	def not_enough_posts_tagged(self):
		"""Checks whether there exists at least one example of each class."""
		numberOfDemandPosts = sum([1 if self.determine_class_from_conflicting_votes(post_id, "demand") == "demand" else 0 for post_id in self.classification])
		numberOfNoDemandPosts = len(self.classification) - numberOfDemandPosts
		return not (numberOfDemandPosts > 0 and numberOfNoDemandPosts > 0)

	def determine_class_from_conflicting_votes(self, post_id, key):
		"""For a given post and key, this determines whether a class can be determined or whether there is a conflict (None)."""
		votes = self.classification.get(post_id,{}).get(key,{})
		freqs = Counter(votes.values()).most_common(2)
		if (len(freqs) == 0) or (len(freqs) > 1 and freqs[0][1] == freqs[1][1]):
			# First two votes have the same count --> conflict --> do not predict anything
			return None
		else:
			return freqs[0][0]

	def post(self, post_id):
		"""Returns one post."""
		posts = [post for post in self.posts if post.id == post_id]
		return posts

	def build_classifier(self):
		global X_predict, predict_ids
		# TODO: Unlabeled posts is reavaluated on each access, while X_predict is only calculated once at
		# startup for performance reasons. They should be kept in sync by removing the irrelevant entries
		# from X_predict
		unlabeled_posts = [post
			for post in self.posts
				if not (post.id in self.classification
					and self.classification[post.id]['demand'])]

		relevant_ids = set(map(lambda x: x.id, unlabeled_posts))

		new_X_predict = []
		new_predict_ids = []
		for index, x in enumerate(X_predict):
			if predict_ids[index] in relevant_ids:
				new_X_predict.append(x)
				new_predict_ids.append(predict_ids[index])
		X_predict = new_X_predict
		predict_ids = new_predict_ids

		# Train the classifier
		classifier = Classifiers.CLASSIFIERS[Classifiers.BERNOULLI_NB]
		print classifier.__class__.__name__
		classifier.fit(X_train, y_train)
		return classifier, X_predict, unlabeled_posts


	def predicted_posts(self, type):
		if self.not_enough_posts_tagged():
			print "Choosing random posts"
			return [Post.fromPost(post) for post in np.random.choice(self.posts, 5, False)]

		print "Choosing " + type + " posts"

		classifier, X_predict, unlabeled_posts = self.build_classifier()

		assert(len(X_predict) == len(unlabeled_posts))
		# Old predictions, where we have a confidence
		# predictions = self.calculate_predictions(classifier, X_predict)
		# As we are using BernoulliNB basically, we do not have prediction confidences anymore
		predictions = self.calculate_predictions_nb(classifier, X_predict)

		confidence_predictions = sorted(predictions, key = lambda prediction: prediction.confidence)

		if type == "uncertain":
			confidence_predictions = confidence_predictions[:10]
		elif type == "certain":
			confidence_predictions = confidence_predictions[-25:]
		else:
			raise Exception("Unknown type requested, must be either 'certain' or 'uncertain'.")

		# print len(unlabeled_posts)

		predicted_posts	 = []
		for prediction in confidence_predictions:
			# print prediction.index
			predicted_posts.append(Post.fromPost(unlabeled_posts[prediction.index], prediction=prediction))

		return predicted_posts

	def determine_tagged_posts(self, tagger = None):
		tagged_posts = [
			Post.fromPost(
				post,
				demand_votes=self.classification[post.id].get("demand"),
				category_votes=self.classification[post.id].get("category"),
				demand=self.determine_class_from_conflicting_votes(post.id,"demand"),
				category=self.determine_class_from_conflicting_votes(post.id,"category")
			)
			for post in self.posts
			if post.id in self.classification and not (tagger and tagger in self.classification[post.id].get("demand", {}))
		]
		return tagged_posts

	def determine_conflicted_posts(self):
		conflicting_posts = []

		for post in self.posts:
			if not post.id in self.classification:
				continue
			classification = self.classification[post.id]
			if not "demand" in classification:
				continue
			if not "category" in classification:
				continue

			demand_votes   = classification["demand"]
			category_votes = classification["category"]
			if (self.determine_class_from_conflicting_votes(post.id, "demand") is not None and
				self.determine_class_from_conflicting_votes(post.id, "category") is not None):
				continue

			# we have a conflict!
			post.demand_votes   = demand_votes
			post.category_votes = category_votes
			conflicting_posts.append(post)
		return conflicting_posts

	def calculate_predictions(self, classifier, data):
		confidences = np.abs(classifier.decision_function(data))
		# The following line first sorts the confidences, and then extracts the predictions from these orders.
		# The index for the highest confidence is in the last position.
		# We then build Prediction objects for these.
		predictions = [Prediction(classifier.classes_[confOrders[-1]], confidences[index][confOrders[-1]], index) for index, confOrders in enumerate(np.argsort(confidences))]
		return predictions

	def calculate_predictions_nb(self, classifier, X_predict):
		predictions = []
		for index, x in enumerate(X_predict):
			predictedClass = classifier.predict(x)
			class_probs = map(lambda x: exp(x), classifier.predict_log_proba(x)[0])
			# if index == 4: # print some example values
			# 	print class_probs
			# 	print max(class_probs)
			# 	print sum(class_probs)
			prob = max(class_probs) / sum(class_probs)
			predictions.append(Prediction(predictedClass, prob, index))
		return predictions


