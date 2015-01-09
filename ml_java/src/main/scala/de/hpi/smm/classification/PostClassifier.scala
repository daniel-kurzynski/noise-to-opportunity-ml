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

case class Classification(cls: String, prob: Double)

class PostClassifier(val featureExtractorBuilder: FeatureExtractorBuilder) {

	val demandFeatureExtractor = featureExtractorBuilder.buildDemandFeautureExtractor()

	val demandClassifier = new Classifier("demand", featureExtractorBuilder.posts, demandFeatureExtractor, featureExtractorBuilder.dataReader)




	val r = new Random
	def classifyDemand(text: String): Classification = {

		val prob = demandClassifier.classProbability(text)
		Classification("demand", prob)
	}

	/**
	 * Returns an ordered list of classifications.
	 */
	def classifyProduct(text: String): List[Classification] = {
		List(
			Classification("HCM" , r.nextDouble()),
			Classification("ECOM", 0.6),
			Classification("CRM" , 0.4),
			Classification("LVM" , 0.2)
		)
	}
}
