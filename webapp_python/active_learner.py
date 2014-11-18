from os.path import join, abspath
from post import Post
import json
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import Perceptron
import numpy as np

class active_learner(object):
	def __init__(self):
		self.load_posts()
		self.load_classification()

	def load_posts(self):
		self.posts = []
		with open(join(abspath("../data"), "linked_in_posts.csv")) as f:
			for line in f:
				id, title, text, _, _, _, _, _, category, _, _ = line.replace("\\,", "<komma>").replace("\"", "").replace("\\", "").split(",")
				title = title.replace("<komma>", ",")
				text = text.replace("<komma>", ",")
				self.posts.append(Post(id,title,text))

	def load_other_classification_files(self):
		pass

	def load_classification(self):
		with open('data/classification.json') as infile:    
			self.classification = json.load(infile)
			
	def save_classification(self):
		with open('data/classification.json', 'w') as outfile:
			json.dump(self.classification, outfile)

	def tag_demand(self, post_id, is_demand):
		self.classification[post_id]={"demand":is_demand}
		self.save_classification()

	def not_enghouh_posts_tagged(self):
		numberOfDemandPosts = sum([1 if self.classification[each]["demand"] else 0 for each in self.classification])
		numberOfNoDemandPosts = len(self.classification)-numberOfDemandPosts;
		if numberOfDemandPosts>0 and numberOfNoDemandPosts>0:
			return False
		return True

	def post(self, post_id):
		return [post for post in self.posts if post.id == post_id]

	def uncertainty_posts(self):

		if self.not_enghouh_posts_tagged():
			print "Choosing radom posts"
			return np.random.choice(self.posts,15,False)

		print "Choosing uncertainty posts"
		X = [post.data for post in self.posts]
		y = [-1 if not post.id in self.classification else 1 if self.classification[post.id][demand] else 0 for post in self.posts]

		vectorizer = TfidfVectorizer(sublinear_tf=True, max_df=0.5, stop_words='english')
		X = vectorizer.fit_transform(X)
		y = np.array(y)
		classifier = Perceptron(n_iter=50)

		classifier.fit(X,y)

		pred_entropies = stats.distributions.entropy(classifier.label_distributions_.T)

		uncertainty_index = np.argsort(pred_entropies)[-15:]

		print self.posts[uncertainty_index]


