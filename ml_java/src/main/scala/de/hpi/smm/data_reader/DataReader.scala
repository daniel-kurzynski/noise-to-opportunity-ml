package de.hpi.smm.data_reader

import java.io.{File, FileReader}

import de.hpi.smm.Constants._
import au.com.bytecode.opencsv.CSVReader
import com.blog_intelligence.nto.{ReadingResult, RawDocument, Document}
import com.lambdaworks.jacks.JacksMapper
import de.hpi.smm.nlp.NLP

class DataReader(val postsFile: File, val brochuresFile: File) {

	val classifiedPosts = JacksMapper.readValue[Map[String, Map[String, Map[String, String]]]](
		new FileReader(CLASSIFICATION_JSON))

	def readBrochuresLinewise(languages: List[String] = List("de", "en"))(extractor: Document => Unit): Unit = {
		val reader = new CSVReader(new FileReader(brochuresFile))

		var brochuresCount: Int = 1
		var line: Array[String] = reader.readNext()
		var lastID = "-1"
		while (line != null && brochuresCount <= Int.MaxValue) {
			if(line.length<4)
				println("!"+line(0)+", lastID: "+lastID)
			val id = line(0)
			lastID = id
			val text = line(1)
			val classification = line(2)
			val language = line(4)

			if (languages.contains(language)) {
				val rawPost = RawDocument(id, "", text, null, language)

				brochuresCount += 1
				val sentences = NLP.detectSentences(rawPost)
				extractor(Document(id, "", text, sentences, classification))
			}
			line = reader.readNext()
		}
		reader.close()
	}

	def readPostsLinewise(extractor: Document => Unit)(className: String = "demand", count: Int = Int.MaxValue): Unit = {
		val reader = new CSVReader(new FileReader(postsFile))

		var postCount: Int = 1
		var line: Array[String] = reader.readNext()
		while (line != null && postCount <= count) {
			val id = line(0)
			val title = line(1)
			val text = line(2)

			val rawPost = RawDocument(id, title, text, classifiedPosts.get(id).orNull)

			val isClassifiedPost = classifiedPosts.contains(id)
			if (isClassifiedPost) {
				val sentences = NLP.detectSentences(rawPost)
				val post = Document(id, title, text, sentences, rawPost.extract(className))
//				if(post.documentClass != "None"){
					postCount += 1
					extractor(post)
//				}
			}
			line = reader.readNext()
		}
		reader.close()
	}

	def getReadingResult: ReadingResult = {

		val demandDocs = new java.util.ArrayList[Document]()
		val productDocs = new java.util.ArrayList[Document]()

		this.readPostsLinewise { post =>
			demandDocs.add(post)
		}()

		this.readBrochuresLinewise(List("en")) { brochure =>
			productDocs.add(brochure)
		}

		ReadingResult(demandDocs, productDocs)
	}

}
