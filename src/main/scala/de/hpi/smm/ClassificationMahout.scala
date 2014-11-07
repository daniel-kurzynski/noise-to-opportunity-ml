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

	val FEATURES = 10000
	val products = new Dictionary()
	val entries = {
		val csvReader = CSVReader.open("data/brochures.csv")
		val lines = csvReader.iterator.toList
		csvReader.close()
		lines
	}

	def classify(): Unit = {
		val traceDictionary = new util.TreeMap[String, util.Set[Integer]]()

		val encoder = new StaticWordValueEncoder("body")
		encoder.setProbes(2)
		encoder.setTraceDictionary(traceDictionary)

		val biasEncoder = new ConstantValueEncoder("intercept")
		biasEncoder.setTraceDictionary(traceDictionary)

		val analyzer = new StandardAnalyzer(Version.LUCENE_46)

		initializeProductsDictionary()

		val learningAlgorithm =
			new OnlineLogisticRegression(
				4, FEATURES, new L1())
				.alpha(1).stepOffset(1000)
				.decayExponent(0.9)
				.lambda(3.0e-5)
				.learningRate(20)

		val vectors = entries.map { case Seq(id, text, clazz, _) =>
			val clazzId = products.intern(clazz)
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

			val v = new RandomAccessSparseVector(FEATURES)
			biasEncoder.addToVector(null, 1, v)
//			encoder.addToVector(null, 50, v)
			words.foreach { case (word, count) =>
				encoder.addToVector(word, Math.log(1 + count), v)
			}
			(v, clazzId)
		}

		Random.shuffle(vectors).foreach { case (v, clazzId) =>
			learningAlgorithm.train(clazzId, v)
		}
		learningAlgorithm.close()

		vectors.foreach { case (v, clazzId) =>
			val res = learningAlgorithm.classify(v)
			val it = res.all().iterator()

			val values = new Array[Double](4)
			var i = 1
			while (it.hasNext) {
				values(i) = it.next.get()
				i+= 1
			}
			values(0) = 1 - values.sum

			val maxIndex = values.zipWithIndex.maxBy(_._1)._2
			println(s"Result: $maxIndex, Actual: $clazzId")
		}
	}

	private def initializeProductsDictionary(): Unit = {
		val clazzes = entries.map { case Seq(_, _, clazz, _) => clazz }.toList.distinct
		println(clazzes)
		clazzes.foreach { clazz =>
			products.intern(clazz)
		}
	}

}
