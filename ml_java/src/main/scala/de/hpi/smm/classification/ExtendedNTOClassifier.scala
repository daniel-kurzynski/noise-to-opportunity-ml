package de.hpi.smm.classification

import com.blog_intelligence.nto.{PredictedPost, FullPrediction, Document, NTOClassifier}
import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.nlp.NLP

import scala.collection.JavaConverters._
import scala.collection.mutable

case class ClassificationOutput(prob: Double, relevantFeatures: Array[Array[Any]] = Array())
case class ExtendedClassification(cls: String, classificationOutput: ClassificationOutput)

class ExtendedNTOClassifier(val dataReader: DataReader, nlp: NLP) extends NTOClassifier(nlp) {

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


	def extractMostCertainPosts(num: Integer, productClass: String, posts: Seq[Document]) : List[PredictedPost] = {
		extractMostCertainPosts(num, productClass, posts.map(_.wholeText).asJava).asScala.toList
	}

	def predictDemandExtendedOutput(text: String): ExtendedClassification = {
		requireNonNull(demandClassifier)
		ExtendedClassification("demand", demandClassifier.classProbability(text))
	}
}
