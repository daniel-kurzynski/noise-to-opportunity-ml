import sys, json

class Merger:

	def __init__(self):
		self.classification={"posts":[]}
		self.conflicts={"posts":[]}

	def load_classification(self):
		with open('data/classification.json') as infile:
			self.classification = json.load(infile)

	def save_classification(self):
		with open('data/classification.json', 'w') as outfile:
			json.dump(self.classification, outfile)

	def load_conflicts(self):
		with open('data/conflicts.json') as infile:
			self.conflicts = json.load(infile)

	def save_conflicts(self):
		with open('data/conflicts.json', 'w') as outfile:
			json.dump(self.conflicts, outfile)

	def merge(self, filename):
		with open(filename) as classification_file:
			another_classification = json.load(classification_file)
			for each in another_classification:
				if each not in self.classification:
					self.classification[each] = another_classification[each]
				else:
					if self.classification[each]['demand'] != another_classification[each]['demand']:
						self.conflicts[each]='demand'
						continue
					if 'category' in self.classification[each] and self.classification[each]['category'] != another_classification[each]['category']:
						self.conflicts[each]='category'
						continue




if __name__ == "__main__":
	merger = Merger()

	merger.load_classification()
	merger.load_conflicts();

	for filename in sys.argv[1:]:
		merger.merge(filename)

	merger.save_classification()
	merger.save_conflicts();
