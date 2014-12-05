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
			sentence.last.text == "?"// || sentence.head.pos == "VP"
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
	val questionWords = Set("where", "how", "who")
	override def name: String = "#question-words"
	override def extract(post: Post): Double = {
		val counts = post.tokens.count { word =>
			questionWords.contains(word.text.toLowerCase)
		}
		counts
	}
}


//class DummyFeature extends Feature {
//	override def name: String = ???
//	override def extract(post: Post): Double = ???
//}
