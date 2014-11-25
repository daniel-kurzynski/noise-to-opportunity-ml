import sys
import json

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

	def merge(self, filename):
		with open(filename) as classification_file:    
			another_classification = json.load(classification_file)
			for post_id in another_classification:
				if post_id not in self.classification:
					self.classification[post_id] = another_classification[post_id]
				else:
					if self.classification[post_id]['demand'] != another_classification[post_id]['demand']:
						self.conflicts[post_id]='demand'
						continue
					if 'category' in self.classification[post_id] and self.classification[post_id]['category'] != another_classification[post_id]['category']:
						self.conflicts[post_id]='category'
						continue
					



if __name__ == "__main__":
	merger = Merger()

	merger.load_classification()
	merger.load_conflicts();

	for filename in sys.argv[1:]:
		merger.merge(filename)

	merger.save_classification()
	merger.save_conflicts();
