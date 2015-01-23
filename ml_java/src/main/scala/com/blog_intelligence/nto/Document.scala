package com.blog_intelligence.nto

import de.hpi.smm.domain.Word

case class RawDocument(id: String,
                   title: String,
                   text: String,
	               rawClassification: Map[String, Map[String, String]],
	               lang: String = null) {
	def wholeText: String = s"$title $text"




	def extractDemand(): String = {
		extract("demand")
	}
	def extractCategory(): String = {
		extract("category")
	}

	def extract(classKey: String): String = {
		if (rawClassification == null)
			return null
		try {
			val groups = rawClassification(classKey).values.groupBy { word => word }.map { case (word, words) => (word, words.size)}
			val sortedCategories = groups.toList.sortBy(-_._2)
			if (sortedCategories.size > 1 && sortedCategories(0)._2 == sortedCategories(1)._2)
				null
			else
				sortedCategories(0)._1
		}
		catch {
			case e: Throwable =>
				println(rawClassification)
				throw e
		}
	}
}

case class Document(
	               id: String,
	               title: String,
	               text: String,
	               sentences: Seq[Seq[Word]],
	               documentClass: String) {

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

	def isClassified: Boolean = {
		documentClass != null
	}

}
