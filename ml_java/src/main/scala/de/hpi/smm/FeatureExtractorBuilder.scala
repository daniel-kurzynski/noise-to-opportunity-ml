package de.hpi.smm

import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.domain.Document
import de.hpi.smm.feature_extraction.FeatureExtractor

class FeatureExtractorBuilder(val dataReader: DataReader) {

	var posts = List[Document]()
	var postForCategory = List[Document]()
	var brochures = List[Document]()

	dataReader.readPostsLinewise { post =>
		posts ::= post
	}()

	dataReader.readPostsLinewise { post =>
		postForCategory ::= post
	}("category")

	dataReader.readBrochuresLinewise(List("en")) { brochure =>
		brochures ::= brochure
	}



	def buildForDemand(): FeatureExtractor = {
		val smoothing = false

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

	def buildForBrochures(clsName:String, thresh1:Double, thresh2:Double): FeatureExtractor = {
		val smoothing = true

		val features = new FeatureExtractor(smoothing)
			.needWords(clsName, (thresh1, thresh2))

		brochures.foreach { brochure =>
			features.touch(brochure)
		}

		features.finishTraining()

		features
	}
}
