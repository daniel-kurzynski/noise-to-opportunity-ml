package com.blog_intelligence.nto

import java.io._

import de.hpi.smm.FeatureExtractorBuilder
import de.hpi.smm.classification.{ProductAnalyzer, DemandClassifier}
import scala.collection.JavaConverters._
import scala.collection.mutable

case class ProductClassification(product: String, prob: Double)

class NTOClassifier {

	var demandClassifier: DemandClassifier = null
	var productClassifier: ProductAnalyzer = null

	def requireNonNull(o: Object) {
		if (o == null)
			throw new Exception("Need to train the classifier first.")
	}

	def persistDemand(modelFile: File): Unit  = {
		persist(modelFile, demandClassifier)
	}

	def persistProducts(modelFile: File): Unit  = {
		persist(modelFile, productClassifier)
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
		productClassifier = loadModel(modelFile).asInstanceOf[ProductAnalyzer]
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
		productClassifier = new ProductAnalyzer(trainingSamples.asScala.toList)
		productClassifier.buildClassifier()
	}

	def predictDemand(text: String): Double = {
		requireNonNull(demandClassifier)
		demandClassifier.classProbability(text).prob
	}

	def predictProduct(text: String): java.util.List[ProductClassification] = {
		requireNonNull(productClassifier)
		productClassifier.predict(text).sortBy(-_.prob).asJava
	}
}
