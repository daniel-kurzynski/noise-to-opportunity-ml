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
		var i = 0
		dataReader.readBrochuresLinewise(List("en")) { doc =>
			val docClass = doc.documentClass
			classCount(docClass) += 1

			if (!wordCount.contains(docClass))
				wordCount(docClass) = mutable.Map[String, Int]().withDefaultValue(0)
			doc.textTokens.foreach { word =>
				wordCount(docClass)(word) += 1

			}
			i += 1
		}
		wordCount.foreach { case (className, counts) =>
			println(className)
			counts.toList.sortBy(-_._2).take(10).foreach(println)
		}
		println(classCount)
		println(i)
	}
}
