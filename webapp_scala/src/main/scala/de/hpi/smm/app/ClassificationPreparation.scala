package de.hpi.smm.app

import java.io.{File, FileReader}

import com.lambdaworks.jacks.JacksMapper
import de.hpi.smm.FeatureExtractorBuilder
import de.hpi.smm.data_reader.DataReader

import de.hpi.smm.classification.PostClassifier
import org.json4s.{DefaultFormats, Formats}

object ClassificationPreparation {

	val classifiedPosts = JacksMapper.readValue[Map[String, Map[String, Map[String, String]]]](
		new FileReader("../webapp_python/data/classification.json"))
	val postsFile = new File("../n2o_data/linked_in_posts.csv")
	val brochuresFile = new File("../n2o_data/brochures.csv")

	val dataReader = new DataReader(classifiedPosts, postsFile, brochuresFile, false)

	val featureExtractorBuilder = new FeatureExtractorBuilder(dataReader)
	val postClassifier = new PostClassifier(featureExtractorBuilder)

	println("Finished preparing post prediction")

}
