class Post(object):
	def __init__(self, id, title, text):
		self.id = id
		self.title = title
		self.text= text
		self.data = title + " " + text

