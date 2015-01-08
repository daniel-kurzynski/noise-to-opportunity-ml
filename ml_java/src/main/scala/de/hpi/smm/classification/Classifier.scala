package de.hpi.smm.classification

import java.util

import de.hpi.smm.domain.Document
import de.hpi.smm.feature_extraction.FeatureExtractor
import weka.classifiers.bayes.NaiveBayes
import weka.core.{DenseInstance, Instances, Attribute}

/**
 * Created by Daniel on 08.01.2015.
 */
class Classifier(val className: String, val documents: List[Document], val featureExtractor: FeatureExtractor) {
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

	def classify(text: String):String={

		var instance = new DenseInstance(1,Array())

//		featureExtractor.buildFeatureVectors(List(new Document(text)), {(document,vector) => {
//
//			instance = buildInstance(document,vector)
//		}
//		});

		val classValue = classifier.classifyInstance(instance)

		classAttribute.value(classValue.toInt)

	}
}
