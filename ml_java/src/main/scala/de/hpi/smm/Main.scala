package de.hpi.smm

import java.io.{FileReader, File}

import au.com.bytecode.opencsv.CSVReader

object Main {

	def main(args: Array[String]): Unit = {
		val features = FeatureBuilder()
			.questionNumber()
			.imperativeWords()
			.mostCommonWordsFromBOW()
			.needWords()
			.share()
			.thankYou()
		extractPostsLinewise { post =>
			features.buildFeatureVector(post)
		}(1)
	}

	def extractPostsLinewise(extractor: Post => Unit)(count: Int = Int.MaxValue): Unit = {
		val postsFile = new File("../n2o_data/linked_in_posts.csv")
		val reader = new CSVReader(new FileReader(postsFile))

		var lineNumber: Int = 1
		var line: Array[String] = reader.readNext()
		while (line != null && lineNumber <= count) {
			if (line.size != 11)
				println("WRONG LINE NUMBER SIZE in $lineNumber")
			lineNumber += 1
			extractor(Post(line(0), line(1), line(2)))
			line = reader.readNext()
		}
		reader.close()

	}

}
