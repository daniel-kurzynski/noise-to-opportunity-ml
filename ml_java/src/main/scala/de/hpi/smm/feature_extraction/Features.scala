package de.hpi.smm.feature_extraction

import de.hpi.smm.domain.{Case, Switch, Post}

import scala.collection.mutable

class NeedWordFeature() extends Feature {
	def name = relevantNeedWords

	val relevantNeedWords = Array(
		"advice", "anyone", "appreciate", "appreciated", "contact", "curious", "expertise",
		"guide", "have", "informative", "interested",
		"looking", "must", "need", "offer", "offering",
		"opportunity", "perspective", "please", "require", "required",
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
//	override def name: Array[String] = relevantNGrams.map(_.mkString(" "))
	override def name: Array[String] = Array("bigrams")
	val relevantNGrams = Array(
		Array("looking", "for"),
		Array("you", "could"),
		Array("could", "you"),
		Array("help", "me"),
		Array("your", "thoughts"),
		Array("interested", "in"),
		Array("in", "advance")
	)

	override def extract(): Switch = {
		Switch(Case(post => {
				val min = relevantNGrams.map(_.size).min
				val max = relevantNGrams.map(_.size).max

				Array((min to max).map { windowSize =>
					post.textTokens.sliding(windowSize).count { t =>
						val relevantNGram = t.seq.toArray
						relevantNGrams.filter(_.size == windowSize).exists { ngram =>
							ngram.map(_.toLowerCase).deep == relevantNGram.deep
						}
					}.toDouble
				}.sum)
			}, "ngrams-one-feature"),
			Case(post => {
			val min = relevantNGrams.map(_.size).min
			val max = relevantNGrams.map(_.size).max

			(min to max).flatMap { windowSize =>
				val currentNGrams = relevantNGrams.filter(_.size == windowSize)
				val currentCounts = mutable.ListMap[Array[String], Double]()
				currentNGrams.foreach { ngram =>
					currentCounts(ngram) = 0
				}
				post.textTokens.sliding(windowSize).foreach { t =>
					currentNGrams.foreach { ngram =>
						if (t.seq.toArray.deep == ngram.deep)
							currentCounts(ngram) += 1
					}
				}
				val results = currentCounts.values.toArray
				results
			}.toArray
		}, "ngrams-different-features")
		)
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

	val addressWords = Set("I")

	override def extract(): Switch = {
		Switch(post =>
			Array(post.textTokens.count { word => addressWords.contains(word.toLowerCase)}.toDouble)
		)
	}
}

class ContainsEMailFeature extends Feature {
	override def name: Array[String] = Array("contains-email")

	override def extract(): Switch = {
		Switch(post => {
			Array(post.text.count { character => character == '@' }.toDouble)
		})
	}
}
