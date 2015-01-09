package de.hpi.smm.classification

import java.util

import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.domain.{RawDocument, Document}
import de.hpi.smm.feature_extraction.FeatureExtractor
import weka.classifiers.Evaluation
import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.evaluation.output.prediction.PlainText
import weka.core.{DenseInstance, Instances, Attribute}
import java.util.Random

/**
 * Created by Daniel on 08.01.2015.
 */
class Classifier(val className: String, val documents: List[Document], val featureExtractor: FeatureExtractor, val dataReader: DataReader) {
	val attributes = new util.ArrayList[Attribute]()

	val classNamesVector = new util.ArrayList[String]()
	classNamesVector.add(className)
	classNamesVector.add("no-"+className)

	val classAttribute = new Attribute("@@class@@", classNamesVector)


	for(feautureName<-featureExtractor.names) {
		if(feautureName!="CLASS" && feautureName!="id"){
			attributes.add(new Attribute(feautureName))
		}
	}
	attributes.add(classAttribute)

	val instances = new Instances(className, attributes,0);
	instances.setClassIndex(classAttribute.index());

	featureExtractor.buildFeatureVectors(documents, {(document,vector) => {

		val instance = buildInstance(document,vector)

		instances.add(instance)
	}
	});

	val classifier = new NaiveBayes()
	classifier.buildClassifier(instances)

	def buildInstance(document: Document, vector: Array[Double]):DenseInstance={
		var documentClassName = document.documentClass
		if(documentClassName!=className){
			documentClassName = "no-"+className
		}

		val values = new Array[Double](attributes.size())
		values(classAttribute.index()) = classAttribute.indexOfValue(documentClassName)

		for((value,index)<-vector.view.zipWithIndex){
			values(index) = value
		}
		new DenseInstance(1.0,values)
	}

	def classPropability(text: String):Double={
		val id = ""
		val title = ""

		val rawPost = RawDocument(id, title, text, null)
		val sentences = dataReader.detectSentences(rawPost)
		val post = Document(id, title, text, sentences, rawPost.extract(className))

		val instance = featureExtractor.buildFeatureVector(post, {(document, vector) =>
			val sum = vector.sum
			buildInstance(document,vector)
		})

		instance.setDataset(instances)

		val classValue = classifier.classifyInstance(instance)
		val classN = classAttribute.value(classValue.toInt)
		val dist = classifier.distributionForInstance(instance)

		dist(0) / (dist.sum)

	}

	def crossValidate(): Evaluation ={

		val evaluation = new Evaluation(instances)
		val buffer = new StringBuffer()
		val plainText = new PlainText()
		plainText.setBuffer(buffer)
		plainText.setOutputDistribution(true)
		evaluation.crossValidateModel(classifier, instances, 10, new Random(1),plainText)

		println(plainText.getBuffer)

		evaluation
	}
}
