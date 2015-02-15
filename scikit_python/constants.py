from os.path import join, abspath

_DATA_FOLDER = abspath("../n2o_data")

LINKED_IN_POSTS = join(_DATA_FOLDER, "linked_in_posts.csv")
CLASSIFICATION = join(_DATA_FOLDER, "latest.json")

DEMAND_FEATURES = join(_DATA_FOLDER, "features.csv")
PRODUCT_FEATURES = lambda product_class, prefix: join(
	_DATA_FOLDER,
	"features_{prefix}{cls}.csv".format({
		"prefix": prefix,
		"cls": product_class.lower()
	}))
