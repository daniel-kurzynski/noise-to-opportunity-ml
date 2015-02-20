package de.hpi.smm.classification

import java.io.File

import com.blog_intelligence.nto.{PredictedPost, FullPrediction, Document, NTOClassifier}
import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.nlp.NLP

import scala.collection.JavaConverters._
import scala.collection.mutable

case class ClassificationOutput(prob: Double, relevantFeatures: Array[Array[Any]] = Array())
case class ExtendedClassification(cls: String, classificationOutput: ClassificationOutput)

class ExtendedNTOClassifier(val dataReader: DataReader, stopWordsFile: File, posModelFile: File) extends NTOClassifier(stopWordsFile, posModelFile) {

	def validate():Unit = {
//		val demandEvaluation = demandClassifier.crossValidate()
//		println("=" * 80)
//		println("Demand")
//		println("=" * 80)
//		println(demandEvaluation.toSummaryString("", false))
////		println(demandEvaluation.pctCorrect())
//		println(demandEvaluation.toMatrixString)

		var posts = mutable.ArrayBuffer[Document]()
		dataReader.readPostsLinewise { post =>
			posts += post
		}("nto")

		println("=" * 80)
		println("Product")
		println("=" * 80)


		val productEvaluation = productClassifier.validate(posts)
		println(productEvaluation.toSummaryString("", false))
//		println(productEvaluation.pctCorrect())
		println(productEvaluation.toMatrixString)
	}


	def extractMostCertainPosts(num: Integer, productClass: String, posts: Seq[Document]) : List[PredictedPost] = {
		extractMostCertainPosts(num, productClass, posts.map(_.wholeText).asJava).asScala.toList
	}

	def predictDemandExtendedOutput(text: String): ExtendedClassification = {
		requireNonNull(demandClassifier)
		ExtendedClassification("demand", demandClassifier.classProbability(text))
	}
}
