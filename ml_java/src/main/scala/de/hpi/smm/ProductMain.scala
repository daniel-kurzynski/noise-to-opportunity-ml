package de.hpi.smm

import java.io.{File, FileReader}

import com.lambdaworks.jacks.JacksMapper
import de.hpi.smm.data_reader.DataReader
import scala.collection.mutable
import Constants._

object ProductMain {

	val FOR_ALL_POSTS = false

	val classifiedPosts = JacksMapper.readValue[Map[String, Map[String, Map[String, String]]]](
		new FileReader(CLASSIFICATION_JSON))
	val postsFile = new File("../n2o_data/linked_in_posts.csv")
	val brochuresFile = new File("../n2o_data/brochures.csv")

	val dataReader = new DataReader(classifiedPosts, postsFile, brochuresFile, FOR_ALL_POSTS)

	val featureBuilder = new FeatureExtractorBuilder(dataReader)

	def main(args: Array[String]): Unit = {
		val classCount = mutable.Map[String, Int]().withDefaultValue(0)
		val wordCount = mutable.Map[String, mutable.Map[String, Int]]()
		val documentCount = mutable.Map[String, Int]().withDefaultValue(0)
		var N = 0

		dataReader.readBrochuresLinewise(List("en")) { doc =>
			val docClass = doc.documentClass
			classCount(docClass) += 1

			if (!wordCount.contains(docClass))
				wordCount(docClass) = mutable.Map[String, Int]().withDefaultValue(0)
			doc.textTokens.foreach { word =>
				wordCount(docClass)(word) += 1
			}
			doc.textTokens.toSet[String].foreach { word =>
				documentCount(word) += 1
			}
			N += 1
		}

		val wordCountWithTfIdf = wordCount.map { case (className, counts) =>
			(className, counts.map { case (word, count) =>
				(word, count.toDouble * Math.log(N.toDouble / documentCount(word).toDouble))
			})
		}

		wordCountWithTfIdf.foreach { case (className, counts) =>
			println(className)
			counts.toList.sortBy(-_._2).take(10).foreach(println)
		}
		println(classCount)
		println(N)
	}
}
