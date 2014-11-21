class Post(object):
	def __init__(self, id, title, text):
		self.id = id
		self.title = title
		self.text = text
		self.data = title + " " + text

class Prediction(object):
	def __init__(self, category = "[Not classified]", conf = -10.0, index = -1):
		self.category = category
		self.conf = conf
		self.index = index

	def __repr__(self):
		return str(self)

	def __str__(self):
		return "Prediction(" + self.category + ", " + str(self.conf) + ")"
