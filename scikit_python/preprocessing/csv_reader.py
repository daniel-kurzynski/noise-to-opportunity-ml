from os.path import join, abspath

class CSVReader(object):

	def __init__(self):
		super(CSVReader, self).__init__()
		self.data = []
		self.target = []
		self.target_mapping = {}

	"""
	Reading CSV files in our fashion.
	"""
	def read(self, filename, extractor):
		with open(join(abspath("../data"), filename)) as f:
			for line in f:
				data, category = extractor(line)
				if category not in self.target_mapping:
					self.target_mapping[category] = len(self.target_mapping)
				self.data.append(data)
				self.target.append(self.target_mapping[category])

	@staticmethod
	def brochure_extractor(line):
		_, data, category, _ = line.replace("\\,", "<komma>").split(",")
		return data.replace("<komma>", ",")[1:-1], category


	@staticmethod
	def linked_in_extractor(line):
		_, data1, data2, _, _, _, _, _, category, _, _ = line.replace("\\,", "<komma>").split(",")
		data = data1[1:-1] + " " + data2[1:-1]
		return data.replace("<komma>", ","), category

