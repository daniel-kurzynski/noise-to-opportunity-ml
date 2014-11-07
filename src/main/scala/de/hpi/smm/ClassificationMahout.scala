package de.hpi.smm

import java.util

import com.github.tototoshi.csv.CSVReader
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.util.Version
import org.apache.mahout.classifier.sgd.{L1, OnlineLogisticRegression}
import org.apache.mahout.vectorizer.encoders.{Dictionary, ConstantValueEncoder, StaticWordValueEncoder}

class ClassificationMahout {

	val FEATURES = -1

	def classify(): Unit = {
		val traceDictionary = new util.TreeMap[String, util.Set[Integer]]()

		val encoder = new StaticWordValueEncoder("body")
		encoder.setProbes(2)
		encoder.setTraceDictionary(traceDictionary)

		val bias = new ConstantValueEncoder("intercept")
		bias.setTraceDictionary(traceDictionary)

		val words = new ConstantValueEncoder("words")
		words.setTraceDictionary(traceDictionary)

		val brochures = new Dictionary()
		val analyzer = new StandardAnalyzer(Version.LUCENE_46)

		val csvReader = CSVReader.open("data/brochures.csv")
		val lines = csvReader.iterator
		lines.foreach { line =>
			println(line)
		}
		csvReader.close()

		val learningAlgorithm =
			new OnlineLogisticRegression(
				5, FEATURES, new L1())
				.alpha(1).stepOffset(1000)
				.decayExponent(0.9)
				.lambda(3.0e-5)
				.learningRate(20);
	}

}
