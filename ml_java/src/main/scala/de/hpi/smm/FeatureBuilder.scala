package de.hpi.smm

class FeatureBuilder {

	var posts = List[Post]()
	var features: List[Feature] = List()

	/**
	 * Demand posts often contain more questions than normal posts,
	 * especially many questions in a row sometimes
	 */
	def questionNumber(): FeatureBuilder = {
		// TODO IMPLEMENT
		this
	}

	/**
	 * Demand posts often contain imperative clauses like "help me", "share your information"
	 */
	def imperativeWords(): FeatureBuilder = {
		// TODO IMPLEMENT
		this
	}

	/**
	 * Captures common need words like "required", "need" etc.
	 */
	def needWords(): FeatureBuilder = {
		// TODO IMPLEMENT
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
	def buildFeatureVector(): Array[Array[Double]] = {
		val featureVec = Array.ofDim[Double](posts.size, features.size)
		features.zipWithIndex.foreach { case (feature, i) =>
			featureVec(i) = feature.extract(post)
		}
		featureVec
	}
}

object FeatureBuilder {
	def apply(): FeatureBuilder = new FeatureBuilder
}
