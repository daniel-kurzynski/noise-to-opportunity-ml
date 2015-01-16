package de.hpi.smm.classification

import java.util

import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.domain.{Document, RawDocument}
import weka.classifiers.Evaluation
import weka.classifiers.`lazy`.IBk
import weka.classifiers.functions.SMO
import weka.classifiers.trees.J48
import weka.classifiers.trees.j48.C45PruneableClassifierTree

import weka.core.{Attribute, DenseInstance, Instances}
import weka.filters.Filter
import weka.filters.unsupervised.attribute.StringToWordVector
import scala.collection.JavaConverters._


class MultiProductClassifier(val brochures: List[Document], val posts: List[Document], val dataReader: DataReader) {

	val baseClassifier = new IBk(20)
	val classifier = new PriorClassifier(baseClassifier, Array(1.0, 1000.0, 10000.0, 10000.0, 10000.0))

	val attributes = new util.ArrayList[Attribute]()

	val classNamesVector = new util.ArrayList[String]()
	classNamesVector.addAll(List("CRM", "ECOM", "HCM", "LVM", "None").asJavaCollection)

	val textAttribute = new Attribute("text", null.asInstanceOf[util.ArrayList[String]])
	val classAttribute = new Attribute("@@class@@", classNamesVector)

	attributes.add(textAttribute)
	attributes.add(classAttribute)

	val instances = new Instances("classifier", attributes,0)
	instances.setClassIndex(classAttribute.index())

	brochures.foreach { brochure =>
		instances.add(buildInstance(brochure))
	}

	val tdfIdfFilter = new StringToWordVector()
	tdfIdfFilter.setIDFTransform(true)
	tdfIdfFilter.setTFTransform(true)
	tdfIdfFilter.setWordsToKeep(40)
	tdfIdfFilter.setInputFormat(instances)
	val filteredInstances = Filter.useFilter(instances,tdfIdfFilter)

	classifier.buildClassifier(filteredInstances)

	def classProbability(text: String): ClassificationOutput = {
		val id = ""
		val title = ""

		val rawPost = RawDocument(id, title, text, null)
		val sentences = dataReader.detectSentences(rawPost)
		val post = Document(id, title, text, sentences, "None")

		val instance = buildInstance(post)
		val dist = classifier.distributionForInstance(instance)

		ClassificationOutput(dist(0), new Array[Array[Any]](0))
	}

	def buildInstance(document: Document): DenseInstance = {
		val text = document.textTokens.mkString(" ")

		val values = new Array[Double](attributes.size())
		values(classAttribute.index()) = classAttribute.indexOfValue(document.documentClass)
		values(textAttribute.index()) = textAttribute.addStringValue(text)

		new DenseInstance(1, values)
	}

	def validate(): Evaluation = {
		val testInstances = new Instances("testClassifier", attributes, 0)
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
