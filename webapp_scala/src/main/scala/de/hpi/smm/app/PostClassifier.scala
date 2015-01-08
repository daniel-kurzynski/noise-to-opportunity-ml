package de.hpi.smm.app

import java.io.{File, FileReader}
import java.util

import com.lambdaworks.jacks.JacksMapper
import de.hpi.smm.FeatureExtractorBuilder
import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.domain.Document
import de.hpi.smm.feature_extraction.FeatureExtractor
import weka.classifiers.bayes.NaiveBayes
import weka.core.{FastVector, DenseInstance, Instances, Attribute}

import scala.util.Random

case class Classification(cls: String, prob: Double)

class PostClassifier {

	val FOR_ALL_POSTS = false

	val classifiedPosts = JacksMapper.readValue[Map[String, Map[String, Map[String, String]]]](
		new FileReader("../webapp_python/data/classification.json"))
	val postsFile = new File("../n2o_data/linked_in_posts.csv")
	val brochuresFile = new File("../n2o_data/brochures.csv")

	val dataReader = new DataReader(classifiedPosts, postsFile, brochuresFile, FOR_ALL_POSTS);

	val featureExtractorBuilder = new FeatureExtractorBuilder(dataReader)

	val demandFeatureExtractor = featureExtractorBuilder.buildDemandFeautureExtractor()

	val demandClassifier = buildClassifier("demand", featureExtractorBuilder.posts, demandFeatureExtractor)


	private def buildClassifier(className: String, documents: List[Document], featureExtractor: FeatureExtractor): NaiveBayes  = {

		val attributes = new FastVector()
		//val idAttribure = new Attribute("id", ids(documents))

		val classNamesVector = new FastVector()
		classNamesVector.addElement(className)
		classNamesVector.addElement("no-"+className)

		val classAttribute = new Attribute("@@class@@", classNamesVector)

		//attributes.addElement(idAttribure)

		for(feautureName<-featureExtractor.names) {
			if(feautureName!="CLASS" && feautureName!="id"){
				attributes.addElement(new Attribute(feautureName))
			}
		}
		attributes.addElement(classAttribute)

		val instances = new Instances(className, attributes,0);
		instances.setClassIndex(classAttribute.index());

		val values = Array[Double](attributes.size())

		demandFeatureExtractor.buildFeatureVectors(documents, {(document,vector) => {

			var documentClassName = document.documentClass
			if(documentClassName!=className){
				documentClassName = "no-"+className
			}

			values(classAttribute.index()) = classAttribute.indexOfValue(documentClassName)

			for((value,index)<-vector.view.zipWithIndex){
				values(index) = value
			}
			val instance = new DenseInstance(1.0,values)
			instances.add(instance)
			}
		});

		val classifier = new NaiveBayes()
		classifier.buildClassifier(instances)

		classifier
	}

	private def ids(documents: List[Document]):FastVector={

		val idLists = documents.collect{ case document: Document =>
			document.id
		}

		val idSet = idLists.toSet

		val ids = new FastVector()

		idSet.foreach { id =>
			ids.addElement(id)
		}
		ids
	}



	val r = new Random
	def classifyDemand(text: String): Classification = {
		Classification("demand", r.nextDouble())
	}

	/**
	 * Returns an ordered list of classifications.
	 */
	def classifyProduct(text: String): List[Classification] = {
		List(
			Classification("HCM" , 0.8),
			Classification("ECOM", 0.6),
			Classification("CRM" , 0.4),
			Classification("LVM" , 0.2)
		)
	}
}
