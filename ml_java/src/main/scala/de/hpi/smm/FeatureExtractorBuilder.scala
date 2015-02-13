package de.hpi.smm

import com.blog_intelligence.nto.Document
import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.feature_extraction.FeatureExtractor
import scala.collection.JavaConverters._

class FeatureExtractorBuilder(val dataReader: DataReader) {

	var posts = List[Document]()
	var postForCategory = List[Document]()
	var brochures = List[Document]()


	if (dataReader != null) {
		dataReader.readPostsLinewise { post =>
			posts ::= post
		}()

		dataReader.readPostsLinewise { post =>
			postForCategory ::= post
		}("category")

		dataReader.readBrochuresLinewise(List("en")) { brochure =>
			brochures ::= brochure
		}
	}

	def buildForDemand(givenPosts: java.util.List[Document] = null): FeatureExtractor = {
		val usedPosts = if (givenPosts != null) givenPosts.asScala else posts
		val smoothing = false

		val features = new FeatureExtractor(smoothing)
			.needWords("demand", (5.0, 2.0))
			.questionNumber()
			.needNGrams()
			.containsEMail()
			.addressTheReader()
			.questionWords()
			.imperativeWords()

		usedPosts.foreach { post =>
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
