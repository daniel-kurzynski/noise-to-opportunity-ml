package de.hpi.smm.classification.helper

import weka.classifiers.AbstractClassifier
import weka.core.{Capabilities, Instance, Instances}

class NoneClassifier(classifier: weka.classifiers.Classifier) extends AbstractClassifier with Serializable {
	var noneIndex: Int = -1

	override def buildClassifier(data: Instances): Unit = {
		noneIndex = data.classAttribute().indexOfValue("None")
		if (noneIndex == -1)
			throw new Exception("Did not find 'None' class.")
		classifier.buildClassifier(data)
	}

	private def isNone(distribution: Array[Double]): Boolean = {
		val noNoneDist = distribution.zipWithIndex.filter(_._2!=noneIndex).map(_._1).sortBy(-_)
		val maxValue = noNoneDist.max
		val minValue = noNoneDist.min
		maxValue-minValue<1.0/noNoneDist.length || maxValue<0.3
	}

	override def distributionForInstance(instance: Instance): Array[Double] = {
		val distribution = classifier.distributionForInstance(instance)
//		println(distribution.mkString(" "))
		if(isNone(distribution)) {
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
