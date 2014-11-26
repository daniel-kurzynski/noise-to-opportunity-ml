class Prediction(object):
	def __init__(self, category = "[Not classified]", confidence = -10.0, index = -1):
		self.category = category
		self.confidence = confidence
		self.index = index

	def __repr__(self):
		return str(self)

	def __str__(self):
		return "Prediction(" + self.category + ", " + str(self.confidence) + ")"

class Post(object):
	@staticmethod
	def fromPost(post, demand_votes = None, category_votes = None, prediction = None):
		return Post(post.id, post.title, post.text, demand_votes, category_votes, prediction);

	def __init__(self, id, title, text, demand_votes = None, category_votes = None, prediction = None):
		self.id = id
		self.title = title
		self.text = text
		self.data = title + " " + text
		self.demand_votes = demand_votes
		self.category_votes = category_votes
		self.prediction = prediction
