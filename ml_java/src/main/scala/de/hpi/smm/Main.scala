package de.hpi.smm

import java.io.{File, FileReader, FileWriter}
import java.util

import au.com.bytecode.opencsv.CSVWriter
import com.blog_intelligence.nto.Document
import com.lambdaworks.jacks.JacksMapper
import de.hpi.smm.classification.NTOAnalyzer
import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.domain._
import Constants._
import weka.classifiers.Evaluation
import weka.core.{Attribute, Instances}
import weka.filters.Filter

object Main {

	val FOR_ALL_POSTS = false

	val classifiedPosts = JacksMapper.readValue[Map[String, Map[String, Map[String, String]]]](
		new FileReader(CLASSIFICATION_JSON))
	val postsFile = new File("../n2o_data/linked_in_posts.csv")
	val brochuresFile = new File("../n2o_data/brochures.csv")

	val dataReader = new DataReader(classifiedPosts, postsFile, brochuresFile, FOR_ALL_POSTS)

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
		val ntoAnalyzer = new NTOAnalyzer(featureExtractorBuilder)
		ntoAnalyzer.trainDemand()
		val classNames = List("CRM", "ECOM", "HCM", "LVM", "None")
		ntoAnalyzer.trainProduct(classNames)

		val post = "I need help. I am looking for support. Thanks in advance. I am searching for a good crm software."
		val demandClassification = ntoAnalyzer.classifyDemand(post)
		println(s"$post is: ${demandClassification.cls} with propability: ${demandClassification.classificationOutput.prob}")

		val productClassification = ntoAnalyzer.classifyProduct(post)
		println(s"$post is: ${productClassification(0).cls} with propability: ${productClassification(0).classificationOutput.prob}")

		ntoAnalyzer.validate()
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
