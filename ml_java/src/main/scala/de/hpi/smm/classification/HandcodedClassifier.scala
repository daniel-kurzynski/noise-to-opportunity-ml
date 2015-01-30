package de.hpi.smm.classification

import weka.classifiers.AbstractClassifier
import weka.core.{Instances, Capabilities, Instance}

class HandcodedClassifier(featureWords: Map[String, Int]) extends AbstractClassifier {

	override def buildClassifier(data: Instances): Unit = {
	}

	override def getCapabilities: Capabilities = ???

	override def classifyInstance(instance: Instance): Double = {
		if (instance.value(featureWords("crm")) > 0.0)
			return 1.0
		else if (instance.value(featureWords("e")) > 0.0)
			return 2.0
		else if (instance.value(featureWords("hcm")) > 0.0)
			return 0.0
		else if (instance.value(featureWords("lvm")) > 0.0)
			return 3.0
		3.0
	}
}
