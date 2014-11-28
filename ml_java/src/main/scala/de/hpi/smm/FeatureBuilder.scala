package de.hpi.smm

class FeatureBuilder {

	var featurePipeline: List[Feature] = List()


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

	def buildFeatureVector(post: Post): Array[Double] = {
		val features = new Array[Double](featurePipeline.size)
		featurePipeline.zipWithIndex.foreach { case (feature, i) =>
			features(i) = feature.extract(post)
		}
		features
	}

}

object FeatureBuilder {
	def apply(): FeatureBuilder = new FeatureBuilder
}