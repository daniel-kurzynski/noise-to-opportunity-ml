package de.hpi.smm.feature_extraction

import de.hpi.smm.domain.{Case, Post}

class FeatureBuilder {
	def names: Array[String] = features.flatMap(_.name).toArray


	var posts = List[Post]()
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
	def needWords(): FeatureBuilder = {
		addFeature(new NeedWordFeature())
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
	def touch(post: Post): Unit = {
		posts ::= post
		features.foreach { feature => feature.touch(post) }
	}
	def buildFeatureVector(vectorHandler: (Post, Array[Double]) => Unit): Unit = {
		val allCases = features.map { feature => feature.extract().cases }
		println(allCases)

		val allCombinations = allCases.foldLeft(Seq(Seq[Case]())) { (feature, cases) =>
			cross(feature.toList, cases.toList)
		}

		posts.foreach { post =>
			vectorHandler(post, features.map { feature => feature.extract().default(post) }.toArray.flatten)
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
