package de.hpi.smm.feature_extraction

import de.hpi.smm.domain.{Case, Switch, Post}

class NeedWordFeature() extends Feature {
	def name = relevantNeedWords

	val relevantNeedWords = Array(
		"advice", "anyone", "appreciate", "appreciated", "expertise",
		"guide", "have", "informative", "interested",
		"looking", "must", "need", "offer", "offering",
		"opportunity", "please", "require", "required",
		"share", "sharing", "thank", "urgent", "urgently",
		"you").reverse

	override def extract(): Switch = {
		Switch(
			Case(post => relevantNeedWords.map { word => post.textTokens.count(_ == word).toDouble },
				"need-word-without-lowercase"),
			Case(post => relevantNeedWords.map { word => post.textTokens.count(_.toLowerCase == word).toDouble },
				"need-word-with-lowercase")
		)
	}
}

class NeedNGramsFeature() extends Feature {
	override def name: Array[String] = relevantNGrams.map(_.mkString(" "))
	val relevantNGrams = Array(Array("looking", "for"), Array("interested", "in"))

	override def extract(): Switch = {
		Switch(post => {
			val min = relevantNGrams.map(_.size).min
			val max = relevantNGrams.map(_.size).max

			Array((min to max).map { windowSize =>
				post.textTokens.sliding(windowSize).count { t =>
					val relevantNGram = t.seq.toArray
					relevantNGrams.filter(_.size == windowSize).exists { ngram =>
						ngram.deep == relevantNGram.deep
					}
				}.toDouble
			}.sum)
		})
	}

}

class QuestionNumberFeature extends Feature {
	override def name: Array[String] = Array("#questions")

	override def extract(): Switch = {
		Switch(
			Case(post => Array(post.sentences.count { sentence => sentence.last.text == "?" }.toDouble),
				"question-number-without-question-word"),
			Case(post => Array(post.sentences.count { sentence => sentence.last.text == "?" || sentence.head.pos.startsWith("W") }.toDouble),
				"question-number-with-question-word")
		)
	}
}

class ImperativeNumberFeature extends Feature {
	override def name: Array[String] = Array("#imperatives")

	override def extract(): Switch = {
		Switch(post =>
			Array(post.tokens.count { word =>
				if (word.pos == "VB") {
	//				println(word)
					true
				}
				else
					false
			}.toDouble)
		)
	}
}

class QuestionWordsFeature extends Feature {
	override def name: Array[String] = Array("#question-words")
	override def extract(): Switch = {
		Switch(post =>
			Array(post.tokens.count { word => word.pos.startsWith("W")}.toDouble)
		)
	}
}

class AddressReaderFeature extends Feature {
	override def name: Array[String] = Array("addressing-the-reader")

	val addressWords = Set("you", "I")

	override def extract(): Switch = {
		Switch(post =>
			Array(post.textTokens.count { word => addressWords.contains(word.toLowerCase)}.toDouble)
		)
	}
}


