package de.hpi.smm

import java.io.{FileWriter, FileReader, File}

import au.com.bytecode.opencsv.{CSVWriter, CSVReader}

object Main {

	def main(args: Array[String]): Unit = {
		val featureFile = new File("../n2o_data/features.csv")
		val writer = new CSVWriter(new FileWriter(featureFile))
		val features = FeatureBuilder()
			.questionNumber()
			.imperativeWords()
			.mostCommonWordsFromBOW()
			.needWords()
			.share()
			.thankYou()
		extractPostsLinewise { post =>
			val vec = features.buildFeatureVector(post)
			writer.writeNext(vec.map(_.toString))
		}(1)
		writer.close()
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