package de.hpi.smm

abstract class Feature {

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
