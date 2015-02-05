package de.hpi.smm

import java.io.{File, FileReader, FileWriter}

import au.com.bytecode.opencsv.CSVWriter
import com.blog_intelligence.nto.Document
import com.lambdaworks.jacks.JacksMapper
import de.hpi.smm.Constants._
import de.hpi.smm.classification.{ExtendedNTOClassifierBuilder, ExtendedNTOClassifier}
import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.Constants._

object Main {

	val postsFile = new File("../n2o_data/linked_in_posts.csv")
	val brochuresFile = new File("../n2o_data/brochures.csv")
	val classificationFile = new File(CLASSIFICATION_JSON)

	val dataReader = new DataReader(postsFile, brochuresFile,classificationFile)

	val featureExtractorBuilder = new FeatureExtractorBuilder(dataReader)

	def main(args: Array[String]): Unit = {
		//println("Demand Feature Extraction")
		//runDemandFeatureExtraction()

//		println("Brochure Feature Extraction")
//		runBrochureFeatureExtraction()

		 println("Classify Post")
		 runClassifyPost()
	}

	def runClassifyPost() {
		val ntoClassifier = ExtendedNTOClassifierBuilder.build(classificationFile,brochuresFile,postsFile)

		val post = "I need help. I am looking for support. Thanks in advance. I am searching for a good crm software."
		val post2 = "What's the best ECommerce Platform for product subscription sales (Continuity model)?  Is there a platform with a strong CRM at it's core?"
		val demandClassification = ntoClassifier.predictDemandExtendedOutput(post2)
		println(s"$post2 is: ${demandClassification.cls} with propability: ${demandClassification.classificationOutput.prob}")

		val productClassification = ntoClassifier.predictProduct(post2)

		println(s"$post2 is: ${productClassification.get(0).product} with propability: ${productClassification.get(0).prob}")

		ntoClassifier.validate()
	}

	def runDemandFeatureExtraction(): Unit = {
		val features = featureExtractorBuilder.buildForDemand()
		val posts = featureExtractorBuilder.posts

		val writer = new CSVWriter(new FileWriter(new File("../n2o_data/features.csv")),
			CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER)

		writer.writeNext(features.names)

		features.buildFeatureVectors(posts, { (post, instance) =>
			val outputLine = buildLine(post, instance, "demand")
			writer.writeNext(outputLine)
		})

		writer.close()

		features.takeTopOccurrence("demand").take(10).foreach(println)
		println("----------------")
		features.takeTopNotOccurrence("demand").take(10).foreach(println)
	}

	def runBrochureFeatureExtraction(): Unit = {
		List(
			("CRM", 1.1, 6.0),
			("ECOM", 1.8, 100.0),
			("HCM", 2.0, 10.0),
			("LVM", 3.4, 10.0)
		).foreach { case (clsName, thresh1, thresh2) =>
			val features = featureExtractorBuilder.buildForBrochures(clsName, thresh1, thresh2)
			val brochures = featureExtractorBuilder.brochures
			val postForCategory = featureExtractorBuilder.postForCategory

			val clsFeatures = new CSVWriter(new FileWriter(new File(s"../n2o_data/features_${clsName.toLowerCase}.csv")),
				CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER)

			clsFeatures.writeNext(features.names)
			features.buildFeatureVectors(brochures, { (post, instance) =>
				val outputLine = buildLine(post, instance, clsName)
				clsFeatures.writeNext(outputLine)
			})
			clsFeatures.close()


			val testWriter = new CSVWriter(new FileWriter(new File(s"../n2o_data/features_test_${clsName.toLowerCase}.csv")),
				CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER)

			testWriter.writeNext(features.names)
				features.buildFeatureVectors(postForCategory, { (post, instance) =>
				val outputLine = buildLine(post, instance, clsName, false)
				testWriter.writeNext(outputLine)
			})

			testWriter.close()

			println(s"=== $clsName ===")
			features.takeTopOccurrence(clsName, thresh1).foreach(println)
			println("======")
			features.takeTopNotOccurrence(clsName, thresh2).foreach(println)
		}

	}

	private def buildLine(post: Document, instance: Array[Double], currentClass: String, emptyClass: Boolean = false): Array[String] = {
		val line = new Array[String](instance.size + 2)
		line(0) = post.id
		if (emptyClass){
			line(line.size - 1) = ""//if (List(currentClass, "no-idea", null).contains(post.documentClass)) post.documentClass else "no-" + currentClass
		} else {
			line(line.size - 1) = if (List(currentClass, "no-idea", null).contains(post.documentClass)) post.documentClass else "no-" + currentClass
		}

		System.arraycopy(instance.map(_.toString), 0, line, 1, instance.size)
		line
	}

}
