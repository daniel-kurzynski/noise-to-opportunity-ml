
from sklearn.feature_extraction.text import TfidfVectorizer
import simplejson as json, numpy as np, collections


class Post(object):
	def __init__(self, id, title, text, classification = None):
		self.id = id
		self.title = title
		self.text = text
		self.data = title + " " + text
		self.classification = classification

	def is_labeled(self):
		return self.classification is not None

	def get_class(self):
		votes = self.classification["demand"]
		freqs = collections.Counter(votes.values()).most_common(2)
		if len(freqs) > 1 and freqs[0][1] == freqs[1][1]:
			# First two votes have the same count --> conflict --> do not predict anything
			return None
		else:
			return freqs[0][0]

def build_data():
	print "=== Bag of Words Extractor ==="
	with open('../webapp_python/data/classification.json') as infile:
		classification = json.JSONDecoder(object_pairs_hook=collections.OrderedDict).decode(infile.read())

	posts = []
	with open("../n2o_data/linked_in_posts.csv") as f:
		for line in f:
			line = line.replace("<br />", "")
			id, title, text, _, _, _, _, _, category, _, _ = line.replace("\\,", "<komma>").replace("\"", "").replace("\\", "").split(",")
			title = title.replace("<komma>", ",")
			text = text.replace("<komma>", ",")
			posts.append(Post(id, title, text, classification.get(id)))

	labeled_posts = [post
			for post in posts
				if post.is_labeled() and post.get_class() != "no-idea"]

	# Build vectorizer
	vectorizer = TfidfVectorizer(sublinear_tf = True, max_df = 0.5, stop_words = 'english')
	X_train   = vectorizer.fit_transform([post.data for post in labeled_posts])
	y_train = np.array([post.get_class() for post in labeled_posts])

	return X_train, y_train, vectorizer
