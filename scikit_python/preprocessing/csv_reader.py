import collections


def _binarize_class(value, current_class):
	if current_class in [None, value]:
		return value
	else:
		return "no-%s" %current_class

class Data(object):
	def __init__(self, idx, title = "", text = "", classification = None):
		self.id = idx
		self.title = title
		self.text = text
		self.data = title + " " + text if title else text
		self.classification = classification

	def is_labeled(self):
		return self.classification is not None

	def get_class(self, key, product_class = None):
		if isinstance(self.classification, str):
			return _binarize_class(self.classification, product_class)
		votes = self.classification[key]
		freqs = collections.Counter(votes.values()).most_common(2)
		if len(freqs) > 1 and freqs[0][1] == freqs[1][1]:
			# First two votes have the same count --> conflict --> do not predict anything
			raise Exception("we have a conflict at doc %s: %s!" %(self.id, str(self.classification)))
			return None
		else:
			return _binarize_class(freqs[0][0], product_class)


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
		self.data = []
		with open(fpath) as f:
			for line in f:
				data_obj = extractor(line, self.classification)
				if data_obj is not None: self.data.append(data_obj)

	@staticmethod
	def brochure_extractor(line, classification):
		line = reduce(
			lambda l, replacements: l.replace(replacements[0], replacements[1]),
			CSVReader.replacements,
			line)
		idx, data, category, _, lang = line.strip().split(",")
		if lang == "de": return None
		return Data(idx, text = data.replace("<komma>", ","), classification = category)


	@staticmethod
	def linked_in_extractor(line, classification):
		line = reduce(
			lambda l, replacements: l.replace(replacements[0], replacements[1]),
			CSVReader.replacements,
			line)
		idx, title, text, _, _, _, _, _, category, _, _ = line.split(",")
		title, text = title.replace("<komma>", ","), text.replace("<komma>", ",")
		return Data(idx, title, text, classification.get(idx))

