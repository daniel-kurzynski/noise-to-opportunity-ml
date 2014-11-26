from os.path import join, abspath
from post import Post, Prediction
import simplejson as json
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import Perceptron
import numpy as np
import collections
from collections import Counter


class active_learner(object):

	def __init__(self):
		self.classification = collections.OrderedDict()
		self.load_posts()
		self.load_classification()
		self.tagger_name = open("tagger_name.conf").read().strip()

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

	def save_classification(self):
		with open('data/classification.json', 'w') as outfile:
			json.dump(self.classification, outfile, indent = 2)

	def tag_post(self, post_id, key, value):
		if not post_id in self.classification:
			self.classification[post_id] = {}
		if not key in self.classification[post_id]:
			self.classification[post_id][key] = {}

		self.classification[post_id][key][self.tagger_name] = value

		self.save_classification()

	def not_enough_posts_tagged(self):
		numberOfDemandPosts = sum([1 if self.determine_class_from_conflicting_votes(post_id, "demand") == "demand" else 0 for post_id in self.classification])
		numberOfNoDemandPosts = len(self.classification) - numberOfDemandPosts
		return not (numberOfDemandPosts > 0 and numberOfNoDemandPosts > 0)

	def determine_class_from_conflicting_votes(self, post_id, key):
		votes = self.classification[post_id][key]
		freqs = Counter(votes.values()).most_common(2)
		if len(freqs) > 1 and freqs[0][1] == freqs[1][1]:
			# First two votes have the same count --> conflict --> do not predict anything
			return None
		else:
			return freqs[0][0]

	def post(self, post_id):
		posts = [post for post in self.posts if post.id == post_id]
		classifier, data, _ = self.build_classifier(posts)
		return [Post.fromPost(posts[prediction.index],prediction=prediction) for prediction in self.calculate_predictions(classifier, data)]

	def build_classifier(self, unlabeled_posts = None):
		labeled_posts  =  [post for post in self.posts if post.id in self.classification and self.determine_class_from_conflicting_votes(post.id, "demand") is not None]
		if unlabeled_posts is None:
			unlabeled_posts = [post for post in self.posts if not (post.id in self.classification and self.classification[post.id]['demand'])]
		X_train   = [post.data for post in labeled_posts]
		X_predict = [post.data for post in unlabeled_posts]
		Y_train = [self.determine_class_from_conflicting_votes(post.id, 'demand') for post in labeled_posts]

		# Build vectorizer
		vectorizer = TfidfVectorizer(sublinear_tf = True, max_df = 0.5, stop_words = 'english')
		X_train   = vectorizer.fit_transform(X_train)
		X_predict = vectorizer.transform(X_predict)
		Y_train   = np.array(Y_train)

		# Train the classifier
		classifier = Perceptron(n_iter = 50)
		classifier.fit(X_train, Y_train)
		return classifier, X_predict, unlabeled_posts


	def predicted_posts(self, uncertain=True):
		if self.not_enough_posts_tagged():
			print "Choosing random posts"
			return [(post, Prediction()) for post in np.random.choice(self.posts, 5, False)],[]

		print "Choosing uncertain posts"

		classifier, X_predict, unlabeled_posts = self.build_classifier()
		predictions = self.calculate_predictions(classifier, X_predict)

		confidence_predictions = sorted(predictions, key = lambda prediction: prediction.confidence)
		if(uncertain):
			confidence_predictions = confidence_predictions[:10]
		else:
			confidence_predictions = confidence_predictions[-25:]

		print confidence_predictions
		return [Post.fromPost(unlabeled_posts[prediction.index],prediction=prediction) for prediction in confidence_predictions]

	def determin_certain_posts(self):
		classifier, X_predict, unlabeled_posts = self.build_classifier()
		predictions = self.calculate_predictions(classifier, X_predict)

		low_confidence_predictions = sorted(predictions, key = lambda prediction: prediction.confidence)
		low_confidence_predictions = low_confidence_predictions[:10]

		return [Post.fromPost(unlabeled_posts[prediction.index],prediction=prediction) for prediction in low_confidence_predictions]

	def determine_tagged_posts(self, withoutMine = True):
		tagged_posts = [Post.fromPost(post, demand_votes=self.classification[post.id].get("demand"), category_votes=self.classification[post.id].get("category")) for post in self.posts if post.id in self.classification and not (withoutMine and self.tagger_name in self.classification[post.id].get("demand", {}))]
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
			if (len(set(demand_votes.values())) <= 1 and
				len(set(category_votes.values())) <= 1):
				continue

			# we have a conflict!
			post.demand_votes   = json.dumps(demand_votes)
			post.category_votes = json.dumps(category_votes)
			conflicting_posts.append(post)
		return conflicting_posts

	def calculate_predictions(self, classifier, data):
		confidences = np.abs(classifier.decision_function(data))
		# The following line first sorts the confidences, and then extracts the predictions from these orders.
		# The index for the highest confidence is in the last position.
		# We then build Prediction objects for these.
		predictions = [Prediction(classifier.classes_[confOrders[-1]], confidences[index][confOrders[-1]], index) for index, confOrders in enumerate(np.argsort(confidences))]
		return predictions


