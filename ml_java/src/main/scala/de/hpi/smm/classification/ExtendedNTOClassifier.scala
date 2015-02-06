package de.hpi.smm.classification

import com.blog_intelligence.nto.{Document, NTOClassifier}
import de.hpi.smm.data_reader.DataReader
import scala.collection.mutable

case class ClassificationOutput(prob: Double, relevantFeatures: Array[Array[Any]] = Array())
case class ExtendedClassification(cls: String, classificationOutput: ClassificationOutput)

class ExtendedNTOClassifier(dataReader: DataReader) extends NTOClassifier {

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


	def predictDemandExtendedOutput(text: String): ExtendedClassification = {
		requireNonNull(demandClassifier)
		ExtendedClassification("demand", demandClassifier.classProbability(text))
	}
}
