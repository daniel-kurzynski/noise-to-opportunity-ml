package de.hpi.smm.app

import java.io.File

import de.hpi.smm.Constants._
import de.hpi.smm.classification.ExtendedNTOClassifierBuilder

object ClassificationPreparation {

	println("Start preparing post prediction")
	val classificationFile = new File(CLASSIFICATION_JSON)
	val postsFile = new File("../n2o_data/linked_in_posts.csv")
	val brochuresFile = new File("../n2o_data/brochures.csv")
	val stopWordsFile = new File(STOPWORDS_PATH)
	val posModelFile = new File(POSMODEL_PATH)

	val ntoClassifier = ExtendedNTOClassifierBuilder.build(classificationFile, brochuresFile, postsFile, stopWordsFile, posModelFile)

	println("Finished preparing post prediction")

}
