package de.hpi.smm.feature_extraction

import com.blog_intelligence.nto.Document
import de.hpi.smm.domain.Switch

abstract class Feature {

	/**
	 * Name of a feature, will be the header in the csv file.
	 */
	def name: Array[String]

	/**
	 * Returns a Switch of possible implementations for this feature.
	 */
	def extract(): Switch

	/**
	 * Some features need to see all posts, before they can build a feature vector element,
	 * e.g. a BOW model with the n highest words needs to know all words beforehand.
	 *
	 * This method is called for each post, that is added as an instance, and the feature can build internal state
	 * with that post.
	 */
	def touch(post: Document): Unit = {}

	def finishTraining(): Unit = { }
}
