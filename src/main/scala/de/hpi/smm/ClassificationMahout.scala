package de.hpi.smm

import java.io.StringReader
import java.util

import com.github.tototoshi.csv.CSVReader
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.util.Version
import org.apache.mahout.classifier.sgd.{L1, OnlineLogisticRegression}
import org.apache.mahout.math.RandomAccessSparseVector
import org.apache.mahout.vectorizer.encoders.{Dictionary, ConstantValueEncoder, StaticWordValueEncoder}

import scala.collection.mutable
import scala.util.Random

class ClassificationMahout {

	// Define the size of the feature space
	val FEATURES = 10000

	// Classes
	val products = new Dictionary()

	// Feature encoders
	val encoder = new StaticWordValueEncoder("words")
	encoder.setProbes(2)
	val biasEncoder = new ConstantValueEncoder("constant")

	// Read entries from CSV
	val entries = {
		val csvReader = CSVReader.open("data/brochures.csv")
		val lines = csvReader.iterator.toList
		csvReader.close()
		lines
	}


	def classifyBrochures(): Unit = {
		initializeProductsDictionary()

		val learningAlgorithm =
			new OnlineLogisticRegression(
				4, FEATURES, new L1())
				.alpha(1).stepOffset(1000)
				.decayExponent(0.9)
				.lambda(3.0e-5)
				.learningRate(20)

		/*
		 * VECTORIZATION
		 */
		val vectors = buildFeatureFectors()

		/*
		 * TRAINING
		 */
		(1 to 5).foreach { _ =>
			Random.shuffle(vectors).foreach { case (v, clazzId) =>
				learningAlgorithm.train(clazzId, v)
			}
		}
		learningAlgorithm.close()

		/*
		 * EVALUATION
		 */
		vectors.foreach { case (v, clazzId) =>
			val res = learningAlgorithm.classifyFull(v)
			val maxIndex = res.maxValueIndex()
			println(s"${(maxIndex == clazzId).toString.toUpperCase}, Classification: $maxIndex, Actual: $clazzId")
		}
	}

	def buildFeatureFectors(): List[(RandomAccessSparseVector, Int)] = {
		val analyzer = new StandardAnalyzer(Version.LUCENE_46)
		entries.map { case Seq(id, text, clazz, _) =>
			// retrieve class id
			val clazzId = products.intern(clazz)

			val words = buildWordMap(analyzer, text)

			val v = new RandomAccessSparseVector(FEATURES)
			biasEncoder.addToVector(null, 1, v)
			//			encoder.addToVector(null, 50, v)
			words.foreach { case (word, count) =>
				encoder.addToVector(word, Math.log(1 + count), v)
			}
			(v, clazzId)
		}
	}

	def buildWordMap(analyzer: StandardAnalyzer, text: String): mutable.Map[String, Int] = {
		val words = mutable.Map[String, Int]()
		val ts = analyzer.tokenStream("body", new StringReader(text))
		val termAtt = ts.addAttribute(classOf[CharTermAttribute])
		ts.reset()
		while (ts.incrementToken()) {
			val termBuffer = termAtt.buffer()
			val termLen = termAtt.length()
			val w = new String(termBuffer, 0, termLen)
			words.get(w) match {
				case Some(c) => words += w -> (c + 1)
				case None => words += w -> 1
			}
		}
		ts.close()
		words
	}

	private def initializeProductsDictionary(): Unit = {
		val clazzes = entries.map { case Seq(_, _, clazz, _) => clazz }.toList.distinct
		println(clazzes)
		clazzes.foreach { clazz =>
			products.intern(clazz)
		}
	}

}
