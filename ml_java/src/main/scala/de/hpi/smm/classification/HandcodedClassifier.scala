package de.hpi.smm.classification

import weka.classifiers.AbstractClassifier
import weka.core.{Attribute, Instances, Instance}
import scala.collection.mutable

class HandcodedClassifier(wordCountWithTfIdf: mutable.Map[String, mutable.Map[String, Double]], featureWords: Map[String, Int]) extends AbstractClassifier {


	var importantIndices: mutable.Map[String, List[String]] = null

	var classAttr: Attribute = null
	override def buildClassifier(data: Instances): Unit = {
//		val relevantIndices: Array[Array[Int]] = Array()
		classAttr = data.classAttribute()

		println("x" * 100)
		importantIndices = wordCountWithTfIdf.map { case (className, counts) =>
			val importantWords = counts.toList.sortBy(-_._2).take(3).map(_._1)
			counts --= (counts.keySet -- importantWords)
			println(className)
			println(counts.keys)
			(className, counts.keys.toList)
		}
		println(importantIndices.keys)
		println("x" * 100)
	}

	var i = 0
	override def classifyInstance(instance: Instance): Double = {

		val predictedClass = importantIndices.maxBy { case (className, words) =>
			val values = words.map { word => instance.value(featureWords(word)) * wordCountWithTfIdf(className)(word) }
			val sum = values.sum
			sum
		}._1

		classAttr.indexOfValue(predictedClass)

//		if (instance.value(featureWords("crm")) > 0.0)
//			return 1.0
//		else if (instance.value(featureWords("e")) > 0.0)
//			return 2.0
//		else if (instance.value(featureWords("hcm")) > 0.0)
//			return 0.0
//		else if (instance.value(featureWords("cloud")) > 0.0)
//			return 3.0
//		3.0
	}

}
