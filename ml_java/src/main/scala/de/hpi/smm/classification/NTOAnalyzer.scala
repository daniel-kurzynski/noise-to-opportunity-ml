package de.hpi.smm.classification


import java.util.Random

import de.hpi.smm.FeatureExtractorBuilder
import weka.classifiers.Evaluation
import weka.core.Instances

import scala.collection.JavaConverters._

case class ClassificationOutput(prob: Double, relevantFeatures: Array[Array[Any]] = Array())
case class Classification(cls: String, classificationOutput: ClassificationOutput)

class NTOAnalyzer(featureExtractorBuilder: FeatureExtractorBuilder) {

	var demandClassifier: DemandClassifier = null
	var productClassifier: ProductClassifier = null
	val productConverter = new WekaInstancesConverter()
	var testInstances : Instances = null

	def trainDemand(): Unit = {
		demandClassifier = new DemandClassifier("demand",
			featureExtractorBuilder.posts,
			featureExtractorBuilder.buildForDemand())
	}

	def trainProduct(classNames: List[String]): Unit = {
		productClassifier = new ProductClassifier()
		productConverter.fit(classNames)
		val brochureInstances = productConverter.convert("train",featureExtractorBuilder.brochures)

		val posts = featureExtractorBuilder.postForCategory
		val postInstances = productConverter.convert("posts", posts)

		postInstances.randomize(new Random(0))

		val numberTestInstances = math.min(postInstances.size(),10)
		val numberTrainInstances = math.max(0,postInstances.size()-numberTestInstances)

		//brochureInstances.addAll(new Instances(postInstances,1,numberTrainInstances))
		//testInstances = new Instances(postInstances,numberTrainInstances+1,postInstances.size())
		testInstances = postInstances

		productClassifier.buildClassifier(brochureInstances)
	}

	def classifyDemand(text: String): Classification = {
		Classification("demand", demandClassifier.classProbability(text))
	}

	/**
	 * Returns an ordered list of classifications.
	 */
	def classifyProduct(text: String): List[Classification] = {
		val instance = productConverter.buildPostInstance(text, "None")

		val dist = productClassifier.distributionForInstance(instance)

		dist.zipWithIndex.map { case (value, index) =>
			val className = productConverter.classAttribute.value(index)
			Classification(className, ClassificationOutput(value, new Array[Array[Any]](0)))
		}.toList.sortBy(-_.classificationOutput.prob)
	}

	def classifyProductAsJavaList(text: String): java.util.List[Classification] = {
		classifyProduct(text).asJava

	}

	def validate():Unit = {
		val demandEvaluation = demandClassifier.crossValidate()
		println(demandEvaluation.toSummaryString(f"%nResults%n======%n", false))
		println(demandEvaluation.toMatrixString)


		val productEvaluation = new Evaluation(testInstances)
		productEvaluation.evaluateModel(productClassifier,testInstances)
		println(productEvaluation.toSummaryString(f"%nResults%n======%n", false))
		println(productEvaluation.toMatrixString)
	}
}
