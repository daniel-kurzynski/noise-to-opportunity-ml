package de.hpi.smm

import java.io.{File, FileWriter}

import au.com.bytecode.opencsv.CSVWriter
import com.blog_intelligence.nto.Document
import de.hpi.smm.Constants._
import de.hpi.smm.classification.ExtendedNTOClassifierBuilder
import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.nlp.NLP

import scala.collection.mutable

object EvaluateNTOClassifier {

	val postsFile = new File(POSTS_PATH)
	val brochuresFile = new File(BROCHURES_PATH)
	val classificationFile = new File(CLASSIFICATION_JSON)
	val stopWordsFile = new File(STOPWORDS_PATH)
	val posModelFile = new File(POSMODEL_PATH)
	val nlp = new NLP(stopWordsFile, posModelFile)
	val dataReader = new DataReader(postsFile, brochuresFile, classificationFile, nlp)

	val featureExtractorBuilder = new FeatureExtractorBuilder(dataReader)

	def main(args: Array[String]): Unit = {

		println("Validate NTO Classifier")
		validateNTOClassifier()

//		println("Most certain Posts")
//		mostCertainPosts()
	}


	def mostCertainPosts() = {
		val ntoClassifier = ExtendedNTOClassifierBuilder.build(
			classificationFile,
			brochuresFile,
			postsFile,
			stopWordsFile,
			posModelFile,
			includeNone = true
		)

		val allPosts = mutable.ArrayBuffer[Document]()
		ntoClassifier.dataReader.readPostsLinewise{post => allPosts += post}("category")

		List(
			"CRM"
			, "ECOM"
			, "HCM"
			, "LVM"
		).foreach { productClass =>
			println(s"========== Most certain posts for $productClass ==========")
			val predictedPosts = ntoClassifier.extractMostCertainPosts(20, productClass, allPosts)
			val writer = new FileWriter(new File(s"../n2o_data/most_certain/${productClass}_most_certain.csv"))

			predictedPosts.foreach { predictedPost=>
				writer.write(predictedPost.text + "\n")
				writer.write(s"=== product-prob: ${predictedPost.fullPrediction.productProb}, demand-prob: ${predictedPost.fullPrediction.demandProb}." + "\n")
				writer.write("=====================================================================================\n\n")
			}

			writer.close()
		}
	}

	def validateNTOClassifier() {
		val ntoClassifier =  ExtendedNTOClassifierBuilder.build(
			classificationFile,
			brochuresFile,
			postsFile,
			stopWordsFile,
			posModelFile,
			includeNone = true
		)

		ntoClassifier.validate()
	}
}
