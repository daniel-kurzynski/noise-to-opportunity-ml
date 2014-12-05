package de.hpi.smm.feature_extraction

import de.hpi.smm.domain.Post

class FeatureBuilder {
	def names: Array[String] = features.map(_.name).toArray


	var posts = List[Post]()
	var features: List[Feature] = List()

	val relevantNeedWords = List("anyone", "opportunity", "need", "must", "have", "you", "required", "require", "please",
	"offering", "offer", "advice", "thank", "share", "sharing", "expertise", "urgent", "urgently", "appreciated",
	"informative", "guide")

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
		relevantNeedWords.foreach { word =>
			addFeature(new NeedWordFeature(word))
		}
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
		posts.foreach { post =>
      vectorHandler(post, features.map { feature => feature.extract(post) }.toArray)
		}
	}

	private def addFeature(feature: Feature): Unit = {
		features ::= feature
	}
}

object FeatureBuilder {
	def apply(): FeatureBuilder = new FeatureBuilder
}
