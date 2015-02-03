package de.hpi.smm.classification

import weka.classifiers.`lazy`.IBk
import weka.classifiers.trees.J48
import weka.core.{Attribute, Instances, Capabilities, Instance}
import weka.filters.Filter
import weka.filters.unsupervised.attribute.{Normalize, StringToWordVector}

class ProductClassifier extends weka.classifiers.Classifier {

	val baseClassifier = new IBk(5)
//	2.0, 5.00, 2.0, 8.0, 20.0
	val classifier = new PriorClassifier(baseClassifier, Array(2.0, 5.00, 2.0, 8.0, 20.0))

	var tdfIdfFilter = null.asInstanceOf[StringToWordVector]
	var normelizeFilter = null.asInstanceOf[Normalize]
	var structure = null.asInstanceOf[Instances]


	private def initialFeautureCreation(data: Instances): Instances = {
		structure = new Instances(data,0,0)
		structure.clear()
		tdfIdfFilter = new StringToWordVector()
		tdfIdfFilter.setIDFTransform(false)
		tdfIdfFilter.setTFTransform(false)
		tdfIdfFilter.setDoNotOperateOnPerClassBasis(false)
		tdfIdfFilter.setWordsToKeep(20)
		tdfIdfFilter.setInputFormat(data)

		val filteredInstances = Filter.useFilter(data,tdfIdfFilter)

		val enumeration = tdfIdfFilter.getOutputFormat.enumerateAttributes()

		while(enumeration.hasMoreElements){
			val attribute = enumeration.nextElement().asInstanceOf[Attribute]
			println(attribute.toString)
		}

		normelizeFilter = new Normalize()
		normelizeFilter.setInputFormat(filteredInstances)

		val normilizedInstances = Filter.useFilter(filteredInstances,normelizeFilter)
		println("finished")
		filteredInstances
	}

	private def feautureCreation(instance: Instance): Instance = {

		val instances = new Instances(structure)
		structure.add(instance)
		instance.setDataset(structure)

		val filteredInstances = Filter.useFilter(structure,tdfIdfFilter)
		val normilizedInstances = Filter.useFilter(filteredInstances,normelizeFilter)

		val result = normilizedInstances.firstInstance()
		structure.clear()
		printInstance(result)
		result
	}

	private def printInstance(instance: Instance):Unit = {

		if(!instance.classIsMissing()){
			val classAttribute = instance.classAttribute()
			val className = classAttribute.value(instance.value(classAttribute).toInt)
			print(f"Class: $className - ")
		}
		val enumeration = instance.dataset().enumerateAttributes()
		while(enumeration.hasMoreElements){
			val attribute = enumeration.nextElement().asInstanceOf[Attribute]
			val value = instance.value(attribute)
			if(value!=0){
				val attributeName = attribute.name()
				print(f"Attribute: $attributeName%s: $value - ")
			}
		}
		println()
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
