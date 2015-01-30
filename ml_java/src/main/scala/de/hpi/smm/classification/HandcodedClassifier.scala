package de.hpi.smm.classification

import weka.classifiers.{AbstractClassifier, Classifier}
import weka.core.{Instances, Capabilities, Instance}

class HandcodedClassifier extends AbstractClassifier {

	override def buildClassifier(data: Instances): Unit = {
	}

	override def getCapabilities: Capabilities = ???

	override def classifyInstance(instance: Instance): Double = {
		0.0
	}
}
