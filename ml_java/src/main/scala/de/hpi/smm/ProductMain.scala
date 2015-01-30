package de.hpi.smm

import java.io.File
import java.util.Random

import com.blog_intelligence.nto.Document
import de.hpi.smm.classification.HandcodedClassifier
import de.hpi.smm.data_reader.DataReader
import weka.classifiers.evaluation.output.prediction.PlainText
import weka.classifiers.{Classifier, Evaluation}
import weka.classifiers.trees.J48
import weka.core.{DenseInstance, Attribute, Instances}
import scala.collection.mutable
import scala.collection.JavaConverters._

class ProductAnalyzer() {

	private val dataReader = new DataReader(
		new File("../n2o_data/linked_in_posts.csv"),
		new File("../n2o_data/brochures.csv"))

	private val classCount = mutable.Map[String, Int]().withDefaultValue(0)
	private var wordCountWithTfIdf = mutable.Map[String, mutable.Map[String, Double]]()

	private var featureAttributes: java.util.ArrayList[Attribute] = null
	var featureWords: Map[String, Int] = null
	private var classAttr: Attribute = null

	private var classifier: Classifier = null
	private var evaluation: Evaluation = null

	private var trainInstances: Instances = null
	private var testInstances: Instances = null

	init()

	private def init() = {
		val wordCount = mutable.Map[String, mutable.Map[String, Int]]()
		val documentCount = mutable.Map[String, Int]().withDefaultValue(0)
		var N = 0

		dataReader.readBrochuresLinewise(List("en")) { doc =>
			val docClass = doc.documentClass

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

		wordCountWithTfIdf = wordCount.map { case (className, counts) =>
			(className, counts.map { case (word, count) =>
				(word, count.toDouble * Math.log(N.toDouble / documentCount(word).toDouble))
			})
		}

		featureWords = determineFeatures(wordCountWithTfIdf).zipWithIndex.toMap
		println(featureWords)
		classAttr = new Attribute("@@class@@", new java.util.ArrayList[String](wordCountWithTfIdf.keySet.asJava))
		println(classAttr)

		featureAttributes = new java.util.ArrayList[Attribute](featureWords.keys.map(new Attribute(_)).asJavaCollection)
		featureAttributes.add(classAttr)
	}

	def setClassifier(classifier: Classifier): Unit = {
		this.classifier = classifier
	}

	private def determineFeatures(wordCounts: mutable.Map[String, mutable.Map[String, Double]]): Array[String] = {
		var result = mutable.Set[String]()
		wordCounts.foreach { case (className, counts) =>
			counts.toList.sortBy(-_._2).take(10).foreach { case (word, _) =>
				result += word
			}
		}
		result.toArray
	}

	private def constructFeatureValues(featureAttributes: Map[String, Int], doc: Document, classAttr: Attribute): Array[Double] = {
		val result = new Array[Double](featureAttributes.size + 1)
		doc.textTokens.foreach { word =>
			if(featureAttributes.contains(word))
				result(featureAttributes(word)) = 1.0
		}
		result(result.size - 1) = classAttr.indexOfValue(doc.documentClass)
		result
	}


	def train() : Unit = {
		trainInstances = new Instances("train", featureAttributes, featureAttributes.size())
		trainInstances.setClassIndex(featureAttributes.size() - 1)
		dataReader.readBrochuresLinewise(List("en")) { doc =>
			trainInstances.add(new DenseInstance(1.0, constructFeatureValues(featureWords, doc, classAttr)))
		}
		evaluation = new Evaluation(trainInstances)
	}

	def buildClassifier(): Unit = {
		classifier.buildClassifier(trainInstances)
	}

	private def readTestInstances() : Unit = {

		testInstances = new Instances("test", featureAttributes, featureAttributes.size())
		testInstances.setClassIndex(featureAttributes.size() - 1)
		dataReader.readPostsLinewise { doc =>
			testInstances.add(new DenseInstance(1.0, constructFeatureValues(featureWords, doc, classAttr)))
		}("category")
	}

	def validate(): Unit = {
		train()
		readTestInstances()
		evaluation.evaluateModel(classifier, testInstances)

	}

	def crossValidate(): PlainText = {
		readTestInstances()
		val result = new PlainText()
		result.setBuffer(new StringBuffer())
		result.setOutputDistribution(true)

		evaluation.crossValidateModel(classifier, trainInstances, 10, new Random(18), result)

		result
	}

	def printEvaluation() : Unit  = {
		println(evaluation.toSummaryString(f"%nResults%n======%n", false))
		println(evaluation.toMatrixString)
	}

}

object ProductMain {

	def main(args: Array[String]): Unit = {
//		val wordCountWithTfIdf = countFeatureWords()
//		val (trainInstances: Instances, testInstances: Instances) = buildInstances(wordCountWithTfIdf)
//
//		val classifiers = List(new J48(), new HandcodedClassifier())
//		classifiers.foreach { classifier =>
//			val evaluation = new Evaluation(trainInstances)
//
//			classifier.buildClassifier(trainInstances)
//			evaluation.evaluateModel(classifier, testInstances)
//			println(classifier.toString)
//			println(evaluation.toSummaryString(f"%nResults%n======%n", false))
//			println(evaluation.toMatrixString)
//		}
		val analyzer = new ProductAnalyzer()

		List(new J48, new HandcodedClassifier(analyzer.featureWords)).foreach { classifier =>
			analyzer.setClassifier(classifier)
			analyzer.train()

			val cross = false
			if (cross) {
				val results = analyzer.crossValidate()
//				println(results.getBuffer)
			} else {
				analyzer.buildClassifier()
				analyzer.validate()
			}

			analyzer.printEvaluation()
		}
	}

}
