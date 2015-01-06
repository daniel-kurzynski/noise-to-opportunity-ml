package de.hpi.smm.feature_extraction

import de.hpi.smm.domain._

import scala.collection.mutable

class NeedWordFeature(counts: GenericCountsCounter, clsName: String, thresholds: (Double, Double), words: Array[String]) extends Feature {
	var relevantNeedWordsStore: Array[String] = _

	def name(): Array[String] = {
		relevantNeedWordsStore
	}

	def this(counts: GenericCountsCounter, clsName: String, thresholds: (Double, Double)) = {
		this(counts, clsName, thresholds, null)
	}

	override def finishTraining(): Unit = {
		relevantNeedWordsStore = determineRelevantWords()
	}

	private def determineRelevantWords(): Array[String] = {
		val result = (counts.takeTopOccurrence(clsName, thresholds._1).map(_._1) ++
				counts.takeTopNotOccurrence(clsName, thresholds._2).map(_._1)).toArray.distinct
		result
	}

	override def extract(): Switch = {
		Switch(
			Case({ post =>
				relevantNeedWordsStore.map { word => post.textTokens.count(_.toLowerCase == word).toDouble}
			},
			"need-word-with-lowercase")
			,
			Case(post => relevantNeedWordsStore.map { word => post.textTokens.count(_ == word).toDouble},
			"need-word-without-lowercase")
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
		Switch(
			Case(post => {
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
			Array(post.text.count { character => character == '@'}.toDouble)
		})
	}

}
