package de.hpi.smm.feature_extraction

import com.blog_intelligence.nto.Document
import de.hpi.smm.domain.GenericCountsCounter

import scala.collection.mutable

class FeatureExtractor(smooting: Boolean) extends Serializable {

	var finished = false
	
	val genericCounter = new GenericCountsCounter()
	genericCounter.smoothing = smooting


	val blacklist = Array(
		".", ",", ":", "-RRB-", "-LRB-", "$",
		// english
		"IN", "DT", "TO", "CC", "VBZ",
		// german
		"APP", "ART", "KO", "KO", "PP",
		"PR", "PT", "TRUNC", "VA", "VM", "VV"
	)

	def names: Array[String] = {
		val values = features.flatMap(_.name).toArray
		val names = new Array[String](values.size + 2)
		names(0) = "id"
		names(names.size - 1) = "CLASS"
		System.arraycopy(values, 0, names, 1, values.size)
		names
	}

	var features: List[Feature] = List()

	/**
	 * Demand posts often contain more questions than normal posts,
	 * especially many questions in a row sometimes
	 */
	def questionNumber(): FeatureExtractor = {
		addFeature(new QuestionNumberFeature())
		this
	}

	def questionWords(): FeatureExtractor = {
		addFeature(new QuestionWordsFeature())
		this
	}

	def addressTheReader(): FeatureExtractor = {
		addFeature(new AddressReaderFeature())
		this
	}

	def needNGrams(): FeatureExtractor = {
		addFeature(new NeedNGramsFeature())
		this
	}

	def containsEMail(): FeatureExtractor = {
		addFeature(new ContainsEMailFeature())
		this
	}

	/**
	 * Demand posts often contain imperative clauses like "help me", "share your information"
	 */
	def imperativeWords(): FeatureExtractor = {
		addFeature(new ImperativeNumberFeature())
		this
	}

	/**
	 * Captures common need words like "required", "need" etc.
	 */
	def needWords(clsName: String, thresholds: (Double, Double)): FeatureExtractor = {
		addFeature(new NeedWordFeature(genericCounter, clsName, thresholds))
		this
	}

	/**
	 * Add a new posts to this feature builder, and store it for internal use.
	 */
	def touch(document: Document): Unit = {
		if(finished)
			throw new Exception("Touch not possible - Learning already finished")

		if (document.isClassified) {
			features.foreach { feature => feature.touch(document)}
			countTypes(document)
			countWords(document)
		}
	}
	def finishTraining(): Unit = {
		finished = true
		features.foreach(_.finishTraining())
	}
	def buildFeatureVectors(documents: Seq[Document], vectorHandler: (Document, Array[Double]) => Unit): Unit = {
		documents.foreach { document =>
			buildFeatureVector(document, vectorHandler)
		}
	}

	def buildFeatureVector[T](document: Document, vectorHandler: (Document, Array[Double]) => T): T = {
		vectorHandler(document, features.map { feature => feature.extract().default(document) }.toArray.flatten)
	}

	private def addFeature(feature: Feature): Unit = {
		features ::= feature
	}

//	def cross[X](x: Seq[X], y: Seq[X]): Seq[Seq[X]] = {
//		for (xi <- x; yi <- y) yield Vector(xi, yi)
//	}

	def cross[X](x: Seq[Seq[X]], y: Seq[X]): Seq[Seq[X]] = {
		for (xi <- x; yi <- y) yield xi :+ yi
	}

	private def countTypes(doc: Document): Unit = {
		genericCounter.classCounts(doc.documentClass) += 1
	}

	private def countWords(doc: Document): Unit = {
		doc.sentences.flatten.filter { word => !blacklist.exists(word.pos.startsWith)}.map(_.text).distinct.foreach { word =>
			val lowerWord = word.toLowerCase
			if (!genericCounter.wordCounts.contains(lowerWord))
				genericCounter.wordCounts(lowerWord) = new mutable.HashMap[String, Int]().withDefaultValue(0)
			genericCounter.wordCounts(lowerWord)(doc.documentClass) += 1
		}
	}

	def removeClassCounts(className: String):Unit={
		genericCounter.classCounts.remove(className)
	}

	def takeTopOccurrence(className: String): List[(String, mutable.Map[String, Int], Double, Double)] = {
		genericCounter.takeTopOccurrence(className)
	}

	def takeTopOccurrence(className: String, threshold: Double): List[(String, mutable.Map[String, Int], Double, Double)] = {
		genericCounter.takeTopOccurrence(className, threshold)
	}

	def takeTopNotOccurrence(className: String): List[(String, mutable.Map[String, Int], Double, Double)] = {
		genericCounter.takeTopNotOccurrence(className)
	}

	def takeTopNotOccurrence(className: String, threshold: Double): List[(String, mutable.Map[String, Int], Double, Double)] = {
		genericCounter.takeTopNotOccurrence(className, threshold)
	}
}
