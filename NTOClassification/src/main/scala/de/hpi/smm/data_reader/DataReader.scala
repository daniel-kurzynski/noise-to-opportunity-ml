package de.hpi.smm.data_reader

import java.io.{File, FileReader}
import java.util.Collections

import au.com.bytecode.opencsv.CSVReader
import com.blog_intelligence.nto.{ReadingResult, RawDocument, Document}
import com.lambdaworks.jacks.JacksMapper
import de.hpi.smm.domain.Word
import de.hpi.smm.nlp.NLP
import scala.collection.mutable
import scala.collection.JavaConverters._
import scala.util.Random

object DataReader {
	val theirClassifications = mutable.Map[String, String]()
}

class DataReader(val postsFile: File, val brochuresFile: File, classificationFile: File, nlp: NLP, val includeNone: Boolean = false) {

	val classifiedPosts = JacksMapper.readValue[Map[String, Map[String, Map[String, String]]]](
			new FileReader(classificationFile))


	def readBrochuresLinewise(languages: List[String] = List("de", "en"))(extractor: Document => Unit): Unit = {
		val reader = new CSVReader(new FileReader(brochuresFile))

		var brochuresCount: Int = 1
		var line: Array[String] = reader.readNext()
		var lastID = "-1"
		while (line != null && brochuresCount <= Int.MaxValue) {
			if (line.length < 4)
				println("!" + line(0) + ", lastID: " + lastID)
			val id = line(0)
			lastID = id
			val text = line(1)
			val classification = line(2)
			val language = line(4)

			if (languages.contains(language)) {
				val rawPost = RawDocument(id, "", text, null, language)

				brochuresCount += 1
				val sentences = nlp.detectSentences(rawPost)
				extractor(Document(id, "", text, sentences, classification))
			}
			line = reader.readNext()
		}
		reader.close()
	}

	def readPostsLinewise(extractor: Document => Unit)(className: String = "demand", count: Int = Int.MaxValue, all: Boolean = false): Unit = {
		val reader = new CSVReader(new FileReader(postsFile))

		var postCount: Int = 1
		var line: Array[String] = reader.readNext()
		while (line != null && postCount <= count) {
			val id = line(0)
			val title = line(1)
			val text = line(2)
			val theirClassification = line(8)
			DataReader.theirClassifications(id) = theirClassification

			val rawPost = RawDocument(id, title, text, classifiedPosts.get(id).orNull)

			if (all || classifiedPosts.contains(id)) {
				val sentences = nlp.detectSentences(rawPost)
        val extractedClass = if (className == "nto") {
          if (rawPost.extractDemand() == "demand")
            rawPost.extractCategory()
          else
            "None"
        } else
          rawPost.extract(className)
				val post = Document(id, title, text, sentences, extractedClass)
				if (includeNone || post.documentClass != "None") {
					postCount += 1
					extractor(post)
				}
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
//		val sentences = mutable.Map[String, Set[Seq[Word]]]()
//		demandDocs.asScala.foreach { post =>
//			val docClass = post.documentClass
//			if (!sentences.contains(docClass))
//				sentences(docClass) = Set[Seq[Word]]()
//			sentences(docClass) ++= post.sentences
//		}
//		demandDocs.clear()
//		sentences.foreach { case (key, set) =>
//			println(s"$key --> ${set.size}")
//		}
//		val r = new Random(7)
//		var i = 0
//		sentences.foreach { case (docClass, sentencesForKey) =>
//			for (_ <- 1 to 300) {
//				val chosenSentences = r.shuffle(sentencesForKey.toList).take(2)
//				val doc = Document(i.toString, "", chosenSentences.flatten.mkString(" "), chosenSentences, docClass)
//				demandDocs.add(doc)
//				i += 1
//			}
//		}
//		Collections.shuffle(demandDocs, new java.util.Random(7))
//		println(demandDocs.size)

		this.readBrochuresLinewise(List("en")) { brochure =>
			productDocs.add(brochure)
		}

		ReadingResult(demandDocs, productDocs)
	}

}
