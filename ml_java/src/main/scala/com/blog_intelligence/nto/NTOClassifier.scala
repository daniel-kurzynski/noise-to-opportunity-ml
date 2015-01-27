package com.blog_intelligence.nto

import java.io._

import de.hpi.smm.FeatureExtractorBuilder
import de.hpi.smm.classification.DemandClassifier
import de.hpi.smm.classification.old_classifier.ProductClassifier
import scala.collection.JavaConverters._

class NTOClassifier {

	var demandClassifier: DemandClassifier = null
	var productClassifiers: List[ProductClassifier] = null

	def requireNonNull(o: Object) {
		if (o == null)
			throw new Exception("Need to train the classifier first.")
	}

	def persistDemand(modelFile: File): Unit  = {
		persist(modelFile, demandClassifier)
	}

	def persistProducts(modelFile: File): Unit  = {
		persist(modelFile, productClassifiers)
	}

	private def persist(modelFile: File, o: Object): Unit  = {
		requireNonNull(o)

		val objectStream = new ObjectOutputStream(new FileOutputStream(modelFile))
		objectStream.writeObject(o)
		objectStream.flush()
		objectStream.close()

	}

	def loadDemand(modelFile: File): Unit = {
		demandClassifier = loadModel(modelFile).asInstanceOf[DemandClassifier]
	}

	def loadProduct(modelFile: File): Unit = {
		productClassifiers = loadModel(modelFile).asInstanceOf[List[ProductClassifier]]
	}


	private def loadModel(modelFile: File): Object = {
		val objectStream = new ObjectInputStream(new FileInputStream(modelFile))
		val o = objectStream.readObject()
		objectStream.close()
		o
	}

	def trainDemand(trainingSamples: java.util.List[Document]): Unit = {
		val featureExtraction = new FeatureExtractorBuilder(null).buildForDemand(trainingSamples)
		demandClassifier = new DemandClassifier("demand",
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
		requireNonNull(demandClassifier)
		demandClassifier.classProbability(text).prob
	}

	def predictProduct(text: String): java.util.List[Classification] = {
		requireNonNull(productClassifiers)
		productClassifiers.map { classifier =>
			val prob = classifier.classProbability(text).prob
			Classification(classifier.className, prob)
		}.sortBy(-_.prob).asJava
	}

	case class Classification(product: String, prob: Double)

}
