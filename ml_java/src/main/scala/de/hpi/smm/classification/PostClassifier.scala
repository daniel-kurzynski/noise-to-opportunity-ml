package de.hpi.smm.classification

import java.util
import java.io.{File, FileReader}

import scala.util.Random

import com.lambdaworks.jacks.JacksMapper
import de.hpi.smm.FeatureExtractorBuilder
import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.domain.Document
import de.hpi.smm.feature_extraction.FeatureExtractor
import weka.classifiers.bayes.NaiveBayes
import weka.core.{Attribute, DenseInstance, Instances}

case class ClassificationOutput(prob: Double, relevantFeatures: Array[Array[Any]] = Array())
case class Classification(cls: String, classificationOutput: ClassificationOutput)

class PostClassifier(val featureExtractorBuilder: FeatureExtractorBuilder) {

	val r = new Random

	val demandFeatureExtractor = featureExtractorBuilder.buildDemandFeautureExtractor()
	val demandClassifier = new Classifier("demand", featureExtractorBuilder.posts, demandFeatureExtractor, featureExtractorBuilder.dataReader)

	def classifyDemand(text: String): Classification = {
		val classificationOutput = demandClassifier.classProbability(text)
		Classification("demand", classificationOutput)
	}

	/**
	 * Returns an ordered list of classifications.
	 */
	def classifyProduct(text: String): List[Classification] = {
		List(
			Classification("HCM" , ClassificationOutput(r.nextDouble())),
			Classification("ECOM", ClassificationOutput(0.6)),
			Classification("CRM" , ClassificationOutput(0.4)),
			Classification("LVM" , ClassificationOutput(0.2))
		)
	}
}
