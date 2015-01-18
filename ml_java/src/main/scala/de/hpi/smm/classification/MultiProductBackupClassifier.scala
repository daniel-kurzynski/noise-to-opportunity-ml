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
class MultiProductBackupClassifier(val brochures: List[Document], val posts: List[Document], val dataReader: DataReader) {

	val classes = List("CRM", "ECOM", "HCM", "LVM", "None")
	val tokenMap = Map(
		"HCM"-> List("HCM"),
		"LVM"-> List("LVM"),
		"CRM"-> List("CRM", "customer", "contact"),
		"ECOM"-> List("ECOM")
	)

	def classProbability(text: String): List[Classification] = {

		val id = ""
		val title = ""

		val rawPost = RawDocument(id, title, text, null)
		val sentences = dataReader.detectSentences(rawPost)
		val post = Document(id, title, text, sentences, "None")

		val dist = classes.map{ className =>
			{
				val classTokens = tokenMap(className)

				val sumOfTokens = classTokens.map { token =>
						if(post.textTokens.contains(token)) 1 else 0
				}.sum

				(1.0 * sumOfTokens/classTokens.size)
			}
		}

		val normDist = dist.map(distValue => distValue/dist.sum)

		classes.zipWithIndex.map { case(className, index) =>
			Classification(className, ClassificationOutput(dist(index),new Array[Array[Any]](0)))
		}

	}

	def validate(): Evaluation = {
		throw RuntimeException("Not implemented yet!")
	}
}
