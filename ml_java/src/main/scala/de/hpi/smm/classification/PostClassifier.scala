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

	val demandClassifier = new Classifier( "demand",
		featureExtractorBuilder.posts,
		featureExtractorBuilder.buildDemandFeatureExtractor(),
		featureExtractorBuilder.dataReader)

	val CRMClassifier = new Classifier( "CRM",
		featureExtractorBuilder.posts,
		featureExtractorBuilder.buildBroshuresFeatureExtractor("CRM", 2.0, 5.0),
		featureExtractorBuilder.dataReader)

	val ECOMClassifier = new Classifier( "ECOM",
		featureExtractorBuilder.posts,
		featureExtractorBuilder.buildBroshuresFeatureExtractor("ECOM", 4.0, 100.0),
		featureExtractorBuilder.dataReader)

	val HCMClassifier = new Classifier( "HCM",
		featureExtractorBuilder.posts,
		featureExtractorBuilder.buildBroshuresFeatureExtractor("HCM", 3.0, 13.0),
		featureExtractorBuilder.dataReader)

	val LVMClassifier = new Classifier( "LVM",
		featureExtractorBuilder.posts,
		featureExtractorBuilder.buildBroshuresFeatureExtractor("LVM", 7.0, 35.0),
		featureExtractorBuilder.dataReader)

	def classifyDemand(text: String): Classification = {
		Classification("demand", demandClassifier.classProbability(text))
	}

	/**
	 * Returns an ordered list of classifications.
	 */
	def classifyProduct(text: String): List[Classification] = {
		List(
			Classification("HCM" , HCMClassifier.classProbability(text)),
			Classification("ECOM", ECOMClassifier.classProbability(text)),
			Classification("CRM" , CRMClassifier.classProbability(text)),
			Classification("LVM" , LVMClassifier.classProbability(text))
		).sortBy(-_.classificationOutput.prob)
	}
}
