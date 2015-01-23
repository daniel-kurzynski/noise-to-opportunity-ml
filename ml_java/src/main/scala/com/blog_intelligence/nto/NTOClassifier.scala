package com.blog_intelligence.nto

import de.hpi.smm.FeatureExtractorBuilder
import de.hpi.smm.classification.{Classifier, NTOAnalyzer}
import scala.collection.JavaConverters._

class NTOClassifier {

	var demandClassifier: Classifier = null

	def trainDemand(trainingSamples: java.util.List[Document]): Unit = {
		val featureExtraction = new FeatureExtractorBuilder(null).buildForDemand(trainingSamples)
		demandClassifier = new Classifier("demand",
			trainingSamples.asScala,
			featureExtraction)
	}

	def trainProduct(trainingSamples: java.util.List[Document]): Unit = {
		println("I am doing nothing.")
	}

	def predictDemand(doc: Document): Double = {
		if (demandClassifier == null)
			throw new Exception("Need to train the classifier first.")
		demandClassifier.classProbability(doc.wholeText).prob

	}

}
