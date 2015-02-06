package de.hpi.smm.classification

import weka.classifiers.AbstractClassifier
import weka.core.{Instance, Instances}

class TheirClassifier extends AbstractClassifier {


	var instances: Instances = _
	override def buildClassifier(data: Instances): Unit = {
		instances = data
	}

	override def classifyInstance(data: Instance): Double = {
		val theirClassification = data.asInstanceOf[CustomTheirInstance].theirClassification
		instances.classAttribute().indexOfValue(theirClassification)
	}
}
