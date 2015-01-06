package de.hpi.smm

import java.io.{File, FileReader, FileWriter}

import au.com.bytecode.opencsv.CSVWriter
import com.lambdaworks.jacks.JacksMapper
import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.domain._
import de.hpi.smm.feature_extraction.FeatureExtractor

object Main {

	val FOR_ALL_POSTS = true

	val classifiedPosts = JacksMapper.readValue[Map[String, Map[String, Map[String, String]]]](
		new FileReader("../webapp_python/data/classification.json"))
	val postsFile = new File("../n2o_data/linked_in_posts.csv")
	val brochuresFile = new File("../n2o_data/brochures.csv")

	val dataReader = new DataReader(classifiedPosts, postsFile, brochuresFile, FOR_ALL_POSTS);

	def main(args: Array[String]): Unit = {
		println("Demand Feature Extraction")
		runDemandFeatureExtraction()

//		println("Brochure Feature Extraction")
//		runBrochureFeatureExtraction()
	}

	def runDemandFeatureExtraction(): Unit = {

		val smoothing = false;

		val features = new FeatureExtractor(smoothing)
			.needWords("demand", (5.0, 2.0))
			.questionNumber()
			.needNGrams()
			.containsEMail()
			.addressTheReader()
			.questionWords()
			.imperativeWords()

		dataReader.readPostsLinewise { post =>
			features.touch(post)
		}()
		features.finishTraining()

		features.removeClassCounts("no-idea")

		val writer = new CSVWriter(new FileWriter(new File("../n2o_data/features.csv")),
			CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER)

		writer.writeNext(features.names)
		features.buildFeatureVector { (post, instance) =>
			val outputLine = buildLine(post, instance, "demand")
			writer.writeNext(outputLine)
		}
		writer.close()

		features.takeTopOccurrence("demand").take(10).foreach(println)
		println("----------------")
		features.takeTopNotOccurrence("demand").take(10).foreach(println)
	}

	def runBrochureFeatureExtraction(): Unit = {

		List(
			("CRM", 2.0, 4.0),
			("ECOM", 1.3, 3.7),
			("HCM", 2.3, 5.5),
			("LVM", 3.0, 5.5)
		).foreach { case (clsName, thresh1, thresh2) =>

			val smoothing = true;

			val features = new FeatureExtractor(smoothing)
				.needWords(clsName, (thresh1, thresh2))
				.needNGrams()


			// extract train features
			dataReader.readBrochuresLinewise { brochure =>
				features.touch(brochure)
			}()

			features.finishTraining()

			val writer = new CSVWriter(new FileWriter(new File(s"../n2o_data/features_${clsName.toLowerCase}.csv")),
				CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER)

			writer.writeNext(features.names)
			features.buildFeatureVector { (post, instance) =>
				val outputLine = buildLine(post, instance, clsName)
				writer.writeNext(outputLine)
			}
			writer.close()

			features.documents = List()

			dataReader.readPostsLinewise { post =>
				features.touch(post)
			}("category")


			val testWriter = new CSVWriter(new FileWriter(new File(s"../n2o_data/features_test_${clsName.toLowerCase}.csv")),
				CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER)

			testWriter.writeNext(features.names)
			features.buildFeatureVector { (post, instance) =>
				val outputLine = buildLine(post, instance, clsName)
				testWriter.writeNext(outputLine)
			}
			testWriter.close()

			println(s"=== $clsName ===")
			features.takeTopOccurrence(clsName, thresh1).take(3).foreach(println)
			println("======")
			features.takeTopNotOccurrence(clsName, thresh2).take(3).foreach(println)
		}

	}

	private def buildLine(post: Document, instance: Array[Double], currentClass: String): Array[String] = {
		val line = new Array[String](instance.size + 2)
		line(0) = post.id
		line(line.size - 1) = if (List(currentClass, "no-idea", null).contains(post.documentClass)) post.documentClass else "no-" + currentClass
		System.arraycopy(instance.map(_.toString), 0, line, 1, instance.size)
		line
	}

}
