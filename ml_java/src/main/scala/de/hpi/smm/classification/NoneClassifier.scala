package de.hpi.smm.classification

import weka.classifiers.AbstractClassifier
import weka.core.{Instances, Capabilities, Instance}

class NoneClassifier(classifier: weka.classifiers.Classifier) extends AbstractClassifier with Serializable {
	var noneIndex: Int = -1

	override def buildClassifier(data: Instances): Unit = {
		noneIndex = data.classAttribute().indexOfValue("None")
		if (noneIndex == -1)
			throw new Exception("Did not find 'None' class.")
		classifier.buildClassifier(data)
	}

	override def distributionForInstance(instance: Instance): Array[Double] = {
		val distribution = classifier.distributionForInstance(instance)
		val (maxProb, maxIndex) = distribution.zipWithIndex.maxBy(_._1)
		if(maxIndex != noneIndex && maxProb < 0.5) {
			distribution.zipWithIndex.map { case (prob, index) =>
				if(index == noneIndex) 1.0 else 0.0
			}
		}
		else {
			distribution
		}
	}

	override def getCapabilities: Capabilities = {
		classifier.getCapabilities
	}

//	override def classifyInstance(instance: Instance): Double = {
//		val distribution = distributionForInstance(instance)
//		val (_, maxIndex) = distribution.zipWithIndex.maxBy(_._1)
//		maxIndex
//	}
}
