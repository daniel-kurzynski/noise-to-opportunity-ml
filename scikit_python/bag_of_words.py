from sklearn.feature_extraction.text import TfidfVectorizer
import simplejson as json, numpy as np, collections

from preprocessing import CSVReader

def build_demand_data():
	print "=== Bag of Words Extractor ==="
	with open('../webapp_python/data/classification.json') as infile:
		classification = json.JSONDecoder(object_pairs_hook=collections.OrderedDict).decode(infile.read())


	csv_reader = CSVReader(classification = classification)
	csv_reader.read("../n2o_data/linked_in_posts.csv", CSVReader.linked_in_extractor)

	labeled_posts = [post
			for post in csv_reader.data
				if post.is_labeled() and post.get_class() != "no-idea"]

	# Build vectorizer
	vectorizer = TfidfVectorizer(sublinear_tf = True, max_df = 0.5, stop_words = 'english')
	X_train   = vectorizer.fit_transform([post.data for post in labeled_posts])
	y_train = np.array([post.get_class() for post in labeled_posts])

	return X_train, y_train, vectorizer


def build_product_data():
	csv_reader = CSVReader()
	csv_reader.read("../n2o_data/brochures.csv", CSVReader.brochure_extractor)
	return None, None, None, None
