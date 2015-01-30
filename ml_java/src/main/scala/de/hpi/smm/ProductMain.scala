package de.hpi.smm

import java.io.File
import java.util
import java.util.Random

import com.blog_intelligence.nto.Document
import de.hpi.smm.data_reader.DataReader
import weka.classifiers.Evaluation
import weka.classifiers.trees.J48
import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.evaluation.output.prediction.PlainText
import weka.core.{DenseInstance, Attribute, Instances}
import scala.collection.mutable
import scala.collection.JavaConverters._

object ProductMain {

	val postsFile = new File("../n2o_data/linked_in_posts.csv")
	val brochuresFile = new File("../n2o_data/brochures.csv")

	val dataReader = new DataReader(postsFile, brochuresFile)

	def main(args: Array[String]): Unit = {
		val wordCountWithTfIdf = countFeatureWords()
		val (trainInstances: Instances, testInstances: Instances) = buildInstances(wordCountWithTfIdf)

		val classifier = new J48()
		val evaluation = new Evaluation(trainInstances)

		classifier.buildClassifier(trainInstances)
		evaluation.evaluateModel(classifier, testInstances)
		println(classifier.toString)
		println(evaluation.toSummaryString(f"%nResults%n======%n", false))
		println(evaluation.toMatrixString)
	}

	def countFeatureWords(): mutable.Map[String, mutable.Map[String, Double]] = {
		val classCount    = mutable.Map[String, Int]().withDefaultValue(0)
		val wordCount     = mutable.Map[String, mutable.Map[String, Int]]()
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
//		wordCountWithTfIdf.foreach { case (className, counts) =>
//			println(className)
//			counts.toList.sortBy(-_._2).take(10).foreach(println)
//		}
//		println(classCount)
//		println(N)

//		classCount("None") = 0
		wordCountWithTfIdf
	}

	def buildInstances(wordCountWithTfIdf: mutable.Map[String, mutable.Map[String, Double]]): (Instances, Instances) = {
		val featureWords = determineFeatures(wordCountWithTfIdf).zipWithIndex.toMap
		println(s"Feature-Words: $featureWords")
		val featureAttributes = new util.ArrayList[Attribute](featureWords.keys.map(new Attribute(_)).asJavaCollection)
		val classAttr = new Attribute("@@class@@", new util.ArrayList[String](wordCountWithTfIdf.keySet.asJava))
		featureAttributes.add(classAttr)

		val trainInstances = new Instances("train", featureAttributes, featureAttributes.size())
		val testInstances = new Instances("test", featureAttributes, featureAttributes.size())
		trainInstances.setClassIndex(featureAttributes.size() - 1)
		testInstances.setClassIndex(featureAttributes.size() - 1)

		dataReader.readBrochuresLinewise(List("en")) { doc =>
			trainInstances.add(new DenseInstance(1.0, constructFeatureValues(featureWords, doc, classAttr)))
		}

		dataReader.readPostsLinewise { doc =>
			testInstances.add(new DenseInstance(1.0, constructFeatureValues(featureWords, doc, classAttr)))
		}("category")
		(trainInstances, testInstances)
	}

	def constructFeatureValues(featureAttributes: Map[String, Int], doc: Document, classAttr: Attribute): Array[Double] ={
		val result = new Array[Double](featureAttributes.size + 1)
		doc.textTokens.foreach { word =>
			if (featureAttributes.contains(word))
				result(featureAttributes(word)) = 1.0
		}
		result(result.size - 1) = classAttr.indexOfValue(doc.documentClass)
		result
	}

	def determineFeatures(wordCounts: mutable.Map[String, mutable.Map[String, Double]]): Array[String] = {
		var result = mutable.Set[String]()
		wordCounts.foreach { case (className, counts) =>
			counts.toList.sortBy(-_._2).take(10).foreach { case (word, _) =>
				result += word
			}
		}
		result.toArray
	}
}
