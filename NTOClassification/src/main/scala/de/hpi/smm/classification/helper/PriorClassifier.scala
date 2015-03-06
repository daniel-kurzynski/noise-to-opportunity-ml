package de.hpi.smm.classification.helper

import weka.core.{Capabilities, Instance, Instances, Utils}

class PriorClassifier(classifier: weka.classifiers.Classifier, parameterFactors: Array[Double]) extends weka.classifiers.Classifier with Serializable {
	override def buildClassifier(data: Instances): Unit = {
		classifier.buildClassifier(data)
	}

	override def distributionForInstance(instance: Instance): Array[Double] = {
		val distribution = classifier.distributionForInstance(instance)
		assert(parameterFactors.size == distribution.size)

//		if (distribution.size == 5)
//			println(distribution.mkString("   "))
		parameterFactors.zipWithIndex.foreach { case (value, i) =>
				distribution(i) *= value
		}

		Utils.normalize(distribution)
		distribution
	}

	override def getCapabilities: Capabilities = {
		classifier.getCapabilities
	}

	override def classifyInstance(instance: Instance): Double = {
		classifier.classifyInstance(instance)
	}
}
