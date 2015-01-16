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

lvm_theirs          = filter(lambda tuple: tuple[0] == "LVM",  cumulated)
lvm_correct_theirs  = filter(lambda tuple: tuple[1] == "LVM",  lvm_theirs)
hcm_theirs          = filter(lambda tuple: tuple[0] == "HCM",  cumulated)
hcm_correct_theirs  = filter(lambda tuple: tuple[1] == "HCM",  hcm_theirs)
crm_theirs          = filter(lambda tuple: tuple[0] == "CRM",  cumulated)
crm_correct_theirs  = filter(lambda tuple: tuple[1] == "CRM",  crm_theirs)
ecom_theirs         = filter(lambda tuple: tuple[0] == "ECOM", cumulated)
ecom_correct_theirs  = filter(lambda tuple: tuple[1] == "ECOM",  ecom_theirs)

their_predict_counts = {
	"LVM":  float(len(lvm_correct_theirs)) / len(lvm_theirs),
	"HCM":  float(len(hcm_correct_theirs)) / len(hcm_theirs),
	"CRM":  float(len(crm_correct_theirs)) / len(crm_theirs),
	"ECOM": float(len(ecom_correct_theirs)) / len(ecom_theirs),
	"None": 0
}


counts = defaultdict(int)
all_counts = defaultdict(int)

for label in our_labeled_posts:
	all_counts[label] += 1

for tp in true_positives:
	counts[tp] += 1

print "Class\t\tCorrectPred\tActualDist\tRecall of Class\tPrecision of Class"
for key in ["LVM", "ECOM", "CRM", "HCM", "None"]:
	print "\t\t".join([key, str(counts[key]), str(all_counts[key]), str(round(float(counts[key]) * 100 / all_counts[key], 2)), str(round(their_predict_counts[key] * 100, 2))])

