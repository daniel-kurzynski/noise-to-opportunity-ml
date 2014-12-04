import collections

class Data(object):
	def __init__(self, idx, title = "", text = "", classification = None):
		self.id = idx
		self.title = title
		self.text = text
		self.data = title + " " + text if title else text
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


class CSVReader(object):

	replacements = [
		["<br />", ""],
		["\\,", "<komma>"],
		["\"", ""],
		["\\", ""],
	]


	def __init__(self, classification = {}):
		super(CSVReader, self).__init__()
		self.data = []
		self.classification = classification

	"""
	Reading CSV files in our fashion.
	"""
	def read(self, fpath, extractor):
		with open(fpath) as f:
			self.data = [extractor(line, self.classification) for line in f]

	@staticmethod
	def brochure_extractor(line):
		_, data, category, _ = line.replace("\\,", "<komma>").split(",")
		return data.replace("<komma>", ",")[1:-1], category


	@staticmethod
	def linked_in_extractor(line, classification):
		line = reduce(
			lambda l, replacements: l.replace(replacements[0], replacements[1]),
			CSVReader.replacements,
			line)
		idx, title, text, _, _, _, _, _, category, _, _ = line.split(",")
		title, text = title.replace("<komma>", ","), text.replace("<komma>", ",")
		return Data(idx, title, text, classification.get(idx))

