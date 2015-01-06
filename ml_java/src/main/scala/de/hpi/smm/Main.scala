package de.hpi.smm

import java.io.{FileWriter, FileReader, File}
import java.util

import au.com.bytecode.opencsv.{CSVWriter, CSVReader}
import com.lambdaworks.jacks.JacksMapper
import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.domain._
import de.hpi.smm.feature_extraction.FeatureBuilder
import edu.stanford.nlp.ling.CoreAnnotations._
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.util.logging.RedwoodConfiguration
import scala.collection.JavaConverters._

import scala.collection.mutable


object Main {

	val FOR_ALL_POSTS = false

	val classifiedPosts = JacksMapper.readValue[Map[String, Map[String, Map[String, String]]]](
		new FileReader("../webapp_python/data/classification.json"))
	val postsFile = new File("../n2o_data/linked_in_posts.csv")
	val brochuresFile = new File("../n2o_data/brochures.csv")

	val dataReader = new DataReader(classifiedPosts, postsFile, brochuresFile, FOR_ALL_POSTS);

	//	val demandCounts = new DemandCountsCounter()
	//	var brochureCounts = new BrochuresCountsCounter()
	var genericCounter = new GenericCountsCounter()

	val blacklist = Array(
		".", ",", ":", "-RRB-", "-LRB-", "$",
		// english
		"IN", "DT", "TO", "CC", "VBZ",
		// german
		"APP", "ART", "KO", "KO", "PP",
		"PR", "PT", "TRUNC", "VA", "VM", "VV"
	)

	def main(args: Array[String]): Unit = {
		//println("Demand Feature Extraction")
		//runDemandFeatureExtraction()

		println("Brochure Feature Extraction")
		runBrochureFeatureExtraction()
	}

	def runDemandFeatureExtraction(): Unit = {
		genericCounter.smoothing = false

		val features = FeatureBuilder()
			.needWords(genericCounter, "demand", (5.0, 2.0))
			.questionNumber()
			.needNGrams()
			.containsEMail()
			.addressTheReader()
			.questionWords()
			.imperativeWords()

		dataReader.extractPostsLinewise { post =>
			features.touch(post)
			if (post.isClassified) {
				countTypes(post)
				countWords(post)
			}
		}()

		genericCounter.classCounts.remove("no-idea")

		val writer = new CSVWriter(new FileWriter(new File("../n2o_data/features.csv")),
			CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER)

		writer.writeNext(features.names)
		features.buildFeatureVector { (post, instance) =>
			val outputLine = buildLine(post, instance, "demand")
			writer.writeNext(outputLine)
		}
		writer.close()

		genericCounter.takeTopOccurrence("demand").take(10).foreach(println)
		println("----------------")
		genericCounter.takeTopNotOccurrence("demand").take(10).foreach(println)
	}

	def runBrochureFeatureExtraction(): Unit = {

		List(
			("CRM", 2.0, 4.0),
			("ECOM", 1.3, 3.7),
			("HCM", 2.3, 5.5),
			("LVM", 3.0, 5.5)
		).foreach { case (clsName, thresh1, thresh2) =>

			genericCounter = new GenericCountsCounter()
			genericCounter.smoothing = true

			val trainFeatures = FeatureBuilder()
				.needWords(genericCounter, clsName, (thresh1, thresh2))
				.questionNumber()
				.needNGrams()
				.containsEMail()
				.addressTheReader()
				.questionWords()
				.imperativeWords()


			// extract train features
			dataReader.extractBrochuresLinewise { brochure =>
				trainFeatures.touch(brochure)
				countTypes(brochure)
				countWords(brochure)
			}()

			val writer = new CSVWriter(new FileWriter(new File(s"../n2o_data/features_${clsName.toLowerCase}.csv")),
				CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER)

			writer.writeNext(trainFeatures.names)
			trainFeatures.buildFeatureVector { (post, instance) =>
				val outputLine = buildLine(post, instance, clsName)
				writer.writeNext(outputLine)
			}
			writer.close()


			// extract test features
			val testFeatures = FeatureBuilder()
				.needWords((genericCounter.takeTopOccurrence(clsName, thresh1).map(_._1) ++ genericCounter.takeTopNotOccurrence(clsName, thresh2).map(_._1)).toArray.distinct)
				.questionNumber()
				.needNGrams()
				.containsEMail()
				.addressTheReader()
				.questionWords()
				.imperativeWords()

			dataReader.extractPostsLinewise { post =>
				testFeatures.touch(post)
			}("category")


			val testWriter = new CSVWriter(new FileWriter(new File(s"../n2o_data/features_test_${clsName.toLowerCase}.csv")),
				CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER)

			testWriter.writeNext(testFeatures.names)
			testFeatures.buildFeatureVector { (post, instance) =>
				val outputLine = buildLine(post, instance, clsName)
				testWriter.writeNext(outputLine)
			}
			testWriter.close()

			println(s"=== $clsName ===")
			genericCounter.takeTopOccurrence(clsName, thresh1).foreach(println)
			println("======")
			genericCounter.takeTopNotOccurrence(clsName, thresh2).foreach(println)
		}

	}

	private def buildLine(post: Document, instance: Array[Double], currentClass: String): Array[String] = {
		val line = new Array[String](instance.size + 2)
		line(0) = post.id
		line(line.size - 1) = if (List(currentClass, "no-idea", null).contains(post.documentClass)) post.documentClass else "no-" + currentClass
		System.arraycopy(instance.map(_.toString), 0, line, 1, instance.size)
		line
	}


	private def countTypes(doc: Document): Unit = {
		genericCounter.classCounts(doc.documentClass) += 1
	}

	private def countWords(doc: Document): Unit = {
		doc.sentences.flatten.filter { word => !blacklist.exists(word.pos.startsWith)}.map(_.text).distinct.foreach { word =>
			if (!genericCounter.wordCounts.contains(word))
				genericCounter.wordCounts(word) = new mutable.HashMap[String, Int]().withDefaultValue(0)
			genericCounter.wordCounts(word)(doc.documentClass) += 1
		}
	}

}
