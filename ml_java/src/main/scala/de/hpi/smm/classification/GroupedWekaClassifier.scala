package de.hpi.smm.classification

import weka.core.{Instances, Capabilities, Instance}

/**
 * Created by Daniel on 19.01.2015.
 */
class GroupedWekaClassifier(classifiers: Map[String, weka.classifiers.Classifier]) extends weka.classifiers.Classifier{

	def distributionForInstance(instance: Instance): Array[Double]={
		val classNames = classifiers.keys

		var dist = classNames.map {className =>
			val classifer = classifiers(className)
			classifer.distributionForInstance(instance)(0)
		}.toList

		dist ::= 1-dist.max
		dist.toArray
	}

	override def buildClassifier(data: Instances): Unit = {
		throw new RuntimeException("Not implemented yet!")
	}

	override def getCapabilities: Capabilities ={
		throw new RuntimeException("Not implemented yet!")
	}

	override def classifyInstance(instance: Instance): Double = {
		distributionForInstance(instance).zipWithIndex.max._2
	}
}
