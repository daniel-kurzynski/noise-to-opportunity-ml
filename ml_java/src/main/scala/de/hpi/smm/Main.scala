package de.hpi.smm

import java.io.{File, FileReader, FileWriter}

import au.com.bytecode.opencsv.CSVWriter
import com.lambdaworks.jacks.JacksMapper
import de.hpi.smm.classification.NTOAnalyzer
import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.domain._
import Constants._

object Main {

	val FOR_ALL_POSTS = false

	val classifiedPosts = JacksMapper.readValue[Map[String, Map[String, Map[String, String]]]](
		new FileReader(CLASSIFICATION_JSON))
	val postsFile = new File("../n2o_data/linked_in_posts.csv")
	val brochuresFile = new File("../n2o_data/brochures.csv")

	val dataReader = new DataReader(classifiedPosts, postsFile, brochuresFile, FOR_ALL_POSTS)

	val featureBuilder = new FeatureExtractorBuilder(dataReader)

	def main(args: Array[String]): Unit = {
//		println("Demand Feature Extraction")
//		runDemandFeatureExtraction()
//
//		println("Brochure Feature Extraction")
//		runBrochureFeatureExtraction()

		 println("Classify Post")
		 runClassifyPost()
	}

	def runClassifyPost() {
		val postClassifier = new NTOAnalyzer(featureBuilder)

		val demandPost = "I need help. I am looking for support. Thanks in advance."
		val demandClassification = postClassifier.classifyDemand(demandPost)
		println(s"$demandPost is: ${demandClassification.cls} with propability: ${demandClassification.classificationOutput.prob}")

		val evaluation = postClassifier.demandClassifier.crossValidate()
		println(evaluation.toSummaryString(f"%nResults%n======%n", false))
		println(evaluation.toMatrixString)
	}

	def runDemandFeatureExtraction(): Unit = {
		val features = featureBuilder.buildForDemand()
		val posts = featureBuilder.posts

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
			("CRM", 2.0, 5.0),
			("ECOM", 4.0, 100.0),
			("HCM", 3.0, 13.0),
			("LVM", 7.0, 35.0)
		).foreach { case (clsName, thresh1, thresh2) =>
			val features = featureBuilder.buildForBrochures(clsName, thresh1, thresh2)
			val brochures = featureBuilder.brochures
			val postForCategory = featureBuilder.postForCategory

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
