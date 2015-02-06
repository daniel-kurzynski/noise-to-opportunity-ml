package de.hpi.smm.classification


import com.blog_intelligence.nto.{Document, NTOClassifier}
import de.hpi.smm.data_reader.DataReader
import scala.collection.mutable

case class ClassificationOutput(prob: Double, relevantFeatures: Array[Array[Any]] = Array())
case class ExtendedClassification(cls: String, classificationOutput: ClassificationOutput)

class ExtendedNTOClassifier(val dataReader: DataReader) extends NTOClassifier{

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

	def extractMostCertainPosts(num: Integer, productClass: String, posts: Seq[Document]) : List[(Document, FullPrediction)] = {

		val predictions = mutable.Map[Document, FullPrediction]()
		posts.foreach { post =>
			val demandPrediction = demandClassifier.classProbability(post.wholeText)
			val productPrediction = productClassifier.predict(post.wholeText).maxBy(_.prob)
			predictions(post) = FullPrediction(demandPrediction.prob, productPrediction.product, productPrediction.prob)
		}


		def harmonicMean(prediction: FullPrediction): Double = {
			(2 * prediction.demandProb * prediction.productProb) / (prediction.demandProb + prediction.productProb)
		}

		predictions.toList
			.filter {case (doc, prediction) => prediction.productClass == productClass}
			.filter {case (doc, prediction) => harmonicMean(prediction) > 0.6}
			.sortBy {case (doc, prediction) => -harmonicMean(prediction)}
			.take(num)
	}

	def predictDemandExtendedOutput(text: String): ExtendedClassification = {
		requireNonNull(demandClassifier)
		ExtendedClassification("demand", demandClassifier.classProbability(text))
	}
}
