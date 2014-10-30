package de.hpi.smm

import java.io.File
import java.util.Random

import weka.classifiers.Evaluation
import weka.classifiers.functions.SMO
import weka.core.{Instance, Instances}
import weka.core.converters.CSVLoader
import weka.filters.Filter
import weka.filters.unsupervised.attribute.StringToWordVector
import scala.collection.JavaConversions._

object ClassificationWeka {
	def main(args: Array[String]): Unit = {
		val loader = new CSVLoader()
		loader.setSource(new File("data/brochures.csv"))
		val dataSet = loader.getDataSet
		dataSet.setClassIndex(2)
		val filter = new StringToWordVector()
		filter.setInputFormat(dataSet)
		val filteredData: Instances = Filter.useFilter(dataSet, filter);

		val classifier = new SMO()
		val classifier2 = new SMO();
		classifier2.buildClassifier(filteredData);

		filteredData.enumerateInstances().zipWithIndex.foreach { case (instanceObject, index) =>
			val instance = instanceObject.asInstanceOf[Instance]
			val classifiedValue = classifier2.classifyInstance(instance)
			val instanceValue = instance.classValue()
			if(classifiedValue != instanceValue)
			println(s"${index} Classifier: ${classifiedValue}, Real: ${instanceValue}")

		}
		val evaluation = new Evaluation(filteredData);
		evaluation.crossValidateModel(classifier, filteredData, 10, new Random(1));

		println(evaluation.toClassDetailsString()+evaluation.toMatrixString());
	}
}
