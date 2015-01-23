package de.hpi.smm.classification

import java.io.{FileNotFoundException, File, FileReader}

import com.lambdaworks.jacks.JacksMapper
import de.hpi.smm.Constants._
import de.hpi.smm.FeatureExtractorBuilder
import de.hpi.smm.data_reader.DataReader

/**
 * Created by Daniel on 23.01.2015.
 */
object NTOAnalyzerBuilder {
	def build(classificationFile: File, brochuresFile: File, postsFile: File): NTOAnalyzer = {

		List(classificationFile, brochuresFile, postsFile).foreach { file =>
			if (!file.exists())
				throw new FileNotFoundException(file.getAbsolutePath)
		}

		val classifiedPosts = JacksMapper.readValue[Map[String, Map[String, Map[String, String]]]](
			new FileReader(classificationFile))
		val dataReader = new DataReader(classifiedPosts, postsFile, brochuresFile, false)
		val featureExtractorBuilder = new FeatureExtractorBuilder(dataReader)
		new NTOAnalyzer(featureExtractorBuilder)
	}
}
