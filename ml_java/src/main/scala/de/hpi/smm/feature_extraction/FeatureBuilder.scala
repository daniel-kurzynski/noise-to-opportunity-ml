package de.hpi.smm.feature_extraction

import de.hpi.smm.domain.{GenericCountsCounter, Document}

class FeatureBuilder {

	def names: Array[String] = {
		val values = features.flatMap(_.name).toArray
		val names = new Array[String](values.size + 2)
		names(0) = "id"
		names(names.size - 1) = "CLASS"
		System.arraycopy(values, 0, names, 1, values.size)
		names
	}


	var posts = List[Document]()
	var features: List[Feature] = List()


	/**
	 * Demand posts often contain more questions than normal posts,
	 * especially many questions in a row sometimes
	 */
	def questionNumber(): FeatureBuilder = {
		addFeature(new QuestionNumberFeature())
		this
	}

	def questionWords(): FeatureBuilder = {
		addFeature(new QuestionWordsFeature())
		this
	}

	def addressTheReader(): FeatureBuilder = {
		addFeature(new AddressReaderFeature())
		this
	}

	def needNGrams(): FeatureBuilder = {
		addFeature(new NeedNGramsFeature())
		this
	}

	def containsEMail(): FeatureBuilder = {
		addFeature(new ContainsEMailFeature())
		this
	}

	/**
	 * Demand posts often contain imperative clauses like "help me", "share your information"
	 */
	def imperativeWords(): FeatureBuilder = {
		addFeature(new ImperativeNumberFeature())
		this
	}

	/**
	 * Captures common need words like "required", "need" etc.
	 */
	def needWords(counts: GenericCountsCounter, clsName: String, thresholds: (Double, Double)): FeatureBuilder = {
		addFeature(new NeedWordFeature(counts, clsName, thresholds))
		this
	}

	/**
	 * Captures common thank you notes at the end of a demand post
	 */
	def thankYou(): FeatureBuilder = {
		// TODO IMPLEMENT
		this
	}

	/**
	 * Captures common endings like "share your experiences", "please share your results"
	 */
	def share(): FeatureBuilder = {
		// TODO IMPLEMENT
		this
	}

	/**
	 * Captures the most common demand words from a BOW model
	 */
	def mostCommonWordsFromBOW(): FeatureBuilder = {
		// TODO IMPLEMENT
		this
	}

	/**
	 * Add a new posts to this feature builder, and store it for internal use.
	 */
	def touch(post: Document): Unit = {
		posts ::= post
		if (post.isClassified)
			features.foreach { feature => feature.touch(post)}
	}

	def finishTraining(): Unit = {
		features.foreach(_.finishTraining())
	}

	def buildFeatureVector(vectorHandler: (Document, Array[Double]) => Unit): Unit = {
		//		val allCases = features.map { feature => feature.extract().cases }
		//
		//		val allCombinations = allCases.foldLeft(Seq(Seq[Case]())) { (feature, cases) =>
		//			cross(feature.toList, cases.toList)
		//		}
		posts.foreach { post =>
			vectorHandler(post, features.map { feature => feature.extract().default(post)}.toArray.flatten)
		}
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
}

object FeatureBuilder {
	def apply(): FeatureBuilder = new FeatureBuilder
}
