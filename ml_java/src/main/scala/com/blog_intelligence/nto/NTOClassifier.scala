package com.blog_intelligence.nto

import java.io._

import de.hpi.smm.FeatureExtractorBuilder
import de.hpi.smm.classification.{DemandClassifier, ProductClassifier}
import de.hpi.smm.nlp.NLP

import scala.collection.JavaConverters._
import scala.collection.mutable

case class ProductClassification(product: String, prob: Double)
case class FullPrediction(demandProb: Double, productClass: String, productProb: Double)
case class PredictedPost(text: String, fullPrediction: FullPrediction)


class NTOClassifier(nlp: NLP) {

	var demandClassifier: DemandClassifier = null
	var productClassifier: ProductClassifier = null

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
		productClassifier = loadModel(modelFile).asInstanceOf[ProductClassifier]
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
			featureExtraction,
			nlp)
	}

	def trainProduct(trainingSamples: java.util.List[Document]): Unit = {
		productClassifier = new ProductClassifier(trainingSamples.asScala.toList, nlp)
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

	def extractMostCertainPosts(num: Integer, productClass: String, posts: java.util.List[String]): java.util.List[PredictedPost] = {

		val predictions = mutable.Map[String, FullPrediction]()

		posts.asScala.foreach { post =>
			val demandPrediction = demandClassifier.classProbability(post)
			val productPrediction = productClassifier.predict(post).maxBy(_.prob)
			predictions(post) = FullPrediction(demandPrediction.prob, productPrediction.product, productPrediction.prob)
		}


		def harmonicMean(prediction: FullPrediction, measure: Double = 1): Double = {
			((1 + math.pow(measure, 2)) * prediction.demandProb * prediction.productProb) / (math.pow(measure, 2) * prediction.demandProb + prediction.productProb)
		}

		predictions.toList
			.filter {case (doc, prediction) => prediction.productClass == productClass}
			.filter {case (doc, prediction) => harmonicMean(prediction, 2) > 0.6}
			.sortBy {case (doc, prediction) => -harmonicMean(prediction, 2)}
			.take(num)
			.map {case (doc, prediction) => new PredictedPost(doc, prediction)}
			.asJava
	}

}
