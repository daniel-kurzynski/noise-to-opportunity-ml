package de.hpi.smm.classification

import java.util

import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.domain.{RawDocument, Document}
import de.hpi.smm.feature_extraction.FeatureExtractor
import weka.classifiers.Evaluation
import weka.classifiers.`lazy`.IBk
import weka.core.{FastVector, DenseInstance, Attribute, Instances}
import weka.filters.Filter
import weka.filters.unsupervised.attribute.StringToWordVector

/**
 * Created by Daniel on 16.01.2015.
 */
class ProductClassifier(val className: String, val documents: List[Document], val posts: List[Document], val dataReader: DataReader) {

	val classifier = new IBk()

	val attributes = new util.ArrayList[Attribute]()

	val classNamesVector = new util.ArrayList[String]()
	classNamesVector.add(className)
	classNamesVector.add("no-"+className)

	val textAttribute = new Attribute("text", null.asInstanceOf[util.ArrayList[String]])
	val classAttribute = new Attribute("@@class@@", classNamesVector)

	attributes.add(textAttribute)
	attributes.add(classAttribute)

	val instances = new Instances(className, attributes,0)
	instances.setClassIndex(classAttribute.index())

	documents.foreach { document =>
		instances.add(buildInstance(document))
	}

	val tdfIdfFilter = new StringToWordVector()
	tdfIdfFilter.setInputFormat(instances)
	val filteredInstances = Filter.useFilter(instances,tdfIdfFilter)

	classifier.buildClassifier(filteredInstances)

	def classProbability(text: String): ClassificationOutput = {
		val id = ""
		val title = ""

		val rawPost = RawDocument(id, title, text, null)
		val sentences = dataReader.detectSentences(rawPost)
		val post = Document(id, title, text, sentences, rawPost.extract(className))

		val instance = buildInstance(post)
		val dist = classifier.distributionForInstance(instance)

		ClassificationOutput(dist(0), new Array[Array[Any]](0))
	}

	def buildInstance(document: Document): DenseInstance = {
		val text = document.textTokens.mkString(" ")

		var documentClassName = document.documentClass
		if (documentClassName != className) {
			documentClassName = "no-" + className
		}

		val values = new Array[Double](attributes.size())
		values(classAttribute.index()) = classAttribute.indexOfValue(documentClassName)
		values(textAttribute.index()) = textAttribute.addStringValue(text)

		new DenseInstance(1, values)
	}

	def validate(): Evaluation = {
		val testInstances = new Instances("test"+className, attributes, 0)
		testInstances.setClassIndex(classAttribute.index())
		posts.foreach { post =>
			testInstances.add(buildInstance(post))
		}

		val filteredTestInstances = Filter.useFilter(testInstances,tdfIdfFilter)

		val evaluation = new Evaluation(filteredTestInstances)
		evaluation.evaluateModel(classifier,filteredTestInstances)

		evaluation
	}
}
