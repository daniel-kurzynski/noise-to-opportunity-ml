package de.hpi.smm.classifierfoo

import java.io.File
import java.util.Random

import weka.classifiers.Evaluation
import weka.classifiers.functions.SMO
import weka.core.converters.CSVLoader
import weka.core.{Instance, Instances}
import weka.filters.Filter
import weka.filters.unsupervised.attribute.{NominalToString, Reorder, StringToWordVector}

import scala.collection.JavaConversions._

object ClassificationWeka {
	def main(args: Array[String]): Unit = {

		val trainingData = loadTrainingData()

		val stringToWordVectorFilter: StringToWordVector = new StringToWordVector
		stringToWordVectorFilter.setInputFormat(trainingData)
		val filteredData: Instances = Filter.useFilter(trainingData, stringToWordVectorFilter)

		val smo: SMO = new SMO
		smo.buildClassifier(filteredData)

		filteredData.enumerateInstances().zipWithIndex.foreach { case (instanceObject, index) =>
			val instance = instanceObject.asInstanceOf[Instance]
			val classifiedValue = smo.classifyInstance(instance)
			val instanceValue = instance.classValue()
			if(classifiedValue!=instanceValue)
				println(s"${index} Classifier: ${classifiedValue}, Real: ${instanceValue}")

		}

		val evaluation: Evaluation = new Evaluation(filteredData)
		evaluation.crossValidateModel(smo, filteredData, 10, new Random(1))
		System.out.println(evaluation.toClassDetailsString + evaluation.toMatrixString)
	}

	def loadTrainingData(): Instances ={
		val loader: CSVLoader = new CSVLoader
		loader.setSource(new File("data/brochures.csv"))

		val dataset: Instances = loader.getDataSet
		dataset.setClassIndex(2)

		val reorder: Reorder = new Reorder
		reorder.setAttributeIndicesArray(Array(2, 1))
		reorder.setInputFormat(dataset)

		val reoderedData: Instances = Filter.useFilter(dataset, reorder)

		val nominalToString: NominalToString = new NominalToString
		nominalToString.setInputFormat(reoderedData)

		return Filter.useFilter(reoderedData, nominalToString)
	}
}
