package de.hpi.smm.classification

import java.util

import com.blog_intelligence.nto.{RawDocument, Document}
import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.nlp.NLP
import weka.classifiers.Evaluation
import weka.classifiers.`lazy`.IBk
import weka.classifiers.functions.SMO
import weka.classifiers.trees.J48
import weka.classifiers.trees.j48.C45PruneableClassifierTree

import weka.core.{Attribute, DenseInstance, Instances}
import weka.filters.Filter
import weka.filters.unsupervised.attribute.{Normalize, StringToWordVector}
import scala.collection.JavaConverters._


class MultiProductClassifier(val brochures: List[Document], val posts: List[Document], classNames: List[String]) {

	val baseClassifier = new IBk(5)
	val classifier = new PriorClassifier(baseClassifier, Array(1.0, 10.00, 10.0, 10.0, 10.0))

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
	tdfIdfFilter.setWordsToKeep(5)
	tdfIdfFilter.setInputFormat(instances)
	
	val filteredInstances = Filter.useFilter(instances,tdfIdfFilter)

	val normelizeFilter = new Normalize()
	normelizeFilter.setInputFormat(filteredInstances)

	val normilizedInstances = Filter.useFilter(filteredInstances,normelizeFilter)
	
	
	classifier.buildClassifier(normilizedInstances)

	def classProbability(text: String): List[Classification] = {
		val id = ""
		val title = ""

		val rawPost = RawDocument(id, title, text, null)
		val sentences = NLP.detectSentences(rawPost)
		val post = Document(id, title, text, sentences, "None")

		val instance = buildInstance(post)

		val classifyInstances = new Instances("toClassify", attributes,0)
		classifyInstances.add(instance)

		val filteredClassifyInstances = Filter.useFilter(classifyInstances,tdfIdfFilter)
		val normilizedClassifyInstances = Filter.useFilter(filteredClassifyInstances,normelizeFilter)

		val dist = classifier.distributionForInstance(normilizedClassifyInstances.get(0))

		dist.toList.zipWithIndex.map { case (distValue, index) =>
			Classification(classAttribute.value(index),ClassificationOutput(distValue,new Array[Array[Any]](0)))
		}
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

		val normilizedTestInstances = Filter.useFilter(filteredTestInstances,normelizeFilter)

		val evaluation = new Evaluation(normilizedTestInstances)
		evaluation.evaluateModel(classifier,normilizedTestInstances)

		evaluation
	}
}
