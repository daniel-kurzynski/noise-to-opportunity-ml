package de.hpi.smm

import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.domain.Document
import de.hpi.smm.feature_extraction.FeatureExtractor

/**
 * Created by Daniel on 06.01.2015.
 */
class FeatureExtractorBuilder(val dataReader: DataReader) {

	var posts = List[Document]()
	var postForCategory = List[Document]()
	var broshures = List[Document]()

	dataReader.readPostsLinewise { post =>
		posts ::= post
	}()

	dataReader.readPostsLinewise { post =>
		postForCategory ::= post
	}("category")

	dataReader.readBrochuresLinewise { broshure =>
		broshures ::= broshure
	}()



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

		posts.foreach { post =>
			features.touch(post)
		}

		features.finishTraining()

		features.removeClassCounts("no-idea")

		features
	}

	def buildBroshuresFeatureExtractor(clsName:String, thresh1:Double, thresh2:Double):FeatureExtractor={
		val smoothing = true;

		val features = new FeatureExtractor(smoothing)
			.needWords(clsName, (thresh1, thresh2))
			.needNGrams()

		broshures.foreach { brochure =>
			features.touch(brochure)
		}

		features.finishTraining()

		features
	}
}
