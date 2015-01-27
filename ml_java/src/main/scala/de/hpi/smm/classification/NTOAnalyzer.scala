package de.hpi.smm.classification


import de.hpi.smm.FeatureExtractorBuilder
import weka.classifiers.Evaluation
import scala.collection.JavaConverters._

case class ClassificationOutput(prob: Double, relevantFeatures: Array[Array[Any]] = Array())
case class Classification(cls: String, classificationOutput: ClassificationOutput)

class NTOAnalyzer(featureExtractorBuilder: FeatureExtractorBuilder) {

	var demandClassifier: DemandClassifier = null
	var productClassifier: ProductClassifier = null
	val productConverter = new WekaInstancesConverter()

	def trainDemand(): Unit = {
		demandClassifier = new DemandClassifier("demand",
			featureExtractorBuilder.posts,
			featureExtractorBuilder.buildForDemand())
	}

	def trainProduct(classNames: List[String]): Unit = {
		productClassifier = new ProductClassifier()
		productConverter.fit(classNames)
		val instances = productConverter.convert("train",featureExtractorBuilder.brochures)
		productClassifier.buildClassifier(instances)
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
		}.toList
	}

	def classifyProductAsJavaList(text: String): java.util.List[Classification] = {
		classifyProduct(text).asJava

	}

	def validate():Unit = {
		val demandEvaluation = demandClassifier.crossValidate()
		println(demandEvaluation.toSummaryString(f"%nResults%n======%n", false))
		println(demandEvaluation.toMatrixString)


		val posts = featureExtractorBuilder.postForCategory
		val instances = productConverter.convert("posts", posts)

		val productEvaluation = new Evaluation(instances)
		println(demandEvaluation.toSummaryString(f"%nResults%n======%n", false))
		println(demandEvaluation.toMatrixString)
	}
}
