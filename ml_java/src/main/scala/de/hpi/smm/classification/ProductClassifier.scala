package de.hpi.smm.classification

import weka.classifiers.`lazy`.IBk
import weka.core.{Instances, Capabilities, Instance}
import weka.filters.Filter
import weka.filters.unsupervised.attribute.{Normalize, StringToWordVector}

class ProductClassifier extends weka.classifiers.Classifier {

	val baseClassifier = new IBk(5)
	val classifier = new PriorClassifier(baseClassifier, Array(1.0, 10.00, 10.0, 10.0, 10.0))

	var tdfIdfFilter = null.asInstanceOf[StringToWordVector]
	var normelizeFilter = null.asInstanceOf[Normalize]
	var structure = null.asInstanceOf[Instances]


	private def initialFeautureCreation(data: Instances): Instances = {
		structure = new Instances(data,0,0)
		structure.clear()
		tdfIdfFilter = new StringToWordVector()
		tdfIdfFilter.setIDFTransform(true)
		tdfIdfFilter.setTFTransform(true)
		tdfIdfFilter.setWordsToKeep(5)
		tdfIdfFilter.setInputFormat(data)

		val filteredInstances = Filter.useFilter(data,tdfIdfFilter)

		normelizeFilter = new Normalize()
		normelizeFilter.setInputFormat(filteredInstances)

		val normilizedInstances = Filter.useFilter(filteredInstances,normelizeFilter)

		normilizedInstances
	}

	private def feautureCreation(instance: Instance): Instance = {

		val instances = new Instances(structure)
		structure.add(instance)

		val filteredInstances = Filter.useFilter(structure,tdfIdfFilter)
		val normilizedInstances = Filter.useFilter(filteredInstances,normelizeFilter)

		normilizedInstances.firstInstance()
	}


	override def buildClassifier(data: Instances): Unit = {
		var trainingData = initialFeautureCreation(data)
		classifier.buildClassifier(trainingData)
	}

	override def distributionForInstance(instance: Instance): Array[Double] = {
		val featuredInstance = feautureCreation(instance);
		classifier.distributionForInstance(featuredInstance)
	}

	override def getCapabilities: Capabilities = {
		throw new RuntimeException("Not implemented yet")
	}

	override def classifyInstance(instance: Instance): Double = {
		val featuredInstance = feautureCreation(instance);
		classifier.classifyInstance(featuredInstance)
	}
}
