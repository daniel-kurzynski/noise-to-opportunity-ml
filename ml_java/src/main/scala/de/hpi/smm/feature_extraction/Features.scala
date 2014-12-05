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
			// Adding the part after '||' brings 6 % precision and -3 % recall
			sentence.last.text == "?" || sentence.head.pos.startsWith("W")
		}
	}
}

class ImperativeNumberFeature extends Feature {
	override def name: String = "#imperatives"

	override def extract(post: Post): Double = {
		post.tokens.count { word =>
			if (word.pos == "VB") {
//				println(word)
				true
			}
			else
				false
		}
	}
}

class QuestionWordsFeature extends Feature {
	override def name: String = "#question-words"
	override def extract(post: Post): Double = {
		val counts = post.tokens.count { word =>
			word.pos.startsWith("W")
		}
		counts
	}
}


//class DummyFeature extends Feature {
//	override def name: String = ???
//	override def extract(post: Post): Double = ???
//}
