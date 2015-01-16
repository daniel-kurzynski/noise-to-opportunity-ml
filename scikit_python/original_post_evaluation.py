from bag_of_words import get_labeled_posts
from preprocessing import Data, CSVReader
from collections import defaultdict


our_labeled_posts = get_labeled_posts("category")

def linked_in_extractor(line):
	line = reduce(
		lambda l, replacements: l.replace(replacements[0], replacements[1]),
		CSVReader.replacements,
		line)
	idx, title, text, _, _, _, _, _, category, _, _ = line.split(",")
	title, text = title.replace("<komma>", ""), text.replace("<komma>", "")
	return Data(idx, title, text, category)

with open("../n2o_data/linked_in_posts.csv") as f:
	their_labeled_posts = [linked_in_extractor(line) for line in f]

sort_key = lambda data: data.id

our_ids = map(lambda data: data.id, our_labeled_posts)
their_labeled_posts = map(lambda data: data.get_class(""), sorted([data for data in their_labeled_posts if data.id in our_ids], key = sort_key))
our_labeled_posts = map(lambda data: data.get_class("category"), sorted(our_labeled_posts, key = sort_key))

cumulated = zip(their_labeled_posts, our_labeled_posts)
true_positives = [val[0] for val in map(lambda tuple: (tuple[0], tuple[0] == tuple[1]), cumulated) if val[1]]

counts = defaultdict(int)
all_counts = defaultdict(int)

for label in our_labeled_posts:
	all_counts[label] += 1

for tp in true_positives:
	counts[tp] += 1


for key in ["LVM", "ECOM", "CRM", "HCM", "None"]:
	print "\t".join([key, str(counts[key]), str(all_counts[key]), str(float(counts[key]) / all_counts[key])])

