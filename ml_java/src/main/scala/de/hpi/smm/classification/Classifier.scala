package de.hpi.smm.classification

import java.util

import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.domain.{RawDocument, Document}
import de.hpi.smm.feature_extraction.FeatureExtractor
import weka.classifiers.Evaluation
import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.evaluation.output.prediction.PlainText
import weka.core.{Utils, DenseInstance, Instances, Attribute}
import java.util.Random

class Classifier(val className: String, val documents: List[Document], val featureExtractor: FeatureExtractor, val dataReader: DataReader) {
	val attributes = new util.ArrayList[Attribute]()

	val classNamesVector = new util.ArrayList[String]()
	classNamesVector.add(className)
	classNamesVector.add("no-"+className)

	val classAttribute = new Attribute("@@class@@", classNamesVector)


	for (featureName <- featureExtractor.names) {
		if (featureName != "CLASS" && featureName != "id"){
			attributes.add(new Attribute(featureName))
		}
	}
	attributes.add(classAttribute)

	val instances = new Instances(className, attributes,0)
	instances.setClassIndex(classAttribute.index())

	featureExtractor.buildFeatureVectors(documents, {(document,vector) =>
		val (instance, _, _) = buildInstance(document,vector)
		instances.add(instance)
	})

	val classifier = new NaiveBayes()
	classifier.buildClassifier(instances)

	def buildInstance(document: Document, vector: Array[Double]): (DenseInstance, Array[Double], Array[Double]) = {
		var documentClassName = document.documentClass
		if(documentClassName != className) {
			documentClassName = "no-" + className
		}

		val values = new Array[Double](attributes.size())
		val occProbs = new Array[Double](attributes.size())
		values(classAttribute.index()) = classAttribute.indexOfValue(documentClassName)

		for ((value, index) <- vector.view.zipWithIndex) {
			values(index) = value
			occProbs(index) = 1.0
		}
		(new DenseInstance(1.0, values.map(_.signum.toDouble)), occProbs, null)
	}

	def classProbability(text: String): ClassificationOutput = {
		val id = ""
		val title = ""

		val rawPost = RawDocument(id, title, text, null)
		val sentences = dataReader.detectSentences(rawPost)
		val post = Document(id, title, text, sentences, rawPost.extract(className))

		val (instance, occProbs, _) = featureExtractor.buildFeatureVector(post, {(document, vector) =>
//			println(s"Sum: ${vector.sum}")
			buildInstance(document, vector)
		})

		val l1 = featureExtractor.names.drop(1)
		val l2 = instance.toDoubleArray
		val l3 = occProbs
		val relevantFeatures = l1.zip(l2).zip(l3).map { case ((t1, t2), t3) => (t1, t2, t3) }.filter { case (feature, prob, occProb) =>
			prob > 0 && feature != "CLASS"
		}.map(_.productIterator.toArray)

		instance.setDataset(instances)

		val classValue = classifier.classifyInstance(instance)
		val classN = classAttribute.value(classValue.toInt)
		val dist = classifier.distributionForInstance(instance)
		val counts = featureExtractor.genericCounter.classCounts
		dist(0) /= counts(className)
		dist(1) /= counts.values.sum - counts(className)

		Utils.normalize(dist)
		ClassificationOutput(dist(0), relevantFeatures)
	}

	def crossValidate(): Evaluation = {
		val evaluation = new Evaluation(instances)
		val buffer = new StringBuffer()
		val plainText = new PlainText()
		plainText.setBuffer(buffer)
		plainText.setOutputDistribution(true)
		evaluation.crossValidateModel(classifier, instances, 10, new Random(18), plainText)

		println(plainText.getBuffer)
		evaluation
	}
}
