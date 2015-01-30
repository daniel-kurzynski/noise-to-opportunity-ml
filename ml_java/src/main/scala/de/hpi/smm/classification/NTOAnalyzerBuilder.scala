package de.hpi.smm.classification

import java.io.{FileNotFoundException, File, FileReader}

import com.lambdaworks.jacks.JacksMapper
import de.hpi.smm.Constants._
import de.hpi.smm.FeatureExtractorBuilder
import de.hpi.smm.data_reader.DataReader

object NTOAnalyzerBuilder {
	def build(classificationFile: File, brochuresFile: File, postsFile: File): NTOAnalyzer = {

		List(classificationFile, brochuresFile, postsFile).foreach { file =>
			if (!file.exists())
				throw new FileNotFoundException(file.getAbsolutePath)
		}

		val dataReader = new DataReader(postsFile, brochuresFile)
		val featureExtractorBuilder = new FeatureExtractorBuilder(dataReader)
		new NTOAnalyzer(featureExtractorBuilder)
	}
}
