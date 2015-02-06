package de.hpi.smm.classification

import com.blog_intelligence.nto.{Document, NTOClassifier}
import de.hpi.smm.data_reader.DataReader
import scala.collection.mutable
import scala.collection.JavaConverters._

case class ClassificationOutput(prob: Double, relevantFeatures: Array[Array[Any]] = Array())
case class ExtendedClassification(cls: String, classificationOutput: ClassificationOutput)

class ExtendedNTOClassifier(val dataReader: DataReader) extends NTOClassifier {

	def validate():Unit = {
		val demandEvaluation = demandClassifier.crossValidate()
		println(demandEvaluation.toSummaryString(f"%nResults%n======%n", false))
		println(demandEvaluation.toMatrixString)

		var posts = mutable.ArrayBuffer[Document]()
		dataReader.readPostsLinewise { post =>
			posts += post
		}("category")

		productClassifier.printValidation(posts.toList)
	}

	case class FullPrediction(demandProb: Double, productClass: String, productProb: Double)

	def extractMostCertainPosts(num: Integer, productClass: String, posts: java.util.List[String]): java.util.List[(String, FullPrediction)] = {

		val predictions = mutable.Map[String, FullPrediction]()

		posts.asScala.foreach { post =>
			val demandPrediction = demandClassifier.classProbability(post)
			val productPrediction = productClassifier.predict(post).maxBy(_.prob)
			predictions(post) = FullPrediction(demandPrediction.prob, productPrediction.product, productPrediction.prob)
		}


		def harmonicMean(prediction: FullPrediction, measure: Double = 1): Double = {
			((1 + math.pow(measure, 2)) * prediction.demandProb * prediction.productProb) / (math.pow(measure, 2) * prediction.demandProb + prediction.productProb)
		}

		predictions.toList
			.filter {case (doc, prediction) => prediction.productClass == productClass}
			.filter {case (doc, prediction) => harmonicMean(prediction, 2) > 0.6}
			.sortBy {case (doc, prediction) => -harmonicMean(prediction, 2)}
			.take(num).asJava
	}

	def extractMostCertainPosts(num: Integer, productClass: String, posts: Seq[Document]) : List[(String, FullPrediction)] = {

		extractMostCertainPosts(num, productClass, posts.map(_.wholeText).asJava).asScala.toList
	}

	def predictDemandExtendedOutput(text: String): ExtendedClassification = {
		requireNonNull(demandClassifier)
		ExtendedClassification("demand", demandClassifier.classProbability(text))
	}
}
