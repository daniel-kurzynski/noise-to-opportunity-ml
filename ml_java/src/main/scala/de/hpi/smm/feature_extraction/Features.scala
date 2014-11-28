package de.hpi.smm.feature_extraction

import de.hpi.smm.domain.Post

class NeedWordFeature(word: String) extends Feature {
	def name = s"occurrence_of_$word"
	override def extract(post: Post): Double = {
		post.tokens.count(_ == word)
	}
}

class QuestionNumberFeature extends Feature {
	override def name: String = "number_of_questions"

	/**
	 * Extract a feature vector element from a post
	 */
	override def extract(post: Post): Double = {
		post.numberOfQuestions
	}
}
