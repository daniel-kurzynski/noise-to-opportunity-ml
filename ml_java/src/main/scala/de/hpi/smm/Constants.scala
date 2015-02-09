package de.hpi.smm

import java.io.File


object Constants {
	val CLASSIFICATION_JSON = new File("../n2o_data/classification_updates/latest.json").getAbsolutePath;
	val BROCHURES_PATH = new File("../n2o_data/brochures.csv").getAbsolutePath
	val POSTS_PATH = new File("../n2o_data/linked_in_posts.csv").getAbsolutePath
	val STOPWORDS_PATH = new File("../n2o_data/stopwords.txt").getAbsolutePath
	val POSMODEL_PATH = new File("../n2o_data/german-fast.tagger").getAbsolutePath
}
