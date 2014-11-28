package de.hpi.smm

import java.io.{FileWriter, FileReader, File}

import au.com.bytecode.opencsv.{CSVWriter, CSVReader}

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
			features.touch(post)
			println(post.data)
			println(post.tokens.mkString(" "))
//			val vec = features.buildFeatureVector(post)
//			writer.writeNext(vec.map(_.toString))
		}(1)
		val featureFile = new File("../n2o_data/features.csv")
		val writer = new CSVWriter(new FileWriter(featureFile), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER)
		writer.writeNext(features.names)
		features.buildFeatureVector().foreach { instance =>
			writer.writeNext(instance.map(_.toString))
		}
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
			val id = line(0)
			val title = line(1)
			val text = line(2)
			val wholeText = s"$title $text"
			val tokens = TokenizerHelper.tokenize(wholeText, false)
			extractor(Post(id, title, text, tokens))
			line = reader.readNext()
		}
		reader.close()

	}

}
