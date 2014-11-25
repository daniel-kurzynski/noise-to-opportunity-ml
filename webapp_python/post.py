class Post(object):
	def __init__(self, id, title, text, demand_votes = {}, category_votes = {}):
		self.id = id
		self.title = title
		self.text = text
		self.data = title + " " + text
		self.demand_votes = demand_votes
		self.category_votes = category_votes

class Prediction(object):
	def __init__(self, category = "[Not classified]", conf = -10.0, index = -1):
		self.category = category
		self.conf = conf
		self.index = index

	def __repr__(self):
		return str(self)

	def __str__(self):
		return "Prediction(" + self.category + ", " + str(self.conf) + ")"
