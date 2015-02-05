package de.hpi.smm

import java.io.{FileWriter, File}
import java.util.Random

import com.blog_intelligence.nto.Document
import de.hpi.smm.data_reader.DataReader
import weka.classifiers.`lazy`.IBk
import weka.classifiers.bayes.{NaiveBayesMultinomial, NaiveBayes}
import weka.classifiers.evaluation.output.prediction.PlainText
import weka.classifiers.functions.{MultilayerPerceptron, SMO, Logistic, VotedPerceptron}
import weka.classifiers.{Classifier, Evaluation}
import weka.classifiers.trees.J48
import weka.core.{Utils, DenseInstance, Attribute, Instances}
import scala.collection.mutable
import scala.collection.JavaConverters._

object ProductMain {

	def main(args: Array[String]): Unit = {
		val analyzer = new ProductAnalyzer()

		List(
		new Logistic
		, new SMO()
		, new MultilayerPerceptron()
//		, new NaiveBayes()
//		, new NaiveBayesMultinomial()
//		, new IBk(5)
//		, new IBk(15)
//			,
//			new HandcodedClassifier(analyzer.wordCountWithTfIdf, analyzer.featureWords)
		).foreach { classifier =>
//			classifier.
			analyzer.setClassifier(classifier)
			analyzer.buildTrainInstances()

			analyzer.buildClassifier()


			analyzer.validate()

			analyzer.printEvaluation()
		}
	}

	def writeBestPredictions(): Unit = {
//		val result = mutable.Map[Document, Array[Double]]()
//
//		analyzer.dataReader.readPostsLinewise { doc =>
//			result(doc) = analyzer.distributionForInstance(doc)
//		}("category")
//
//		for (i <- 0 to analyzer.wordCountWithTfIdf.size) {
//			val f = new FileWriter(new File(s"../ml_java/${analyzer.classAttr.value(i)}.csv"))
//			result.toArray.filter { case (doc, distribution) =>
//				distribution(i) == distribution.max
//			}.sortBy(-_._2(i)).take(100).foreach { case (doc, distribution) =>
//				f.write(doc.id + s": ${distribution(i)}" + "\n" + doc.wholeText + "\n\n")
//			}
//			f.close()
//		}
	}

}
