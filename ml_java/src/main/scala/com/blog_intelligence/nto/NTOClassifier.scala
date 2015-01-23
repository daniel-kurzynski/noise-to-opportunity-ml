package com.blog_intelligence.nto

import de.hpi.smm.FeatureExtractorBuilder
import de.hpi.smm.classification.{ProductClassifier, Classifier}
import scala.collection.JavaConverters._

class NTOClassifier {

	var demandClassifier: Classifier = null
	var productClassifiers: List[ProductClassifier] = null

	def trainDemand(trainingSamples: java.util.List[Document]): Unit = {
		val featureExtraction = new FeatureExtractorBuilder(null).buildForDemand(trainingSamples)
		demandClassifier = new Classifier("demand",
			trainingSamples.asScala,
			featureExtraction)
	}

	def trainProduct(trainingSamples: java.util.List[Document]): Unit = {
		val productClasses = List("CRM", "HCM", "ECOM", "LVM")
		productClassifiers = productClasses.map { clazz =>
			new ProductClassifier(clazz,
				trainingSamples.asScala)
		}
	}

	def predictDemand(text: String): Double = {
		if (demandClassifier == null)
			throw new Exception("Need to train the classifier first.")
		demandClassifier.classProbability(text).prob
	}

	def predictProduct(text: String): java.util.List[Classification] = {
		if (productClassifiers == null)
			throw new Exception("Need to train the classifier first.")
		productClassifiers.map { classifier =>
			val prob = classifier.classProbability(text).prob
			Classification(classifier.className, prob)
		}.sortBy(-_.prob).asJava
	}

	case class Classification(product: String, prob: Double)

}
