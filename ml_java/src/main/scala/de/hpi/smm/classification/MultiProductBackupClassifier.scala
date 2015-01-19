package de.hpi.smm.classification

import java.util

import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.domain.{Document, RawDocument}
import weka.classifiers.Evaluation
import weka.classifiers.`lazy`.IBk
import weka.classifiers.functions.SMO
import weka.classifiers.trees.J48
import weka.classifiers.trees.j48.C45PruneableClassifierTree

import weka.core.{Utils, Attribute, DenseInstance, Instances}
import weka.filters.Filter
import weka.filters.unsupervised.attribute.{Normalize, StringToWordVector}
import scala.collection.JavaConverters._

/**
 * Created by Daniel on 18.01.2015.
 */
class MultiProductBackupClassifier(val brochures: List[Document], val posts: List[Document], classNames: List[String], val dataReader: DataReader) {

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
		val sentences = dataReader.detectSentences(rawPost)
		val post = Document(id, title, text, sentences, "None")

		val dist = classNames.map{ className =>
			{
				val classTokens = tokenMap(className)

				val sumOfTokens = classTokens.map { token =>
					val postTokens = post.textTokens
						if(postTokens.contains(token.toLowerCase)) 1 else 0
				}.sum

				(1.0 * sumOfTokens/classTokens.size)

			}
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
