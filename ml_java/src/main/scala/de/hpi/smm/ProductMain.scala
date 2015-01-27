package de.hpi.smm

import java.io.{File, FileReader}

import com.blog_intelligence.nto.Document
import com.lambdaworks.jacks.JacksMapper
import de.hpi.smm.data_reader.DataReader
import weka.core.{DenseInstance, Attribute, Instances}
import scala.collection.mutable
import scala.collection.JavaConverters._
import Constants._

object ProductMain {

	val FOR_ALL_POSTS = false

	val classifiedPosts = JacksMapper.readValue[Map[String, Map[String, Map[String, String]]]](
		new FileReader(CLASSIFICATION_JSON))
	val postsFile = new File("../n2o_data/linked_in_posts.csv")
	val brochuresFile = new File("../n2o_data/brochures.csv")

	val dataReader = new DataReader(classifiedPosts, postsFile, brochuresFile, FOR_ALL_POSTS)


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

		// todo: add class as last attribute
		val featureAttributes = determineFeatures(wordCountWithTfIdf)
		val classAttr = new Attribute("class", new java.util.ArrayList[String](classCount.keySet.asJava))

		featureAttributes.add(classAttr)
		val featureWords = featureAttributes.asScala.map(_.name()).zipWithIndex.toMap
		val instances = new Instances("", featureAttributes, featureAttributes.size())

		dataReader.readBrochuresLinewise(List("en")) { doc =>
			instances.add(new DenseInstance(1.0, constructFeatureValues(featureWords, doc, classAttr)))
		}





		wordCountWithTfIdf.foreach { case (className, counts) =>
			println(className)
			counts.toList.sortBy(-_._2).take(10).foreach(println)
		}
		println(classCount)
		println(N)
	}

	def constructFeatureValues(featureAttributes: Map[String, Int], doc: Document, classAttr: Attribute): Array[Double] ={
		val result = new Array[Double](featureAttributes.size)
		doc.textTokens.foreach { word =>
			val idx = featureAttributes(word)
			if(featureAttributes.contains(word))
				result(idx) += 1.0
		}
		result(result.size - 1) = classAttr.indexOfValue(doc.documentClass)
		result
	}

	def determineFeatures(wordCounts: mutable.Map[String, mutable.Map[String, Double]]): java.util.ArrayList[Attribute] = {
		var result = mutable.Set[Attribute]()
		wordCounts.foreach { case (className, counts) =>
			counts.toList.sortBy(-_._2).take(10).foreach { case (word, count) =>
				result += new Attribute(word)
			}
		}
		new java.util.ArrayList[Attribute](result.asJava)
	}
}
