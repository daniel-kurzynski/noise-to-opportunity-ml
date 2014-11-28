package de.hpi.smm.feature_extraction

import de.hpi.smm.domain.Post

class NeedWordFeature(word: String) extends Feature {
	def name = s"#$word"
	override def extract(post: Post): Double = {
		post.textTokens.count(_ == word)
	}
}

class QuestionNumberFeature extends Feature {
	override def name: String = "#questions"

	override def extract(post: Post): Double = {
		post.sentences.count { sentence =>
			sentence.last.text == "?"
		}
	}
}

class ImperativeNumberFeature extends Feature {
	override def name: String = "#imperatives"

	override def extract(post: Post): Double = {
		post.tokens.count { word =>
			word.pos == "VB"
		}
	}
}
