package de.hpi.smm

import java.io.File
import de.hpi.smm.Constants._
import com.blog_intelligence.nto.Document
import de.hpi.smm.classification.{TheirClassifier, ProductClassifier}
import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.domain.Word
import weka.classifiers.functions.{MultilayerPerceptron, SMO, Logistic}
import scala.collection.mutable
import scala.util.Random

object ProductMain {

	val BUILD_RANDOM_BROCHURES = true

	val dataReader = new DataReader(
		new File("../n2o_data/linked_in_posts.csv"),
		new File("../n2o_data/brochures.csv"),
		new File(CLASSIFICATION_JSON))

	var posts = mutable.ArrayBuffer[Document]()
	var brochures = mutable.ArrayBuffer[Document]()

	def readData(): Unit = {
		posts = mutable.ArrayBuffer[Document]()
		brochures = mutable.ArrayBuffer[Document]()

		if (dataReader != null) {

			dataReader.readPostsLinewise { post =>
				posts += post
			}("category")

			dataReader.readBrochuresLinewise(List("en")) { brochure =>
				brochures += brochure
			}
		}
	}

	val groupSizes  = List(6)
	val classifiers = List(
		new MultilayerPerceptron()
		, new Logistic
		, new SMO()
//		, new TheirClassifier
	)
	val binaryFeatures = List(false, true)
	val normalize = List(false, true)

	def main(args: Array[String]): Unit = {
		readData()

		val sentenceSet = mutable.Map[String, mutable.Set[Seq[Word]]]()

		posts.foreach { post =>
	val docClass = post.documentClass
			if (!sentenceSet.contains(docClass)) {
				sentenceSet(docClass) = mutable.Set[Seq[Word]]()
			}
			sentenceSet(docClass) ++= post.sentences
		}

		if (BUILD_RANDOM_BROCHURES)
			buildRandomBrochures(sentenceSet)

		println(s"Now we have ${posts.size} posts.")

		groupSizes.foreach { groupSize =>
			classifiers.foreach { classifier =>
				binaryFeatures.foreach { useBinaryFeature =>
					normalize.foreach { normalizeFeatures =>
						println(f"Classifier: ${classifier.getClass}, GroupSize: $groupSize, binaryFeature: $useBinaryFeature, normalize: $normalizeFeatures")

						val analyzer = new ProductClassifier(brochures, groupSize, classifier, useBinaryFeature, normalizeFeatures, true, !BUILD_RANDOM_BROCHURES)
						analyzer.buildClassifier()

						analyzer.printValidation(posts)
					}
				}
			}
		}
	}

	def buildRandomBrochures(sentenceSet: mutable.Map[String, mutable.Set[Seq[Word]]]) {
		val r = new Random(44)
		val NUM_DOCS = 30
		val NUM_SENTENCES = 2

		posts.clear()
		sentenceSet.foreach { case (docClass, sentences) =>
			println(s"$docClass ${sentences.size} ${sentences.flatten.size}")
			val sentenceList = sentences.toList
			for (i <- 1 to NUM_DOCS) {
				val newSentences = r.shuffle(sentenceList).take(NUM_SENTENCES + (r.nextInt(4) + 1) / 4)
				posts += Document(r.nextInt().toString, "", newSentences.flatten.mkString(" "), newSentences, docClass)
			}
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
