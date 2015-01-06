package de.hpi.smm

import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.feature_extraction.FeatureExtractor

/**
 * Created by Daniel on 06.01.2015.
 */
class FeatureExtractorBuilder(val dataReader: DataReader) {

	def buildDemandFeautureExtractor():FeatureExtractor={

		val smoothing = false;

		val features = new FeatureExtractor(smoothing)
			.needWords("demand", (5.0, 2.0))
			.questionNumber()
			.needNGrams()
			.containsEMail()
			.addressTheReader()
			.questionWords()
			.imperativeWords()

		dataReader.readPostsLinewise { post =>
			features.touch(post)
		}()
		features.finishTraining()

		features.removeClassCounts("no-idea")

		features
	}

	def buildBroshuresFeatureExtractor(clsName:String, thresh1:Double, thresh2:Double):FeatureExtractor={
		val smoothing = true;

		val features = new FeatureExtractor(smoothing)
			.needWords(clsName, (thresh1, thresh2))
			.needNGrams()


		// extract train features
		dataReader.readBrochuresLinewise { brochure =>
			features.touch(brochure)
		}()

		features.finishTraining()

		features
	}
}
