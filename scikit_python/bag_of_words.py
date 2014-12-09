from sklearn.feature_extraction.text import TfidfVectorizer
import simplejson as json, numpy as np, collections

from preprocessing import CSVReader

def get_labeled_posts(key, exclude):
	with open('../webapp_python/data/classification.json') as infile:
		classification = json.JSONDecoder(object_pairs_hook=collections.OrderedDict).decode(infile.read())

	csv_reader = CSVReader(classification = classification)
	csv_reader.read("../n2o_data/linked_in_posts.csv", CSVReader.linked_in_extractor)

	return [post
			for post in csv_reader.data
				if post.is_labeled() and post.get_class(key) != exclude]

def get_unlabeled_posts(key):
	with open('../webapp_python/data/classification.json') as infile:
		classification = json.JSONDecoder(object_pairs_hook=collections.OrderedDict).decode(infile.read())

	csv_reader = CSVReader(classification = classification)
	csv_reader.read("../n2o_data/linked_in_posts.csv", CSVReader.linked_in_extractor)

	return [post
			for post in csv_reader.data
				if not post.is_labeled()]


def build_demand_data():
	print "=== Bag of Words Extractor ==="
	labeled_posts = get_labeled_posts("demand", "no-idea")
	unlabeled_posts = get_unlabeled_posts("demand")
	# Build vectorizer
	vectorizer = TfidfVectorizer(sublinear_tf = True, max_df = 0.5, stop_words = 'english')
	X   = vectorizer.fit_transform([post.data for post in labeled_posts])
	y = np.array([post.get_class("demand") for post in labeled_posts])
	X_unlabeled = vectorizer.transform([post.data for post in unlabeled_posts])

	return [], X, y, vectorizer, X_unlabeled


def build_product_data():
	print "=== Bag of Words Extractor ==="
	csv_reader = CSVReader()
	csv_reader.read("../n2o_data/brochures.csv", CSVReader.brochure_extractor)

	vectorizer = TfidfVectorizer(sublinear_tf = True, max_df = 0.5, stop_words = 'english')
	X_train   = vectorizer.fit_transform([brochure.data for brochure in csv_reader.data])
	y_train = np.array([brochure.get_class("category") for brochure in csv_reader.data])

	with open('../webapp_python/data/classification.json') as infile:
		classification = json.JSONDecoder(object_pairs_hook=collections.OrderedDict).decode(infile.read())

	labeled_posts = get_labeled_posts("category", "None")

	X_test   = vectorizer.transform([post.data for post in labeled_posts])
	y_true = np.array([post.get_class("category") for post in labeled_posts])
	categories = np.unique(y_train)
	return X_train, y_train, X_test, y_true, categories
