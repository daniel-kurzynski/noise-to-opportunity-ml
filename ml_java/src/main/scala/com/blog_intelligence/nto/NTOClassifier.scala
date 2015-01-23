package com.blog_intelligence.nto

import de.hpi.smm.FeatureExtractorBuilder
import de.hpi.smm.classification.{ProductClassifier, Classifier}
import scala.collection.JavaConverters._

class NTOClassifier {

	var demandClassifier: Classifier = null
	var productClassifiers: List[Classifier] = null

	def trainDemand(trainingSamples: java.util.List[Document]): Unit = {
		val featureExtraction = new FeatureExtractorBuilder(null).buildForDemand(trainingSamples)
		demandClassifier = new Classifier("demand",
			trainingSamples.asScala,
			featureExtraction)
	}

	def trainProduct(trainingSamples: java.util.List[Document]): Unit = {
		val productClasses = List("CRM", "HCM", "ECOM", "LVM")
		productClasses.map { clazz =>
			new ProductClassifier(clazz,
				trainingSamples.asScala)
		}
		println("I am doing nothing.")
	}

	def predictDemand(doc: Document): Double = {
		if (demandClassifier == null)
			throw new Exception("Need to train the classifier first.")
		demandClassifier.classProbability(doc.wholeText).prob
	}

	def predictProduct(text: String): List[Classification] = {
		if (productClassifiers == null)
			throw new Exception("Need to train the classifier first.")
		productClassifiers.map { classifier =>
			val prob = classifier.classProbability(text).prob
			Classification(classifier.className, prob)
		}.sortBy(-_.prob)
	}

	case class Classification(product: String, prob: Double)

}
