package de.hpi.smm.domain

case class RawPost(id: String,
                   title: String,
                   text: String,
	               rawClassification: Map[String, Map[String, String]]) {
	def wholeText: String = s"$title $text"


	def extractClass(): String = {
		val groups = rawClassification("demand").values.groupBy { word => word }.map { case (word, words) => (word, words.size)}
		val sortedCategories = groups.toList.sortBy(-_._2)
		if (sortedCategories.size > 1 && sortedCategories(0)._2 == sortedCategories(1)._2)
			null
		else
			sortedCategories(0)._1
	}
}

case class Post(
	               id: String,
	               title: String,
	               text: String,
	               sentences: Seq[Seq[Word]],
	               demandClass: String) {

	/*
	 * Helper functions for quick access to necessary data structures
	 */
	def wholeText: String = s"$title $text"

	def textTokens: Seq[String] = {
		sentences.flatMap { sentence =>
			sentence.map { word => word.text }
		}
	}

	def tokens: Seq[Word] = {
		sentences.flatMap { sentence => sentence }
	}

}
