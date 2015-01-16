package de.hpi.smm.classification

import weka.classifiers.bayes.NaiveBayes
import weka.core.{Utils, Instances, Capabilities, Instance}

class NaiveBayesClassPriorClassifier(nbClassifier: NaiveBayes, classCounts: Array[Int]) extends weka.classifiers.Classifier with Serializable {
	override def buildClassifier(data: Instances): Unit = {
		nbClassifier.buildClassifier(data)
	}

	override def distributionForInstance(instance: Instance): Array[Double] = {
		val distribution = nbClassifier.distributionForInstance(instance)
		distribution(0) /= classCounts(0) * 100
		distribution(1) /= classCounts(1)
		Utils.normalize(distribution)
		distribution
//		throw new RuntimeException("WHY ON EARTH SHOULD I IMPLEMENT THAT.")
	}

	override def getCapabilities: Capabilities = {
		nbClassifier.getCapabilities
	}

	override def classifyInstance(instance: Instance): Double = {
		nbClassifier.classifyInstance(instance)
	}
}
