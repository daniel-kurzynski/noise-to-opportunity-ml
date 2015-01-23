package com.blog_intelligence.nto

import de.hpi.smm.FeatureExtractorBuilder
import de.hpi.smm.classification.NTOAnalyzer

class NTOClassifier {

	def trainDemand(trainingSamples: java.util.List[Document]): Unit = {
		val featureExtraction = new FeatureExtractorBuilder(null).buildForDemand(trainingSamples)
//		val nto = new NTOAnalyzer(featureExtraction)
		println("I am doing nothing.")
	}

	def trainProduct(trainingSamples: java.util.List[Document]): Unit = {
		println("I am doing nothing.")
	}

	def predictDemand(doc: Document): Double = {
		1.0
	}

}
