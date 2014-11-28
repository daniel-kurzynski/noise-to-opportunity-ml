package de.hpi.smm

abstract class Feature {

	/**
	 * Name of a feature, will be the header in the csv file
	 */
	def name: String

	/**
	 * Extract a feature vector element from a post
	 */
	def extract(post: Post): Double

	/**
	 * Some features need to see all posts, before they can build a feature vector element,
	 * e.g. a BOW model with the n highest words needs to know all words beforehand.
	 *
	 * This method is called for each post, that is added as an instance, and the feature can build internal state
	 * with that post.
	 */
	def touch(post: Post): Unit = {}
}

class NeedWordFeature(word: String) extends Feature {
	def name = word
	override def extract(post: Post): Double = {
		post.tokens.count(_ == word)
	}
}
