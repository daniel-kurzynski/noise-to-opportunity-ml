package de.hpi.smm.classification.old_classifier

import com.blog_intelligence.nto.{Document, RawDocument}
import de.hpi.smm.classification.{ClassificationOutput, Classification}
import de.hpi.smm.nlp.NLP
import weka.classifiers.Evaluation

class MultiProductBackupClassifier(val brochures: List[Document], val posts: List[Document], classNames: List[String])  {

	val tokenMap = Map(
		"HCM"-> List("HCM", "hr"),
		"LVM"-> List("LVM", "virtual", "servers", "landscape"),
		"CRM"-> List("CRM", "customer", "customers", "contact"),
		"ECOM"-> List("ECOM", "e-commerce", "shopping", "entrepreneurs", "ecommerce"),
		"None"-> List("NoneType")
	)

	def classProbability(text: String): List[Classification] = {

		val id = ""
		val title = ""

		val rawPost = RawDocument(id, title, text, null)
		val sentences = NLP.detectSentences(rawPost)
		val post = Document(id, title, text, sentences, "None")

		val dist = classNames.map { className =>
			val classTokens = tokenMap(className)

			val sumOfTokens = classTokens.map { token =>
				val postTokens = post.textTokens
				if (postTokens.contains(token.toLowerCase)) 1 else 0
			}.sum

			1.0 * sumOfTokens / classTokens.size
		}

		val normDist = dist.map(distValue => distValue/dist.sum)

		classNames.zipWithIndex.map { case(className, index) =>
			Classification(className, ClassificationOutput(dist(index),new Array[Array[Any]](0)))
		}

	}

	def validate(): Evaluation = {
		throw new RuntimeException("Not implemented yet!")
	}
}
